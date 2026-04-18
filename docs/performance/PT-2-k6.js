import http from "k6/http";
import { check, fail, group, sleep } from "k6";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const AUTH_HEADER = __ENV.AUTH_HEADER
  || (__ENV.AUTH_TOKEN ? `Bearer ${__ENV.AUTH_TOKEN}` : "");
const ACCEPT_LANGUAGE = __ENV.ACCEPT_LANGUAGE || "zh-CN";
const TEST_DURATION = __ENV.DURATION || "3m";
const THINK_TIME_MS = Number(__ENV.THINK_TIME_MS || "200");
const ORDER_PAGE_SIZE = Number(__ENV.ORDER_PAGE_SIZE || "20");
const ORDER_SAMPLE_SIZE = Number(__ENV.ORDER_SAMPLE_SIZE || "20");
const ENABLE_POINT_PATCH = String(__ENV.ENABLE_POINT_PATCH || "false").toLowerCase() === "true";
const POINT_PATCH_PERCENT = Number(__ENV.POINT_PATCH_PERCENT || "5");
const PATCH_POINT_VALUE = Number(__ENV.PATCH_POINT_VALUE || "300");
const PATCH_POINT_TYPE = __ENV.PATCH_POINT_TYPE || "付费钻石";
const PATCH_ALLOWED_STATUSES = (__ENV.PATCH_ALLOWED_STATUSES || "")
  .split(",")
  .map((item) => item.trim())
  .filter(Boolean);

const ORDER_LIST_CASES = [
  "page=0&size=20&sort=id,desc",
  "page=0&size=20&sort=shipTimeMs,desc",
  "search=internalStatus:ENTERED&page=0&size=20&sort=id,desc",
  "search=internalStatus:CALCULATED&page=0&size=20&sort=id,desc",
  "search=refundStatus:NONE&page=0&size=20&sort=id,desc",
  "search=productName~:%E6%A8%A1%E6%9D%BF&page=0&size=20&sort=id,desc",
];

export const options = {
  scenarios: {
    pt2_order_query_and_settlement_flow: {
      executor: "constant-vus",
      vus: 100,
      duration: TEST_DURATION,
      gracefulStop: "10s",
    },
  },
  thresholds: {
    http_req_failed: ["rate==0"],
    http_req_duration: ["avg<3000", "p(95)<5000"],

    "http_req_duration{endpoint:order_list}": ["avg<3000", "p(95)<5000"],
    "http_req_duration{endpoint:order_detail}": ["avg<3000", "p(95)<5000"],
    "http_req_duration{endpoint:settlement_breakdown}": ["avg<3000", "p(95)<5000"],
    "http_req_duration{endpoint:order_checkout_details}": ["avg<3000", "p(95)<5000"],
    "http_req_duration{endpoint:order_point_patch}": ["avg<3000", "p(95)<5000"],

    "checks{endpoint:order_list}": ["rate>0.99"],
    "checks{endpoint:order_detail}": ["rate>0.99"],
    "checks{endpoint:settlement_breakdown}": ["rate>0.99"],
    "checks{endpoint:order_checkout_details}": ["rate>0.99"],
    "checks{endpoint:order_point_patch}": ["rate>0.99"],
  },
  summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "max"],
};

function authHeaders(extraHeaders = {}) {
  const headers = {
    Accept: "application/json",
    "Accept-Language": ACCEPT_LANGUAGE,
    ...extraHeaders,
  };
  if (AUTH_HEADER) {
    headers.Authorization = AUTH_HEADER;
  }
  if (__ENV.COOKIE) {
    headers.Cookie = __ENV.COOKIE;
  }
  return headers;
}

function assertEnv() {
  if (!AUTH_HEADER && !__ENV.COOKIE) {
    fail("Missing auth credentials. Provide AUTH_TOKEN, AUTH_HEADER, or COOKIE.");
  }
}

function randomOf(list) {
  return list[Math.floor(Math.random() * list.length)];
}

function pickOrderId(orderIds) {
  if (!orderIds || orderIds.length === 0) {
    fail("No order ids available for PT-2. Seed at least one order first.");
  }
  return randomOf(orderIds);
}

function getJson(url, endpointTag) {
  const response = http.get(url, {
    headers: authHeaders(),
    tags: { endpoint: endpointTag },
  });

  const ok = check(response, {
    "status is 200": (r) => r.status === 200,
    "response body is json-like": (r) =>
      (r.headers["Content-Type"] || "").includes("application/json"),
    "payload is not empty": (r) => !!r.body && r.body.length > 2,
  }, { endpoint: endpointTag });

  if (!ok) {
    console.error(`[${endpointTag}] unexpected response: status=${response.status} body=${response.body?.slice(0, 400)}`);
  }

  try {
    return response.json();
  } catch (error) {
    console.error(`[${endpointTag}] failed to parse json: ${error}`);
    return null;
  }
}

