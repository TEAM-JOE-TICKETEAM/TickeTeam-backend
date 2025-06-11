import http from "k6/http";
import { check, fail, sleep } from "k6";
import { SharedArray } from "k6/data";
import { Counter } from "k6/metrics";

// 테스트 설정
export const options = {
  // 시간 및 VU 설정
  // Lamp-up 방식으로 서서히 5000명까지 올렸다 내린다
  stages: [
    { duration: "1m", target: 200 },
    { duration: "3m", target: 500 },
    { duration: "3m", target: 950 },
    { duration: "2m", target: 500 },
    { duration: "1m", target: 200 },
  ],
  insecureSkipTLSVerify: true, // 로컬에서 테스트 중이기에 TLS 인증서 검증 건너뜀
};

// 전역 변수 설정
const BASE_URL = "http://localhost:8080/api/v1";
const initializedUsers = [];
const USER_COUNT_FROM_INITIALIZER = 1000; // 초기 데이터로 넣어주는 사용자 수만큼으로 설정
const USER_PASSWORD = __ENV.USER_PASSWORD;

for (let i = 0; i < USER_COUNT_FROM_INITIALIZER; i++) {
  initializedUsers.push({
    email: `test${i}@example.com`,
    password: USER_PASSWORD,
  });
}
const testUsers = new SharedArray("testUsersArray", function () {
  return initializedUsers;
});

// 커스텀 메트릭 설정
const successfulLogins = new Counter("successful_logins");
const loginFailures = new Counter("login_failures");
const successfulUpcomingViews = new Counter("successful_upcoming_views");
const upcomingViewFailures = new Counter("upcoming_view_failures");
const successfulSeatViews = new Counter("successful_seat_views");
const seatViewFailures = new Counter("seat_view_failures");
const successfulSeatSelections = new Counter("successful_seat_selections");
const seatSelectionFailures = new Counter("seat_selection_failures");
const successfulTicketIssues = new Counter("successful_ticket_issues"); // 티켓 발급 메트릭
const ticketIssueFailures = new Counter("ticket_issue_failures"); // 티켓 발급 메트릭

