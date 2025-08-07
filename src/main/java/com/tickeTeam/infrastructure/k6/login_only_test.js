import http from "k6/http";
import { check, fail, sleep } from "k6";
import { SharedArray } from "k6/data";
import { Counter } from "k6/metrics";

function toQueryString(params) {
  return Object.entries(params)
    .map(([key, val]) => `${encodeURIComponent(key)}=${encodeURIComponent(val)}`)
    .join('&');
}


// --- 설정 부분 ---
export const options = {
  stages: [
//    { duration: "2m", target: 1500 },
//    { duration: "2m", target: 2500 },
//    { duration: "4m", target: 5000 },
//    { duration: "2m", target: 0 },
    { duration: "10s", target: 500 },  // 급격하게 3000 VU까지 올림
    { duration: "1m", target: 3000 },   // 1분간 피크 유지
    { duration: "1m", target: 3000 },   // 1분간 피크 유지
    { duration: "10s", target: 0 },     // 다시 점차 내림
  ],
  insecureSkipTLSVerify: true,
  thresholds: {
    http_req_duration: ["p(95)<1000"],
    http_req_failed: ["rate<0.01"], // 전체 HTTP 실패율은 1% 미만
    login_failures: ["count==0"],
    upcoming_view_failures: ["count==0"],
    seat_view_failures: ["count==0"],
    block_view_failures: ["count==0"],
    seat_selection_failures: ["count==0"],
    ticket_issue_failures: ["count==0"],
  },
};

//export const options = {
//  vus: 50,
//  duration: "30s",
//  thresholds: {
//    http_req_failed: ["rate<0.01"],
//    http_req_duration: ["p(95)<1000"],
//  },
//};

// 전역 변수 설정
const BASE_URL = "http://localhost/api/v1"; // Nginx 로드밸런서 주소
const initializedUsers = [];
const USER_COUNT_FROM_INITIALIZER = 5000;
const USER_PASSWORD = __ENV.USER_PASSWORD || "USER_PASSWORD"; // 환경변수가 없으면 기본값 사용

// 사용자 데이터 생성 로직
for (let i = 0; i < USER_COUNT_FROM_INITIALIZER; i++) {
  initializedUsers.push({
    email: `test${i}@example.com`,
    password: USER_PASSWORD,
  });
}
const testUsers = new SharedArray("testUsersArray", function () {
  return initializedUsers;
});

// 커스텀 메트릭
const loginFailures = new Counter("login_failures");
const upcomingViewFailures = new Counter("upcoming_view_failures");
const seatViewFailures = new Counter("seat_view_failures");
const blockViewFailures = new Counter("block_view_failures");
const seatSelectionFailures = new Counter("seat_selection_failures");
const ticketIssueFailures = new Counter("ticket_issue_failures");

// --- 각 가상 사용자(VU)가 실행할 메인 테스트 로직 ---
export default function () {
  const currentUser = testUsers[__VU % testUsers.length];
  if (!currentUser) {
    return;
  }
  const now = new Date().toISOString();

  // === 1. 로그인 ===
  const loginPayload = JSON.stringify({
    email: currentUser.email,
    password: currentUser.password,
  });
  const loginParams = { headers: { "Content-Type": "application/json" } };
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    loginPayload,
    loginParams
  );
  if (
    !check(loginRes, {
      "Login: status is 200 or 202": (r) =>
        r.status === 200 || r.status === 202,
    })
  ) {
    loginFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU} (${currentUser.email}): Login FAILED - Status: ${loginRes.status}, Body: ${loginRes.body}`
    );
    return;
  }
  if (loginRes.status === 202) {
    sleep(1);
    return;
  }
  const accessToken =
    loginRes.headers["Access-Token"] || loginRes.headers["access-token"];
  if (!accessToken) {
    loginFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU} (${currentUser.email}): Login FAILED - No Access Token`
    );
    return;
  }
  const authedHeaders = {
    headers: { Authorization: accessToken, "Content-Type": "application/json" },
  };

  // === 2. 응원팀 경기 조회 ===
  const upcomingRes = http.get(`${BASE_URL}/game/upcoming`, authedHeaders);
  if (
    !check(upcomingRes, {
      "Upcoming View: status is 200 or 202": (r) =>
        r.status === 200 || r.status === 202,
    })
  ) {
    upcomingViewFailures.add(1);
    return;
  }
  if (upcomingRes.status === 202) {
    sleep(1);
    return;
  }

  const upcomingData = upcomingRes.json("data");
  let gameIdToProcess = null;
  if (upcomingData && upcomingData.games && upcomingData.games.length > 0) {
    gameIdToProcess =
      upcomingData.games[__VU % upcomingData.games.length].gameId;
  }
  if (!gameIdToProcess) {
    upcomingViewFailures.add(1);
    return;
  }

