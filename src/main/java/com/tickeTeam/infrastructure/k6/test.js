import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter } from 'k6/metrics';

// --- 테스트 설정 ---
export const options = {
  stages: [
    { duration: '2m', target: 20 },
    { duration: '3m', target: 50 },
    { duration: '5m', target: 100 },
    { duration: '3m', target: 150 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],      // 실패율 5% 미만 (동시성 고려)
    http_req_duration: ['p(95)<2500'],   // 전체 시나리오 응답 시간 2.5초 미만
    'successful_logins': ['count>=10'],
    'successful_upcoming_views': ['count>=10'],
    'successful_seat_views': ['count>=10'],
    'successful_seat_selections': ['count>=5'], // 선점 경쟁으로 성공률이 낮을 수 있음
    'successful_ticket_issues': ['count>=5'],   // 티켓 발급 성공 카운트
  },
  insecureSkipTLSVerify: true,
};

// --- 테스트 데이터 생성 (UserInitializer 기반) ---
const initializedUsers = [];
const USER_COUNT_FROM_INITIALIZER = 10;
const USER_PASSWORD = "test";

for (let i = 0; i < USER_COUNT_FROM_INITIALIZER; i++) {
  initializedUsers.push({
    email: `test${i}@example.com`,
    password: USER_PASSWORD
  });
}
const testUsers = new SharedArray('testUsersArray', function() { return initializedUsers; });

const BASE_URL = 'http://localhost:8080/api/v1';

// --- 커스텀 메트릭 ---
const successfulLogins = new Counter('successful_logins');
const loginFailures = new Counter('login_failures');
const successfulUpcomingViews = new Counter('successful_upcoming_views');
const upcomingViewFailures = new Counter('upcoming_view_failures');
const successfulSeatViews = new Counter('successful_seat_views');
const seatViewFailures = new Counter('seat_view_failures');
const successfulSeatSelections = new Counter('successful_seat_selections');
const seatSelectionFailures = new Counter('seat_selection_failures');
const successfulTicketIssues = new Counter('successful_ticket_issues'); // 티켓 발급 메트릭
const ticketIssueFailures = new Counter('ticket_issue_failures');     // 티켓 발급 메트릭

