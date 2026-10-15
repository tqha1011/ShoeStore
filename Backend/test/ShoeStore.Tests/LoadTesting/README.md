# Local checkout race/load testing

Run from the repository root (Linux, .NET 10 SDK, Docker Compose v2, k6 and curl):

```bash
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh all 500
```

The command starts **only** the `database` service from `Backend/compose.yaml`
under Compose project `shoestore-loadtest`. PostgreSQL + pgvector listens on
`127.0.0.1:5433`, database `shoestore_loadtest`. The other Compose services are
not started. It runs the unit suite, applies existing EF migrations, seeds a
fresh database, starts the API on `127.0.0.1:8080`, runs k6, audits committed
orders against stock, and stops its API when done. The database volume remains.
Port 8080 must be available; the runner refuses to reuse an unknown API.
The local API connection pool is capped at 80, leaving room in PostgreSQL's
100 client slots for the audit tool. A 500-user burst queues requests through
that bounded pool; it does not open 500 database connections.

## Dataset

| Entity | Seed count |
| --- | ---: |
| Test customers | 1,000, plus the existing migrated admin |
| Products | 500, five brands |
| Variants | 7,500: three colours × five sizes per product |
| Historical invoices | 50,000, delivered COD orders over 180 days |
| Historical invoice lines | 100,000, quantities 1–3 |
| Payment methods | SePay (1), COD (2) |

Catalogue prices range from VND 500,000 to 3,400,000. Initial current stock
varies from 20 to 200 pairs per variant. Historical sales occurred before the
current inventory snapshot (including previous restocks).
This is synthetic data, with no real customer information. Images, addresses,
vouchers, bank webhooks and AI embeddings are not seeded; this test exercises
direct COD checkout with no vouchers.

Seed only:

```bash
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh seed
```

Seed runs once in a transaction and records a version marker. Running it again
does not duplicate rows, overwrite stock or delete orders. It rejects an
unrecognised populated catalogue. The tool accepts only a loopback connection
to `shoestore_loadtest`; `Backend/.env` and its old Azure settings are not loaded.

Customers are `test1@loadtest.local` through `test1000@loadtest.local`.
Their default password is `LoadTest_Local_2026!`. Set `TEST_PASSWORD` **before
the first seed** to choose another password. Passwords use the app's BCrypt
hasher. The runner uses a separate, local-only JWT key and generates tokens
with the app's `TokenService`. It does not send 500 login requests through the
15 requests/IP/minute signin policy; the checkout rate limiter remains enabled.
JWTs expire in one hour and generated token files are excluded from git and
the Docker build context.

## Checkout race scenarios

```bash
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh last-pair 500
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh hot-sku 500
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh mixed 500
```

Each scenario uses 500 separate customers, preinitializes 500 VUs, and waits
for a shared timestamp before sending **one checkout per VU**, without client retry.
The API retries optimistic concurrency internally, at most five transaction
attempts per request, with exponential backoff and jitter (25–600 ms per wait).
Each retry rolls back, clears EF tracking, and rereads inventory, user, cart
and vouchers. A successful request commits only one invoice.
This is a concurrent burst, not 500 requests/second or 500 sustained sessions.
The maximum is 1,000 users. A preflight `prepare` request checks JWT, API and DB
before starting the burst. Checkout uses `fromUserCart=false`, COD and no voucher.

| Mode | Requests | Expected result |
| --- | --- | --- |
| `last-pair` | Same variant, stock reset to 1 | Exactly 1 success; other requests receive stock rejection 400/409 or exhausted-retry 409 |
| `hot-sku` | Same variant, stock reset to 100 | 1–100 successes; other requests receive 400/409 |
| `mixed` | Distinct two-line baskets, quantities 1–3 | All 500 customers succeed; 1,000 invoice lines |

The dedicated hot variant has no seeded historical sales. Resetting it between
runs represents a new test restock; previous test invoices are kept. `mixed`
does not reset stock and avoids the hot variant. Repeated mixed runs consume
inventory and can eventually run out; inspect stock before a long repeated run.

