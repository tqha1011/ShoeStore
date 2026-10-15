# Observed 500-user checkout results

Date: 2026-10-10. Command, from repository root:

```bash
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh all 500
```

Environment: API built in Release with .NET 10, PostgreSQL + pgvector 15 in
Docker, API and k6 on the same local machine. PostgreSQL reports
`max_connections = 100`; the API pool is capped at 80. Every mode uses
500 independently authenticated VUs, one checkout each, with a shared start
timestamp. Checkout rate limiting remains enabled. Tokens are generated
before the burst with the app's token service; signin is not load-tested.

Seed verified: 1,000 customers plus the existing admin, 500 products,
7,500 variants, 50,000 historical invoices, 100,000 historical invoice lines.
Re-running seed preserved these counts and did not duplicate data.
The results below were recorded before adding API concurrency retries.
All 139 unit tests passed, including seven new inventory/basket-audit tests.

| Scenario | Attempts | HTTP 200 | HTTP 409 | Stock rejection 400 | Unexpected | Checkout p95 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Last pair, stock = 1 | 500 | 1 | 79 | 420 | 0 | 1,004 ms |
| Hot SKU, stock = 100 | 500 | 18 | 482 | 0 | 0 | 562 ms |
| Different two-line baskets | 500 | 500 | 0 | 0 | 0 | 294 ms |

Database audit passed for all three scenarios:

- Committed invoices matched successful responses: 1, 18, and 500.
- Invoice line counts were 1, 18, and 1,000.
- Ordered quantities were 1, 18, and 1,499.
- Every saved customer's variant IDs and quantities matched their submitted basket.
- Stock matched `before = after + ordered quantity` for all 7,500 variants.
- No stock mismatch, negative stock, invalid/empty invoice, or unexpected response.
- All k6 thresholds passed; all 500 iterations completed in every scenario.

The hot-SKU run retained 82 pairs after 18 successful orders. The 482 conflicts
are expected with optimistic concurrency and no retry: protecting inventory
does not guarantee that every request succeeds while stock is still available.
These measurements serve as the baseline for the bounded-retry comparison below.

An initial run with the default 100-connection API pool completed its 500
checkouts but blocked the audit's connection with PostgreSQL error 53300.
The runner now caps its local pool at 80; the complete rerun above passed both
HTTP and database verification. That baseline run did not change production checkout code.

Measured durations describe these local bursts, including their warm-up state.
They do not establish production capacity, sustained 500-user session support,
or a latency SLA. COD checkout without vouchers is covered; payment webhooks,
cart removal and voucher races are separate cases.

Raw per-scenario `summary.json` and `audit.json` are retained locally under
`Backend/.artifacts/loadtest/20261010T134953Z-last-pair-20813/`,
`20261010T135001Z-hot-sku-12308/`, and `20261010T135008Z-mixed-20920/`.
Generated JWT fixtures stay local and are excluded from git.

## After bounded API retry

The final rerun uses up to five transaction attempts per checkout request.
Only `DbUpdateConcurrencyException` triggers retry: rollback, clear EF tracking,
wait with exponential jitter, and read fresh stock/user/cart/vouchers. Shortage
after a conflict returns `409 / Checkout.StockChanged`; exhausting retries
returns `409 / Checkout.Concurrency`. An initial shortage remains `400`.
Cart checkout also requires every requested item still present on retry.

All **150 tests passed**, including fresh-stock retry, shortage, exhaustion,
voucher/cart revalidation, cancellation, other DB failures, EF persistence,
and concurrent order-code generation.

| Scenario | Attempts | HTTP 200 | Initial shortage 400 | Changed stock 409 | Exhausted retries 409 | Unexpected | Checkout p95 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Last pair, stock = 1 | 500 | 1 | 420 | 79 | 0 | 0 | 1,034 ms |
| Hot SKU, stock = 100 | 500 | 100 | 0 | 372 | 28 | 0 | 2,024 ms |
| Different two-line baskets | 500 | 500 | 0 | 0 | 0 | 0 | 306 ms |

All three k6 runs and database audits passed. Committed invoice/line counts
were 1/1, 100/100, and 500/1,000. Ordered quantities were 1, 100, and 1,499.
Every basket matched, and all 7,500 inventory rows satisfied the stock invariant,
with no negative stock or duplicate customer orders within a run.
The hot variant ended at zero stock, compared with 82 remaining in the baseline.
The improvement costs extra work and latency: local hot-SKU p95 rose from
562 ms to 2,024 ms. These are observed bursts, not production guarantees;
bounded retries can still exhaust while stock remains.

A preceding rerun correctly **failed** on one HTTP 500 caused by PostgreSQL
`23505 / ix_invoices_order_code`. The existing four-character random suffix
collided with accumulated daily orders; retry generates more candidate codes.
The generator now uses sixteen cryptographically random digits after the
existing prefix/date, compatible with the payment content parser's `DH\d+`.
The database unique index still enforces uniqueness. A regression test exposed
116 duplicate codes in a 20,000-code parallel batch before the fix; the updated
generator passed that test and the final native checkout run.
The failed audit is retained at
`Backend/.artifacts/loadtest/20261010T142454Z-hot-sku-32423/audit.json`.

Final per-scenario `summary.json` and `audit.json` are retained locally at:

- `Backend/.artifacts/loadtest/20261010T142737Z-last-pair-19643/`
- `Backend/.artifacts/loadtest/20261010T142746Z-hot-sku-10476/`
- `Backend/.artifacts/loadtest/20261010T142755Z-mixed-15695/`

## Shopping journey burst: 500 VUs

Command:

```bash
bash Backend/test/ShoeStore.Tests/LoadTesting/run-local.sh journey 500
```

The updated `loadtest.js` uses 500 independent seeded customers and distinct
in-stock variants. Each customer searches for a shoe, opens its details,
prepares checkout, then places one COD order. The journey starts together;
one second of think time follows search and details. The API starts fresh,
with in-process product caching enabled and one search preflight before the burst.
Signin, cart checkout, payment webhooks and sustained sessions are not measured.

All **153 tests passed**. All **500/500 journeys completed**, each making four
successful HTTP requests: 2,000 workload requests, plus one setup preflight.
The two-second p95 threshold passed for every HTTP step:

| Step | p95 |
| --- | ---: |
| Search products | 641 ms |
| Product details | 87 ms |
| Prepare checkout | 1,164 ms |
| Place COD order | 1,007 ms |
| Complete successful journey, including 2 seconds think time | 4,165 ms |

k6 and the database audit both passed. The database contained exactly 500
new invoices, 500 invoice lines, and 500 ordered units for the run. Every
customer's saved basket matched the submitted variant/quantity, with no
inventory mismatch, negative stock, duplicate order, or unexpected response.
Journey preparation did not reset stock or delete previous orders.

Raw reports: `Backend/.artifacts/loadtest/20261010T144201Z-journey-8442/summary.json`
and `audit.json`. This is evidence for this local 500-user burst; it does not
establish sustained production capacity.