// 각 가상 사용자(VU)가 반복적으로 실행할 메인 테스트 로직
export default function () {
  // 현재 시각과 전체 사용자 수 변수 선언
  const now = new Date().toISOString();
  const totalUsers = testUsers.length;
  // testUsers 설정이 진행되지 않은 경우 실패
  if (testUsers.length === 0) {
    fail("No test users configured.");
  }

  // 매 시도 별 해당 테스트를 진행할 사용자 설정
  const currentUser = testUsers[__VU % testUsers.length];
  if (!currentUser || !currentUser.email || !currentUser.password) {
    console.error(
      `[${now}] VU ${__VU}/${totalUsers}: Invalid user data. Skipping.`
    );
    return;
  }

  // === 1. 로그인 ===
  // 로그인 시도를 위한 json 데이터
  const loginPayload = JSON.stringify({
    email: currentUser.email,
    password: currentUser.password,
  });
  // 생성한 json 데이터를 파라미터로 넣은 뒤, 요청을 보냄
  const loginRequestParams = {
    headers: { "Content-Type": "application/json" },
  };
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    loginPayload,
    loginRequestParams
  );
  // 요청 응답에 담긴 access-token 헤더를 꺼내 가져온다
  const accessTokenFromHeader =
    loginRes.headers["access-token"] || loginRes.headers["Access-Token"];
  // 응답 결과에 대해 각종 검증 진행(상태 코드, 결과 코드, 응답 헤더 등)
  const loginSuccess =
    loginRes.status === 200 &&
    loginRes.json().code === "M004" &&
    accessTokenFromHeader &&
    accessTokenFromHeader.startsWith("Bearer ");
  // 검증 결과에 따라 각각에 해당하는 메트릭의 카운트를 올린다.
  if (loginSuccess) {
    successfulLogins.add(1);
  } else {
    loginFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): Login Failed - ${loginRes.status} - ${loginRes.body}`
    );
    return;
  }
  // 다음 API들에서 사용가능하도록 Jwt토큰을 Authorization 헤더에 담음
  const authedHeaders = {
    Authorization: accessTokenFromHeader,
    "Content-Type": "application/json",
  };

  // 2. 응원팀 경기 조회
  const upcomingAPIParams = { headers: authedHeaders };
  const upcomingRes = http.get(`${BASE_URL}/game/upcoming`, upcomingAPIParams);
  const upcomingData =
    upcomingRes.status === 200 ? upcomingRes.json().data : null;
  const upcomingSuccess =
    upcomingRes.status === 200 && upcomingRes.json().code === "G001";
  if (upcomingSuccess) {
    successfulUpcomingViews.add(1);
  } else {
    upcomingViewFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): Upcoming Games Failed - ${upcomingRes.status} - ${upcomingRes.body}`
    );
    return;
  }

  let gameIdToProcess = null;
  if (upcomingData && upcomingData.games && upcomingData.games.length > 0) {
    gameIdToProcess =
      upcomingData.games[__VU % upcomingData.games.length].gameId; // 매 시도별 다른 경기 선택
  }
  if (!gameIdToProcess) {
    console.warn(
      `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): No gameId found from upcoming games.`
    );
    upcomingViewFailures.add(1);
    return;
  }

  // 3. 특정 경기 선택(해당 경기 좌석 조회 결과 반환)
  const seatViewParams = { headers: authedHeaders };
  const seatViewRes = http.get(
    `${BASE_URL}/seat/${gameIdToProcess}`,
    seatViewParams
  );
  const seatViewData =
    seatViewRes.status === 200 ? seatViewRes.json().data : null;
  const seatViewSuccess =
    seatViewRes.status === 200 && seatViewRes.json().code === "S001";
  if (seatViewSuccess) {
    successfulSeatViews.add(1);
  } else {
    seatViewFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): Seat View for game ${gameIdToProcess} Failed - ${seatViewRes.status} - ${seatViewRes.body}`
    );
    return;
  }

  let seatIdForSelection = null;
  if (seatViewData && seatViewData.seats && seatViewData.seats.length > 0) {
    const seatIndex = (__VU + __ITER) % seatViewData.seats.length; // 고유 좌석 선택 시도
    seatIdForSelection = seatViewData.seats[seatIndex].id;
  }
  if (!seatIdForSelection) {
    console.warn(
      `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): No available seat found for selection in game ${gameIdToProcess}.`
    );
    seatViewFailures.add(1);
    return;
  }

  // 4. 좌석 선택(선점 발생)
  const seatSelectPayload = JSON.stringify({ seatIds: [seatIdForSelection] });
  const seatSelectParams = { headers: authedHeaders };
  const seatSelectRes = http.post(
    `${BASE_URL}/seat/selection`,
    seatSelectPayload,
    seatSelectParams
  );
  const seatSelectSuccess =
    seatSelectRes.status === 200 && seatSelectRes.json().code === "S002";

  if (seatSelectSuccess) {
    successfulSeatSelections.add(1);
    // console.log(`VU ${__VU} (${currentUser.email}): Successfully selected seat ${seatIdForSelection} for game ${gameIdToProcess}`);

    // === 5. 티켓 발급 ===
    // TicketIssueRequest DTO는 seatIds (List<Long>)와 gameId (Long)를 받음
    const ticketIssuePayload = JSON.stringify({
      seatIds: [seatIdForSelection], // 방금 선점한 좌석 ID
      gameId: gameIdToProcess, // 해당 경기 ID
    });
    const ticketIssueParams = { headers: authedHeaders };
    // TicketController의 issueTickets 메서드는 /api/v1/ticket/issue 경로로 POST 매핑
    const ticketIssueRes = http.post(
      `${BASE_URL}/ticket/issue`,
      ticketIssuePayload,
      ticketIssueParams
    );

    const ticketIssueCheckSuccess = check(ticketIssueRes, {
      "POST /ticket/issue - status is 200": (r) => r.status === 200,
      "POST /ticket/issue - response code is T001": (r) => {
        // ResultCode.TICKET_ISSUE_SUCCESS
        if (r.status !== 200) return false;
        try {
          // TicketService의 issueTickets는 ReservationInfoResponse를 data로 포함하는 ResultResponse 반환
          const body = r.json();
          return body && body.code === "T001" && body.data !== undefined;
        } catch (e) {
          return false;
        }
      },
    });

    if (ticketIssueCheckSuccess && ticketIssueRes.status === 200) {
      successfulTicketIssues.add(1);
      // console.log(`VU ${__VU} (${currentUser.email}): Successfully issued ticket for seat ${seatIdForSelection}, game ${gameIdToProcess}. Reservation Code: ${ticketIssueRes.json().data.reservationCode}`);
    } else {
      ticketIssueFailures.add(1);
      // 실패 시 응답 본문에 ErrorCode 정보가 있을 수 있음 (예: SEAT_NOT_HELD, SEAT_HELD_BY_OTHER 등)
      console.warn(
        `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): Ticket issue for seat ${seatIdForSelection} failed - Status: ${ticketIssueRes.status}, Body: ${ticketIssueRes.body}`
      );
    }
  } else {
    seatSelectionFailures.add(1);
    console.warn(
      `[${now}] VU ${__VU}/${totalUsers} (${currentUser.email}): Seat selection for seat ${seatIdForSelection} failed - Status: ${seatSelectRes.status}, Body: ${seatSelectRes.body}`
    );
  }

  sleep(1); // 전체 시나리오 반복 후 대기
}