function patchJson(url, body, endpointTag) {
  const response = http.patch(url, JSON.stringify(body), {
    headers: authHeaders({ "Content-Type": "application/json" }),
    tags: { endpoint: endpointTag },
  });

  const ok = check(response, {
    "status is 200 or 204": (r) => r.status === 200 || r.status === 204,
  }, { endpoint: endpointTag });

  if (!ok) {
    console.error(`[${endpointTag}] unexpected response: status=${response.status} body=${response.body?.slice(0, 400)}`);
  }

  return response;
}

function parseOrderIds(payload) {
  const content = payload?.content;
  if (!Array.isArray(content)) {
    return [];
  }
  return content
    .map((row) => row?.id)
    .filter((id) => Number.isFinite(Number(id)))
    .map((id) => Number(id));
}

function parsePatchCandidates(payload) {
  const content = Array.isArray(payload?.content) ? payload.content : [];
  return content
    .filter((row) => Number.isFinite(Number(row?.id)))
    .filter((row) => PATCH_ALLOWED_STATUSES.length === 0 || PATCH_ALLOWED_STATUSES.includes(String(row?.internalStatus || "")))
    .map((row) => Number(row.id));
}

function loadSeedOrders() {
  const seededIds = (__ENV.TEST_ORDER_IDS || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => Number(item))
    .filter((item) => Number.isFinite(item));

  if (seededIds.length > 0) {
    return { orderIds: seededIds, patchCandidates: seededIds };
  }

  const listPayload = getJson(
    `${BASE_URL}/api/v1/commercial/netease-orders?page=0&size=${ORDER_SAMPLE_SIZE}&sort=id,desc`,
    "order_list",
  );
  const orderIds = parseOrderIds(listPayload);
  const patchCandidates = parsePatchCandidates(listPayload);
  return { orderIds, patchCandidates };
}

export function setup() {
  assertEnv();

  const warmup = http.get(`${BASE_URL}/api/v1/commercial/netease-orders?page=0&size=1`, {
    headers: authHeaders(),
    tags: { endpoint: "warmup" },
  });
  if (warmup.status === 401 || warmup.status === 403 || warmup.status === 406) {
    fail(`Auth warmup failed with status ${warmup.status}. Check token/cookie permissions.`);
  }

  const seed = loadSeedOrders();
  if (seed.orderIds.length === 0) {
    fail("Warmup succeeded but no orders were returned. Please seed at least one order or pass TEST_ORDER_IDS.");
  }

  return {
    baseUrl: BASE_URL,
    orderIds: seed.orderIds,
    patchCandidates: seed.patchCandidates,
  };
}

export default function (data) {
  const baseUrl = data.baseUrl;
  const orderId = pickOrderId(data.orderIds);

  group("order_list", () => {
    const query = randomOf(ORDER_LIST_CASES).replace("size=20", `size=${ORDER_PAGE_SIZE}`);
    getJson(`${baseUrl}/api/v1/commercial/netease-orders?${query}`, "order_list");
  });

  group("order_detail", () => {
    getJson(`${baseUrl}/api/v1/commercial/netease-orders/${orderId}`, "order_detail");
  });

  group("settlement_breakdown", () => {
    getJson(`${baseUrl}/api/v1/commercial/netease-orders/${orderId}/settlement-breakdown`, "settlement_breakdown");
  });

  group("order_checkout_details", () => {
    const search = encodeURIComponent(`order.id:${orderId}`);
    getJson(
      `${baseUrl}/api/v1/commercial/balance/checkout-details?search=${search}&page=0&size=20&sort=id,desc`,
      "order_checkout_details",
    );
  });

  if (
    ENABLE_POINT_PATCH
    && data.patchCandidates.length > 0
    && Math.random() * 100 < POINT_PATCH_PERCENT
  ) {
    group("order_point_patch", () => {
      const patchOrderId = pickOrderId(data.patchCandidates);
      patchJson(`${baseUrl}/api/v1/commercial/netease-orders/${patchOrderId}/point`, {
        point: PATCH_POINT_VALUE,
        pointType: PATCH_POINT_TYPE,
      }, "order_point_patch");
    });
  }

  if (THINK_TIME_MS > 0) {
    sleep(THINK_TIME_MS / 1000);
  }
}
