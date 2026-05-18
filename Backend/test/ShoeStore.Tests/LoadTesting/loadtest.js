import http from 'k6/http';
import { sleep, check, group } from 'k6';


export const options = {
  stages: [
    { duration: '15s', target: 50 },  // Up to 50 users in 15 seconds (warm-up)
    { duration: '45s', target: 300 }, // Up to 300 users in 45 seconds (peak load)
    { duration: '15s', target: 0 },   // back-down: decrease to 0 users in 15 seconds (cool-down)
  ],
};

export function setup() {
  // Any setup code can go here, such as preparing test data or authentication tokens
  const loginUrl = 'http://localhost:8080/api/auth/signin';
  const password = __ENV.TEST_PASSWORD;
  if (!password) {
    throw new Error('TEST_PASSWORD environment variable is not set');
  }
  const tokenPool = [];
  for (let i = 1; i <= 11; i++) {
    const payload = JSON.stringify({
      email: `test${i}@gmail.com`,
      password: password
    });
    const res = http.post(loginUrl, payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (res.status === 200) {
      const token = res.json().token;
      if (token) tokenPool.push(token);
      else {
        console.error(`Login failed for test${i}@gmail.com`);
      }
    }
  }
  if (tokenPool.length === 0) {
    throw new Error('No valid tokens obtained from login');
  }
  console.log(`Obtained ${tokenPool.length} tokens for load testing`);
  var productPool = SetUpProductData();
  var variantPool = SetUpVariantData();
  var data = {
    productPool: productPool,
    variantPool: variantPool,
    tokenPool: tokenPool
  }
  return data;
}

export default function (data) {
  const tokenPool = data.tokenPool;
  const productPool = data.productPool;
  const variantPool = data.variantPool;
  const tokenIndex = (__VU - 1) % tokenPool.length; // Rotate through tokens based on VU number
  const currentProduct = randomElement(productPool); // Select a random product
  const currentVariant = randomElement(variantPool); // Select a random variant
  const currentToken = tokenPool[tokenIndex];
  const url = 'http://localhost:8080/api/Products/search';
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${currentToken}`,
    },
  };

  group('Load Testing - Search Products', function () {
    const url = 'http://localhost:8080/api/Products/search';
    const resSearch = http.get(url, params);
    check(resSearch, {
      'Search Products - Status 200': (r) => r.status === 200,
      'Search Products - Response < 2 seconds': (r) => r.timings.duration < 2000,
    });
  });

  sleep(1);

  group('Load Testing - Get Product Details', function () {
    const productGuid = currentProduct;
    const url = `http://localhost:8080/api/Products/${productGuid}`;
    const resDetails = http.get(url, params);
    check(resDetails, {
      'Get Product Details - Status 200': (r) => r.status === 200,
      'Get Product Details - Response < 2 seconds': (r) => r.timings.duration < 2000,
    });
  });

  sleep(1);

  group('Load Testing - Create invoice', function () {
    const url = 'http://localhost:8080/api/checkout/place-order';
    const payload = JSON.stringify({
      items: [
        {
          variantId: currentVariant,
          quantity: 1
        },
      ],
      voucherIds: [],
      fullName: 'Ha Tran Quang',
      address: 'Dai hoc Cong nghe thong tin, TP.HCM',
      paymentId: '2',
      phoneNumber: '0329384723',
    });
    const resCreateInvoice = http.post(url, payload, params);
    if (resCreateInvoice.status !== 200 && resCreateInvoice.status !== 201) {
      console.error(`[LỖI GHI FILE - Mã ${resCreateInvoice.status}] Từ VU ${__VU}: ${resCreateInvoice.body}`);
    }
    check(resCreateInvoice, {
      'Create Invoice - Status 200': (r) => r.status === 200,
      'Create Invoice - Response < 2 seconds': (r) => r.timings.duration < 2000,
    });
  });

  sleep(1);
}

function SetUpProductData() {
  return [
    '1d53d076-9c8d-425a-8abe-fb6442d820a7',
    'f69728c7-6ea4-4af1-a59a-4f9234e67ece',
    '18c406e1-57bf-4845-8b34-dc208ab01879',
    '9aa0fb12-dd2b-4a59-8f28-01e3a6f96e8f',
    '0b78f493-2f6a-4e98-ba3f-815cfa2faa88',
    '3fec347c-ed2e-425e-84a0-7e99ed6a785a',
    'fca4ddd6-7c13-45b6-a116-4241936f3fa3',
    '84d00439-7d7c-4536-a1f5-00ffecee25b9'
  ];
}

function SetUpVariantData() {
  return [
    '06ef03b1-91aa-4196-878f-c842454a8d5b',
    '548201f3-f241-489d-afe8-6d42fb5144d3',
    '97dc7029-cad9-4ee3-bb5e-4ab7e2acb328',
    '65fbf7c7-501d-4d34-84d4-e1795ef386f8',
    'a8c121e8-517c-4b43-8a58-1d142572b1f7',
    '12bc8893-125e-41da-8cbd-a6d383ce4831',
    '1568fe31-0872-4b30-b4a2-5ee820017dab',
    '205c7383-da7e-462e-8ec0-2154d6418b5e',
    'f5cbe2c7-7b24-4886-9f7e-dc84a899c417',
    '0ea5b4b5-727a-49e6-8fc9-fd2e988a09ab',
    'd58ff4f0-fffa-481b-b510-a3fb8074e340',
    'cf6284ce-29c4-43ab-a5a3-95d5b374b65f',
    'b5ed6ee7-88f8-4f8d-a79e-ef63ea1618e3'
  ]
}

function randomElement(array) {
  return array[Math.floor(Math.random() * array.length)];
}