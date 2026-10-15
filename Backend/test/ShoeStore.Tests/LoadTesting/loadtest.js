import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter, Trend } from 'k6/metrics';

const dir = __ENV.FIXTURE_DIR;
if (!dir) throw new Error('Run run-local.sh journey 500 to generate the local fixture');
const config = JSON.parse(open(`${dir}/config.json`));
const users = new SharedArray('journey-users', () => JSON.parse(open(`${dir}/users.json`)));
const products = new SharedArray('journey-products', () => JSON.parse(open(`${dir}/journey.json`)));
const baskets = new SharedArray('journey-baskets', () => JSON.parse(open(`${dir}/baskets.json`)));
const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';
if (!/^http:\/\/(localhost|127\.0\.0\.1|\[::1\])(:\d+)?$/.test(baseUrl))
  throw new Error('This fixture targets a local ShoeStore API only');
if (config.mode !== 'journey' || users.length !== config.users || products.length !== config.users
  || baskets.length !== config.users) throw new Error('Journey fixture count/mode mismatch');

const started = new Counter('journeys_started');
const completed = new Counter('journeys_completed');
const duration = new Trend('journey_duration', true);
const attempted = new Counter('orders_attempted');
const succeeded = new Counter('orders_succeeded');
const unexpected = new Counter('orders_unexpected');
const phases = ['search', 'detail', 'prepare', 'checkout'];
const thresholds = {
  journeys_started: [`count==${config.users}`],
  journeys_completed: [`count==${config.users}`],
  orders_attempted: [`count==${config.users}`],
  orders_succeeded: [`count==${config.users}`],
  orders_unexpected: ['count==0'],
  checks: ['rate==1'],
  'http_req_failed{workload:journey}': ['rate==0'],
};
// Retain the old benchmark's 2-second latency target, applied as p95 per step.
for (const phase of phases) thresholds[`http_req_duration{phase:${phase}}`] = ['p(95)<2000'];
export const options = {
  scenarios: {
    shopping_burst: { executor: 'per-vu-iterations', vus: config.users, iterations: 1, maxDuration: '60s' },
  },
  thresholds,
};

function params(token, phase) {
  return {
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    timeout: '45s', tags: { workload: 'journey', phase },
    responseCallback: http.expectedStatuses(200),
  };
}

function searchUrl(product) {
  return `${baseUrl}/api/Products/search?keyword=${encodeURIComponent(product.productName)}`
    + '&sort=default&pageIndex=1&pageSize=50';
}

function json(response) {
  try { return response.json(); } catch (_) { return {}; }
}

function validate(response, phase, validBody) {
  const ok = response.status === 200 && validBody;
  check(response, { [`${phase}: HTTP 200 and correct data`]: () => ok });
  if (!ok) {
    unexpected.add(1);
    console.error(`VU ${__VU} ${phase}: HTTP ${response.status}, ${response.body?.slice(0, 300)}`);
  }
  return ok;
}

export function setup() {
  const response = http.get(searchUrl(products[0]), params(users[0].token, 'preflight'));
  if (response.status !== 200 || !json(response).items?.some(p => p.publicId === products[0].productId))
    throw new Error(`Catalogue preflight failed: HTTP ${response.status}`);
  return { startAt: Date.now() + 5000 };
}

export default function (data) {
  const wait = (data.startAt - Date.now()) / 1000;
  if (wait > 0) sleep(wait);
  const start = Date.now();
  const index = __VU - 1;
  const product = products[index];
  const token = users[index].token;
  const items = baskets[index].items;
  started.add(1);
  unexpected.add(0);
  attempted.add(0);
  succeeded.add(0);
  completed.add(0);

  const found = group('Search products', () => {
    const response = http.get(searchUrl(product), params(token, 'search'));
    return validate(response, 'search', json(response).items?.some(p => p.publicId === product.productId));
  });
  if (!found) return;
  sleep(1);

  const viewed = group('View selected product', () => {
    const response = http.get(`${baseUrl}/api/Products/${product.productId}`, params(token, 'detail'));
    const body = json(response).data;
    return validate(response, 'detail', body?.publicId === product.productId
      && body.variants?.some(v => v.publicId === items[0].variantId && v.isSelling && v.stock >= items[0].quantity));
  });
  if (!viewed) return;
  sleep(1);

  const prepared = group('Prepare checkout', () => {
    const response = http.post(`${baseUrl}/api/checkout/prepare`, JSON.stringify({
      checkOutList: items, voucherIds: [],
    }), params(token, 'prepare'));
    const body = json(response);
    return validate(response, 'prepare', body.items?.length === items.length
      && body.items.every(item => items.some(expected => expected.variantId === item.variantId
        && expected.quantity === item.quantity) && !item.isOutOfStock));
  });
  if (!prepared) return;

  const bought = group('Place COD order', () => {
    attempted.add(1);
    const response = http.post(`${baseUrl}/api/checkout/place-order?fromUserCart=false`, JSON.stringify({
      items, voucherIds: [], fullName: config.runId,
      address: 'Thu Duc, Ho Chi Minh City', paymentId: 2, phoneNumber: '0901234567',
    }), params(token, 'checkout'));
    const ok = validate(response, 'checkout', !!json(response).orderCode);
    succeeded.add(ok ? 1 : 0);
    return ok;
  });
  if (bought) {
    completed.add(1);
    duration.add(Date.now() - start);
  }
}

export function handleSummary(data) {
  const count = name => data.metrics[name]?.values.count || 0;
  return {
    [`${dir}/summary.json`]: JSON.stringify(data, null, 2),
    stdout: JSON.stringify({
      mode: config.mode, users: config.users,
      started: count('journeys_started'), completed: count('journeys_completed'),
      attempted: count('orders_attempted'), succeeded: count('orders_succeeded'), unexpected: count('orders_unexpected'),
      stepP95Ms: Object.fromEntries(phases.map(phase => [phase,
        data.metrics[`http_req_duration{phase:${phase}}`]?.values['p(95)']])),
      journeyP95Ms: data.metrics.journey_duration?.values['p(95)'],
    }, null, 2) + '\n',
  };
}
