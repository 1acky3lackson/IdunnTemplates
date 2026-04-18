import http from "k6/http";
import { check, fail, group, sleep } from "k6";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const AUTH_HEADER = __ENV.AUTH_HEADER
  || (__ENV.AUTH_TOKEN ? `Bearer ${__ENV.AUTH_TOKEN}` : "");
const ACCEPT_LANGUAGE = __ENV.ACCEPT_LANGUAGE || "zh-CN";
const TEST_DURATION = __ENV.DURATION || "3m";
const THINK_TIME_MS = Number(__ENV.THINK_TIME_MS || "200");

const TEMPLATE_SEARCH_CASES = [
  "pathPrefix=users/&page=0&size=20&sort=metadata.creationTime,desc",
  "pathPrefix=public/&locked=false&page=0&size=20&sort=metadata.creationTime,desc",
  "page=0&size=20&sort=path,asc",
  "minWidth=1&maxWidth=256&page=1&size=20&sort=name,asc",
];

const CHECKOUT_SEARCH_CASES = [
  "status:CREATED",
  "status:CONFIRMED",
  "role:BUILDER",
  "role:MODIFIER",
  "username~:a",
];

const BALANCE_RECORD_SEARCH_CASES = [
  "type:INCOME",
  "type:EXPENSE",
  "description~:订单",
  "username~:a",
];

export const options = {
  scenarios: {
    pt1_web_console_browsing: {
      executor: "constant-vus",
      vus: 100,
      duration: TEST_DURATION,
      gracefulStop: "10s",
    },
  },
  thresholds: {
    http_req_failed: ["rate==0"],
    http_req_duration: ["avg<3000", "p(95)<5000"],

    "http_req_duration{endpoint:templates_search}": ["avg<3000", "p(95)<5000"],
    "http_req_duration{endpoint:checkout_details}": ["avg<3000", "p(95)<5000"],
    "http_req_duration{endpoint:balance_records}": ["avg<3000", "p(95)<5000"],

    "checks{endpoint:templates_search}": ["rate>0.99"],
    "checks{endpoint:checkout_details}": ["rate>0.99"],
    "checks{endpoint:balance_records}": ["rate>0.99"],
  },
  summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "max"],
};

function authHeaders() {
  const headers = {
    Accept: "application/json",
    "Accept-Language": ACCEPT_LANGUAGE,
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

  return response;
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
  return { baseUrl: BASE_URL };
}

export default function (data) {
  const baseUrl = data.baseUrl;

  group("templates_search", () => {
    const query = randomOf(TEMPLATE_SEARCH_CASES);
    getJson(`${baseUrl}/api/v1/templates?${query}`, "templates_search");
  });

  group("checkout_details", () => {
    const search = encodeURIComponent(randomOf(CHECKOUT_SEARCH_CASES));
    getJson(
      `${baseUrl}/api/v1/commercial/balance/checkout-details?search=${search}&page=0&size=20&sort=id,desc`,
      "checkout_details",
    );
  });

  group("balance_records", () => {
    const search = encodeURIComponent(randomOf(BALANCE_RECORD_SEARCH_CASES));
    getJson(
      `${baseUrl}/api/v1/commercial/balance/records?search=${search}&page=0&size=20&sort=createTimeMs,desc`,
      "balance_records",
    );
  });

  if (THINK_TIME_MS > 0) {
    sleep(THINK_TIME_MS / 1000);
  }
}
