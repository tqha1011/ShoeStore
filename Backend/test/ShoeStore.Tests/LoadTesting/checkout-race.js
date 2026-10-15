import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter } from 'k6/metrics';

const dir = __ENV.FIXTURE_DIR;
if (!dir) throw new Error('Run run-local.sh to generate FIXTURE_DIR first');
const config = JSON.parse(open(`${dir}/config.json`));
const users = new SharedArray('checkout-users', () => JSON.parse(open(`${dir}/users.json`)));
const variants = new SharedArray('checkout-variants', () => JSON.parse(open(`${dir}/variants.json`)));
const baskets = new SharedArray('checkout-baskets', () => JSON.parse(open(`${dir}/baskets.json`)));
const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';
if (!/^http:\/\/(localhost|127\.0\.0\.1|\[::1\])(:\d+)?$/.test(baseUrl)) {
  throw new Error('This fixture targets a local ShoeStore API only');
}
if (users.length !== config.users) throw new Error('User fixture count mismatch');
if (baskets.length !== config.users) throw new Error('Basket fixture count mismatch');

const attempted = new Counter('orders_attempted');
const succeeded = new Counter('orders_succeeded');
const conflicts = new Counter('orders_conflict');
const outOfStock = new Counter('orders_out_of_stock');
const stockChanged = new Counter('orders_stock_changed');
const unexpected = new Counter('orders_unexpected');

const successThresholds = config.mode === 'last-pair' ? ['count==1']
  : config.mode === 'hot-sku' ? ['count>0', 'count<=100'] : [`count==${config.users}`];

export const options = {
  scenarios: {
    checkout_burst: {
      executor: 'per-vu-iterations',
      vus: config.users,
      iterations: 1,
      maxDuration: '60s',
    },
  },
  thresholds: {
    orders_attempted: [`count==${config.users}`],
    orders_succeeded: successThresholds,
    orders_unexpected: ['count==0'],
    checks: ['rate==1'],
    'http_req_duration{phase:checkout}': ['p(95)<45000'],
  },
};

export function setup() {
  const user = users[0];
  const response = http.post(`${baseUrl}/api/checkout/prepare`, JSON.stringify({
    checkOutList: [{ variantId: variants[1], quantity: 1 }], voucherIds: [],
  }), { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${user.token}` },
    tags: { phase: 'preflight' } });
  if (response.status !== 200) throw new Error(`Checkout preflight failed: HTTP ${response.status}`);
  // Every VU waits for this shared timestamp before firing its one checkout.
  return { startAt: Date.now() + 5000 };
}

export default function (data) {
  const wait = (data.startAt - Date.now()) / 1000;
  if (wait > 0) sleep(wait);
  const index = __VU - 1;
  // Use the exact submitted baskets that the database audit will compare.
  const items = baskets[index].items;
  attempted.add(1);
  const response = http.post(`${baseUrl}/api/checkout/place-order?fromUserCart=false`, JSON.stringify({
    items, voucherIds: [], fullName: config.runId,
    address: 'Thu Duc, Ho Chi Minh City', paymentId: 2, phoneNumber: '0901234567',
  }), {
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${users[index].token}` },
    timeout: '45s', tags: { phase: 'checkout', mode: config.mode },
    responseCallback: http.expectedStatuses(200, 400, 409),
  });
  let body = {};
  try { body = response.json(); } catch (_) { /* Classified as unexpected below. */ }
  const ok = response.status === 200 && !!body.orderCode;
  const conflict = response.status === 409 && body.code === 'Checkout.Concurrency';
  const shortage = (response.status === 400 && body.message === "Shop's stock not enough. Please try again later.")
    || (response.status === 409 && body.code === 'Checkout.StockChanged');
  const valid = ok || (config.mode !== 'mixed' && (conflict || shortage));
  succeeded.add(ok ? 1 : 0);
  conflicts.add(conflict ? 1 : 0);
  outOfStock.add(shortage ? 1 : 0);
  stockChanged.add(response.status === 409 && body.code === 'Checkout.StockChanged' ? 1 : 0);
  unexpected.add(valid ? 0 : 1);
  check(response, { 'checkout result matches scenario': () => valid });
  if (!valid) console.error(`VU ${__VU}: unexpected HTTP ${response.status}, ${JSON.stringify(body).slice(0, 400)}`);
}

export function handleSummary(data) {
  return {
    [`${dir}/summary.json`]: JSON.stringify(data, null, 2),
    stdout: JSON.stringify({
      mode: config.mode,
      attempted: data.metrics.orders_attempted?.values.count || 0,
      succeeded: data.metrics.orders_succeeded?.values.count || 0,
      conflicts: data.metrics.orders_conflict?.values.count || 0,
      outOfStock: data.metrics.orders_out_of_stock?.values.count || 0,
      stockChanged: data.metrics.orders_stock_changed?.values.count || 0,
      unexpected: data.metrics.orders_unexpected?.values.count || 0,
      checkoutP95Ms: data.metrics['http_req_duration{phase:checkout}']?.values['p(95)'],
    }, null, 2) + '\n',
  };
}