// --- 테스트 실행 함수 ---
export default function () {
  if (testUsers.length === 0) { fail('No test users configured.'); }
  const currentUser = testUsers[__VU % testUsers.length];
  if (!currentUser || !currentUser.email || !currentUser.password) {
    console.error(`VU ${__VU}: Invalid user data. Skipping.`);
    return;
  }

  // === 1. 로그인 ===
  const loginPayload = JSON.stringify({ email: currentUser.email, password: currentUser.password });
  const loginRequestParams = { headers: { 'Content-Type': 'application/json' } };
  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, loginRequestParams);
  const accessTokenFromHeader = loginRes.headers['access-token'] || loginRes.headers['Access-Token'];

  const loginSuccess = loginRes.status === 200 && loginRes.json().code === 'M004' && accessTokenFromHeader && accessTokenFromHeader.startsWith('Bearer ');
  if (loginSuccess) { successfulLogins.add(1); } else { loginFailures.add(1); console.warn(`VU ${__VU} (${currentUser.email}): Login Failed - ${loginRes.status} - ${loginRes.body}`); return; }

  const authedHeaders = { 'Authorization': accessTokenFromHeader, 'Content-Type': 'application/json' };

  // === 2. 응원팀 일정 조회 ===
  const upcomingAPIParams = { headers: authedHeaders };
  const upcomingRes = http.get(`${BASE_URL}/game/upcoming`, upcomingAPIParams);
  const upcomingData = upcomingRes.status === 200 ? upcomingRes.json().data : null;
  const upcomingSuccess = upcomingRes.status === 200 && upcomingRes.json().code === 'G001';

  if (upcomingSuccess) { successfulUpcomingViews.add(1); } else { upcomingViewFailures.add(1); console.warn(`VU ${__VU} (${currentUser.email}): Upcoming Games Failed - ${upcomingRes.status} - ${upcomingRes.body}`); return; }

  let gameIdToProcess = null;
  if (upcomingData && upcomingData.games && upcomingData.games.length > 0) {
    gameIdToProcess = upcomingData.games[0].gameId; // 첫 번째 게임 사용
  }
  if (!gameIdToProcess) { console.warn(`VU ${__VU} (${currentUser.email}): No gameId found from upcoming games.`); upcomingViewFailures.add(1); return; }

  // === 3. 특정 경기 좌석 목록 조회 ===
  const seatViewParams = { headers: authedHeaders };
  const seatViewRes = http.get(`${BASE_URL}/seat/${gameIdToProcess}`, seatViewParams);
  const seatViewData = seatViewRes.status === 200 ? seatViewRes.json().data : null;
  const seatViewSuccess = seatViewRes.status === 200 && seatViewRes.json().code === 'S001';

  if (seatViewSuccess) { successfulSeatViews.add(1); } else { seatViewFailures.add(1); console.warn(`VU ${__VU} (${currentUser.email}): Seat View for game ${gameIdToProcess} Failed - ${seatViewRes.status} - ${seatViewRes.body}`); return; }

  let seatIdForSelection = null;
  if (seatViewData && seatViewData.seats && seatViewData.seats.length > 0) {
    const seatIndex = (__VU + __ITER) % seatViewData.seats.length; // 고유 좌석 선택 시도
    seatIdForSelection = seatViewData.seats[seatIndex].id;
  }
  if (!seatIdForSelection) { console.warn(`VU ${__VU} (${currentUser.email}): No available seat found for selection in game ${gameIdToProcess}.`); seatViewFailures.add(1); return; }

  // === 4. 좌석 선택 (선점) ===
  const seatSelectPayload = JSON.stringify({ seatIds: [seatIdForSelection] });
  const seatSelectParams = { headers: authedHeaders };
  const seatSelectRes = http.post(`${BASE_URL}/seat/selection`, seatSelectPayload, seatSelectParams);
  const seatSelectSuccess = seatSelectRes.status === 200 && seatSelectRes.json().code === 'S002';

  if (seatSelectSuccess) {
    successfulSeatSelections.add(1);
    // console.log(`VU ${__VU} (${currentUser.email}): Successfully selected seat ${seatIdForSelection} for game ${gameIdToProcess}`);

    // === 5. 티켓 발급 ===
    // TicketIssueRequest DTO는 seatIds (List<Long>)와 gameId (Long)를 받음
    const ticketIssuePayload = JSON.stringify({
      seatIds: [seatIdForSelection], // 방금 선점한 좌석 ID
      gameId: gameIdToProcess        // 해당 경기 ID
    });
    const ticketIssueParams = { headers: authedHeaders };
    // TicketController의 issueTickets 메서드는 /api/v1/ticket/issue 경로로 POST 매핑
    const ticketIssueRes = http.post(`${BASE_URL}/ticket/issue`, ticketIssuePayload, ticketIssueParams);

    const ticketIssueCheckSuccess = check(ticketIssueRes, {
      'POST /ticket/issue - status is 200': (r) => r.status === 200,
      'POST /ticket/issue - response code is T001': (r) => { // ResultCode.TICKET_ISSUE_SUCCESS
        if (r.status !== 200) return false;
        try {
            // TicketService의 issueTickets는 ReservationInfoResponse를 data로 포함하는 ResultResponse 반환
            const body = r.json();
            return body && body.code === 'T001' && body.data !== undefined;
        } catch (e) { return false; }
      }
    });

    if (ticketIssueCheckSuccess && ticketIssueRes.status === 200) {
      successfulTicketIssues.add(1);
      // console.log(`VU ${__VU} (${currentUser.email}): Successfully issued ticket for seat ${seatIdForSelection}, game ${gameIdToProcess}. Reservation Code: ${ticketIssueRes.json().data.reservationCode}`);
    } else {
      ticketIssueFailures.add(1);
      // 실패 시 응답 본문에 ErrorCode 정보가 있을 수 있음 (예: SEAT_NOT_HELD, SEAT_HELD_BY_OTHER 등)
      console.warn(`VU ${__VU} (${currentUser.email}): Ticket issue for seat ${seatIdForSelection} failed - Status: ${ticketIssueRes.status}, Body: ${ticketIssueRes.body}`);
    }

  } else {
    seatSelectionFailures.add(1);
    console.warn(`VU ${__VU} (${currentUser.email}): Seat selection for seat ${seatIdForSelection} failed - Status: ${seatSelectRes.status}, Body: ${seatSelectRes.body}`);
  }

  sleep(1); // 전체 시나리오 반복 후 대기
}