//  // === 3-a. 특정 경기 좌석 현황 조회 ===
//  const seatSummaryRes = http.get(
//    `${BASE_URL}/seat/${gameIdToProcess}`,
//    authedHeaders
//  );
//
//  if (
//    !check(seatSummaryRes, {
//      "Seat Summary View: status is 200": (r) => r.status === 200,
//    })
//  ) {
//    seatViewFailures.add(1);
//    return;
//  }
//
//  const summaryData = seatSummaryRes.json("data");
//  const summaries = summaryData && summaryData.seatSummaries;
//
//  let seatSection = null;
//  let seatBlock = null;
//
//  if (summaries && summaries.length > 0) {
//    const selected = summaries[Math.floor(Math.random() * summaries.length)];
//    seatSection = selected.seatSection;
//    seatBlock = selected.seatBlock;
//  } else {
//    seatViewFailures.add(1);
//    console.warn(
//        `[${now}] VU ${__VU} (${currentUser.email}): Seat Summary EMPTY - No seat summaries found for gameId=${gameIdToProcess}, Status: ${seatSummaryRes.status}, Body: ${seatSummaryRes.body}`
//      );
//    return;
//  }
//
//  // === 3-b. 특정 경기 특정 블록 좌석 상세 조회 ===
//  const params = {
//    seatSection: seatSection,
//    seatBlock: seatBlock,
//  };
//
//  const queryString = toQueryString(params);
//
//  const seatDetailRes = http.get(
//    `${BASE_URL}/seat/detail/${gameIdToProcess}?${queryString}`,
//    authedHeaders
//  );
//
//  if (
//    !check(seatDetailRes, {
//      "Block View: status is 200 or 202": (r) =>
//        r.status === 200 || r.status === 202,
//    })
//  ) {
//    blockViewFailures.add(1);
//    return;
//  }
//  if (seatDetailRes.status === 202) {
//    sleep(1);
//    return;
//  }
//
//  const seatViewData = seatDetailRes.json("data");
//  let seatIdForSelection = null;
//  if (seatViewData && seatViewData.seats && seatViewData.seats.length > 0) {
//    seatIdForSelection =
//      seatViewData.seats[Math.floor(Math.random() * seatViewData.seats.length)]
//        .id;
//  }
//  if (!seatIdForSelection) {
//    blockViewFailures.add(1);
//    return;
//  }
//
//  // === 4. 좌석 선점 ===
//  const seatSelectPayload = JSON.stringify({
//    gameId: gameIdToProcess,
//    seatIds: [seatIdForSelection],
//  });
//  const seatSelectRes = http.post(
//    `${BASE_URL}/seat/selection`,
//    seatSelectPayload,
//    authedHeaders
//  );
//
//  const seatSelectJson = seatSelectRes.json();
//
//  const isAlreadyReserved =
//    seatSelectRes.status === 400 &&
//    seatSelectJson.errorMessage === "이미 선점 되어있는 좌석입니다.";
//
//  const isAlreadyHeld =
//    seatSelectRes.status === 400 &&
//    seatSelectJson.errorMessage === "이미 예약 되어있는 좌석입니다.";
//  if (
//    !check(seatSelectRes, {
//      "Seat Select: status is 200, 202, 409, or already held/reserved": (r) =>
//        [200, 202, 409].includes(r.status) || isAlreadyReserved || isAlreadyHeld,
//    })
//  ) {
//    seatSelectionFailures.add(1);
//    console.warn(
//      `[${now}] VU ${__VU} (${currentUser.email}): Seat Select FAILED - Status: ${seatSelectRes.status}, Body: ${seatSelectRes.body}`
//    );
//    return;
//  }
//
//  // 정상 흐름이지만 선점/예약된 좌석이면 로직 종료
//  if (seatSelectRes.status === 202 || seatSelectRes.status === 409 || isAlreadyReserved || isAlreadyHeld) {
//    sleep(1 + Math.random() * 4); // 유사하게 휴식 주기
//    return;
//  }
//
//  // === 5. 티켓 발급===
//  const ticketIssuePayload = JSON.stringify({
//    seatIds: [seatIdForSelection],     // 좌석 ID 배열
//    gameId: gameIdToProcess,           // 경기 ID
//  });
//
//  const ticketIssueRes = http.post(
//    `${BASE_URL}/ticket/issue`,
//    ticketIssuePayload,
//    authedHeaders
//  );
//
//  if (!check(ticketIssueRes, {
//    "Ticket Issue: status is 200 and code is T001": (r) =>
//      r.status === 200 && r.json("code") === "T001",
//  })) {
//    ticketIssueFailures.add(1);
//    console.warn(
//      `[${now}] VU ${__VU} (${currentUser.email}): Ticket Issue FAILED - Status: ${ticketIssueRes.status}, Body: ${ticketIssueRes.body}`
//    );
//    return;
//  }

  sleep(1);
}