After a concurrency exception, sufficient fresh stock lets the API retry the
same basket automatically. A shortage on the first attempt still returns `400`;
a shortage after a conflict returns `409` with `code: Checkout.StockChanged`.
Exhausting five conflicting attempts returns `409` with `code: Checkout.Concurrency`;
stock may still remain in this case, so a hot-SKU burst need not sell all 100 pairs.
The report's `conflicts` counts only exhausted retries. `outOfStock` combines
initial shortages (400) and shortages discovered on retry (409); `stockChanged`
counts the latter subset. None of these rejections is a successful order.
`401`, `404`, `429`, `500`, network timeouts
and unexpected response bodies fail the test. The broad 45-second p95 guard
detects requests reaching the client timeout; the measured p95 is reported,
not asserted to be an application performance SLA.

## Evidence and audit

Generated output is in `Backend/.artifacts/loadtest/`:

- `unit-tests.log`, `api-build.log`, `api.log`: validation and server logs.
- A separate directory per scenario contains `baseline.json`, `summary.json`
  (k6 metrics), `audit.json` (database verification), and JWT/variant fixtures.

The runner exits nonzero if k6 or the database audit fails. It checks:

1. Exactly the requested number of checkout attempts, with no unexpected errors.
2. Committed invoice count equals successful HTTP responses.
3. For every seeded variant, `stock_before = stock_after + units_ordered_in_this_run`.
4. Each customer's saved variant IDs and quantities match the exact submitted basket;
   no customer receives duplicate committed orders in the single-iteration burst.
5. No negative stock, no empty invoices, correct invoice totals and line counts.
6. Last-pair exactly one winner; hot-SKU at most 100 winners; mixed all baskets succeed.

The stock invariant detects lost updates even when final stock is nonnegative.
Run directories isolate previous test orders using a unique `fullName` marker;
historical data is retained. Do not manually modify inventory or run another
checkout workload during an audit window.

This load test checks the real PostgreSQL `xmin` concurrency behaviour. The
`loadtest.js` shopping journey below complements these checkout-only scenarios.
Production/network latency, Android rendering,
payments, voucher races and guaranteed simultaneous reads are outside this
test's scope. Repeated concurrent bursts increase overlap but cannot force
every request to read the same row version.

Unit tests cover retry success with fresh stock, insufficient fresh stock,
five-attempt exhaustion, voucher revalidation, cancellation, and avoiding retry
on other database errors. A cart checkout rejects items consumed by another
checkout before the retry, instead of ordering them twice.
An EF/SQLite persistence test injects one concurrency
exception and checks that retry saves exactly one invoice, one voucher reservation,
one stock deduction and the cart removal. Real PostgreSQL conflicts are exercised
by the k6 scenarios above.

Order codes use the existing prefix/date plus sixteen random digits to reduce
collisions across accumulated orders and retry attempts. The database unique
index remains enabled; a duplicate-key error is an unexpected test failure,
not a stock rejection. See [RESULTS.md](RESULTS.md) for before/after measurements.

## Shopping journey: 500-user burst

```bash
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh journey 500
```

This replaces the old `loadtest.js` that reused eleven accounts and hard-coded
Azure-era product IDs. It generates 500 independent JWTs and 500 distinct
in-stock variants from the current local catalogue, without resetting stock.
The selection avoids the hot race-test variant. Each VU starts at the shared
timestamp and completes this sequence exactly once:

1. Search for its shoe by product name, using the supported default sort.
2. Open that product's details and verify the selected variant and stock.
3. Prepare checkout and verify the requested variant/quantity is available.
4. Place one COD order and receive an order code.

There is one second of think time after search and after product details.
The burst synchronizes the start of the journey; checkout follows preceding
responses and is not forced to happen at one identical timestamp.
All four steps must return 200 with the expected data; all 500 journeys and
orders must complete. The retained latency target is **p95 below two seconds
per HTTP step**, excluding think time. k6 reports each step's p95 and the full
successful journey duration including think time.

The runner also checks 500 committed invoices, one correct line per invoice,
exact buyer/variant/quantity matches, and inventory conservation across all
7,500 variants. A latency-threshold failure makes the runner fail even when
the separate correctness audit passes.
`all 500` continues to run the three checkout race scenarios; run `journey 500`
for the shopping flow. Signin, saved-cart checkout, payments, and sustained
sessions remain outside this COD journey. API product caching is enabled;
each runner starts a new API process, so its cache starts cold apart from one
search-page preflight. JWT files remain local and excluded from git.
