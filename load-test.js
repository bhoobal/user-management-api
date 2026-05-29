import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";

// Custom metrics
const createUserDuration = new Trend("create_user_duration");
const createGroupDuration = new Trend("create_group_duration");
const getUsersDuration = new Trend("get_users_duration");
const getGroupsDuration = new Trend("get_groups_duration");
const errorRate = new Rate("error_rate");
const createdUsers = new Counter("created_users");
const createdGroups = new Counter("created_groups");

const BASE_URL = __ENV.BASE_URL || "http://localhost:8081";
const HEADERS = { "Content-Type": "application/json" };

export const options = {
  scenarios: {
    // Ramp up, hold, ramp down
    load_test: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 10 }, // ramp up to 10 users
        { duration: "1m", target: 10 },  // hold at 10 users
        { duration: "30s", target: 30 }, // spike to 30 users
        { duration: "1m", target: 30 },  // hold at 30 users
        { duration: "30s", target: 0 },  // ramp down
      ],
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],        // <1% errors
    http_req_duration: ["p(95)<500"],      // 95% of requests under 500ms
    error_rate: ["rate<0.01"],
  },
};

function randomString(len) {
  const chars = "abcdefghijklmnopqrstuvwxyz";
  let result = "";
  for (let i = 0; i < len; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function log(method, url, status, ok) {
  const result = ok ? "PASS" : "FAIL";
  console.log(`[${result}] ${method} ${url} → ${status}`);
}

// POST /api/v1/groups
function createGroup() {
  const name = `group-${randomString(6)}`;
  const url = `${BASE_URL}/api/v1/groups`;
  const payload = JSON.stringify({ name, description: `Load test group ${name}` });
  const res = http.post(url, payload, { headers: HEADERS });

  createGroupDuration.add(res.timings.duration);
  const ok = check(res, {
    "create group 201": (r) => r.status === 201 || r.status === 200,
    "create group has id": (r) => {
      try { return JSON.parse(r.body).data.id !== undefined; } catch { return false; }
    },
  });
  errorRate.add(!ok);
  log("POST", url, res.status, ok);

  if (ok) {
    createdGroups.add(1);
    return JSON.parse(res.body).data.id;
  }
  return null;
}

// POST /api/v1/users
function createUser() {
  const tag = randomString(6);
  const url = `${BASE_URL}/api/v1/users`;
  const payload = JSON.stringify({
    firstName: `First${tag}`,
    lastName: `Last${tag}`,
    email: `${tag}@loadtest.com`,
    username: `user_${tag}`,
  });
  const res = http.post(url, payload, { headers: HEADERS });

  createUserDuration.add(res.timings.duration);
  const ok = check(res, {
    "create user 201": (r) => r.status === 201 || r.status === 200,
    "create user has id": (r) => {
      try { return JSON.parse(r.body).data.id !== undefined; } catch { return false; }
    },
  });
  errorRate.add(!ok);
  log("POST", url, res.status, ok);

  if (ok) {
    createdUsers.add(1);
    return JSON.parse(res.body).data.id;
  }
  return null;
}

// GET /api/v1/users
function getAllUsers() {
  const url = `${BASE_URL}/api/v1/users`;
  const res = http.get(url);
  getUsersDuration.add(res.timings.duration);
  const ok = check(res, { "get users 200": (r) => r.status === 200 });
  errorRate.add(!ok);
  log("GET", url, res.status, ok);
}

// GET /api/v1/users/:id
function getUserById(id) {
  const url = `${BASE_URL}/api/v1/users/${id}`;
  const res = http.get(url);
  const ok = check(res, { "get user by id 200": (r) => r.status === 200 });
  errorRate.add(!ok);
  log("GET", url, res.status, ok);
}

// GET /api/v1/groups
function getAllGroups() {
  const url = `${BASE_URL}/api/v1/groups`;
  const res = http.get(url);
  getGroupsDuration.add(res.timings.duration);
  const ok = check(res, { "get groups 200": (r) => r.status === 200 });
  errorRate.add(!ok);
  log("GET", url, res.status, ok);
}

// GET /api/v1/groups/:id
function getGroupById(id) {
  const url = `${BASE_URL}/api/v1/groups/${id}`;
  const res = http.get(url);
  const ok = check(res, { "get group by id 200": (r) => r.status === 200 });
  errorRate.add(!ok);
  log("GET", url, res.status, ok);
}

// POST /api/v1/users/:userId/groups/:groupId
function addUserToGroup(userId, groupId) {
  const url = `${BASE_URL}/api/v1/users/${userId}/groups/${groupId}`;
  const res = http.post(url, null, { headers: HEADERS });
  const ok = check(res, { "add to group 200": (r) => r.status === 200 });
  errorRate.add(!ok);
  log("POST", url, res.status, ok);
}

export default function () {
  // Create a group and a user, then link them
  const groupId = createGroup();
  sleep(0.1);

  const userId = createUser();
  sleep(0.1);

  if (userId && groupId) {
    addUserToGroup(userId, groupId);
    sleep(0.1);
    getGroupById(groupId);
    sleep(0.1);
    getUserById(userId);
    sleep(0.1);
  }

  // Read-heavy operations
  getAllUsers();
  sleep(0.1);
  getAllGroups();
  sleep(0.2);
}
