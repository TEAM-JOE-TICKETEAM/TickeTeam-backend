import http from "k6/http";
import { check, fail, sleep } from "k6";
import { SharedArray } from "k6/data";
import { Counter } from "k6/metrics";

// --- 설정 부분 ---
export const options = {
  stages: [
    { duration: "2m", target: 1500 },
    { duration: "2m", target: 2500 },
    { duration: "3m", target: 5000 },
    { duration: "2m", target: 0 },
  ],
  insecureSkipTLSVerify: true,
  thresholds: {
    http_req_failed: ["rate<0.01"], // 전체 HTTP 실패율은 1% 미만
    login_failures: ["count==0"], // 로그인 실패는 0건이어야 함
  },
};

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

// 커스텀 메트릭 설정 (로그인 관련만 사용)
const successfulLogins = new Counter("successful_logins");
const loginFailures = new Counter("login_failures");
const queuedRequests = new Counter("queued_requests"); // 대기열 진입 횟수 카운터
const successfulUpcomingViews = new Counter("successful_upcoming_views");
const upcomingViewFailures = new Counter("upcoming_view_failures");
const successfulSeatViews = new Counter("successful_seat_views");
const seatViewFailures = new Counter("seat_view_failures");
// --- 각 가상 사용자(VU)가 실행할 메인 테스트 로직 ---
export default function () {
  if (testUsers.length === 0) {
    fail("No test users configured.");
  }

  const now = new Date().toISOString();
  const currentUser = testUsers[__VU % testUsers.length];

  // 현재 VU에 해당하는 사용자 선택
  if (!currentUser || !currentUser.email || !currentUser.password) {
    console.error(`VU ${__VU}: Invalid user data. Skipping.`);
    return;
  }

  // === 1. 로그인 ===
  const loginPayload = JSON.stringify({
    email: currentUser.email,
    password: currentUser.password,
  });

  const loginRequestParams = {
    headers: { "Content-Type": "application/json" },
  };

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    loginPayload,
    loginRequestParams
  );

  // === 로그인 결과 검증 ===
  const loginCheck = check(loginRes, {
    "Request is valid (status 200 or 202)": (r) =>
      r.status === 200 || r.status === 202,
  });

  // 1. check가 실패한 경우 (500 에러 등) -> 명백한 실패
  if (!loginCheck) {
    loginFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU} (${currentUser.email}): Login Request FAILED - Status: ${loginRes.status}, Body: ${loginRes.body}`
    );
    return;
  }

  // 202(대기열) 응답을 받으면 이번 반복은 여기서 종료
  if (loginRes.status === 202) {
    queuedRequests.add(1);
    sleep(1);
    return;
  }

  // 실제 로그인이 성공한 경우(200)에만 다음 로직 진행
  if (loginRes.status === 200) {
    successfulLogins.add(1);

    // 1. 응답 헤더에서 Access Token 추출
    const accessToken =
      loginRes.headers["Access-Token"] || loginRes.headers["access-token"];
    if (!accessToken) {
      // 토큰이 없으면 실패 처리
      loginFailures.add(1);
      return;
    }

    // 2. 인증 헤더 생성
    const authedHeaders = {
      headers: {
        Authorization: accessToken, // 'Bearer ' 접두어는 서버에서 이미 처리된 것으로 가정
        "Content-Type": "application/json",
      },
    };

    // === 2. 응원팀 경기 조회 ===
    const upcomingRes = http.get(`${BASE_URL}/game/upcoming`, authedHeaders);

    const isUpcomingViewPassed = check(upcomingRes, {
      "Upcoming View Passed (status 200 or 202)": (r) =>
        r.status === 200 || r.status === 202,
    });
    if (!isUpcomingViewPassed) {
      upcomingViewFailures.add(1);
      return;
    }
    if (upcomingRes.status === 200) {
      successfulUpcomingViews.add(1);
      const upcomingData = upcomingRes.json("data");
      let gameIdToProcess = null;

      if (upcomingData && upcomingData.games && upcomingData.games.length > 0) {
        gameIdToProcess =
          upcomingData.games[__VU % upcomingData.games.length].gameId;
      }
      if (gameIdToProcess) {
        // ▼▼▼ 3. 특정 경기 좌석 조회 (추가된 로직) ▼▼▼
        const seatViewRes = http.get(
          `${BASE_URL}/seat/${gameIdToProcess}`,
          authedHeaders
        );
        const isSeatViewPassed = check(seatViewRes, {
          "Seat View Passed (200 or 202)": (r) =>
            r.status === 200 || r.status === 202,
        });

        if (isSeatViewPassed && seatViewRes.status === 200) {
          successfulSeatViews.add(1);
        } else {
          seatViewFailures.add(1);
        }
      } else {
        upcomingViewFailures.add(1); // 조회할 경기를 찾지 못해 실패 처리
      }
    }
  }

  sleep(1);
}
