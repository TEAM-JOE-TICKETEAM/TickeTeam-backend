package com.tickeTeam.domain.seat.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.MemberRole;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.seat.dto.request.SeatSelectRequest;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatInfo;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.entity.SeatTemplate;
import com.tickeTeam.domain.seat.entity.SeatType;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.seat.repository.SeatTemplateRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SeatServiceConcurrencyTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private SeatTemplateRepository seatTemplateRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeatService seatService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Game testGame;
    private Seat testSeat; // 동시 접근 대상 좌석
    private List<Member> testMembers; // 여러 사용자
    private SeatTemplate testSeatTemplate;

    // 초기 데이터 설정
    @BeforeEach
    void setUp(){
        // 1. Initializer로 생성된 경기장 및 팀 데이터 조회
        Stadium fetchedStadium = stadiumRepository.findByStadiumName("잠실 야구장")
                .orElseThrow(() -> new IllegalStateException("Stadium '잠실 야구장' not found. Check StadiumInitializer."));

        Team fetchedHomeTeam = teamRepository.findByTeamName("두산 베어스")
                .orElseThrow(() -> new IllegalStateException("Team '두산 베어스' not found. Check TeamInitializer."));
        Team fetchedAwayTeam = teamRepository.findByTeamName("LG 트윈스") // 예시로 다른 팀을 가져오거나, 테스트에 필요한 팀을 사용
                .orElseThrow(() -> new IllegalStateException("Team 'LG 트윈스' not found. Check TeamInitializer."));

        // 2. 경기 생성 (조회한 경기장 및 팀 사용)
        testGame = Game.builder()
                .homeTeam(fetchedHomeTeam)
                .awayTeam(fetchedAwayTeam)
                .stadium(fetchedStadium)
                .matchDay(LocalDate.now().plusDays(1)) // 오늘 날짜 [2025-05-18] 기준 다음 날
                .matchTime(LocalTime.of(18, 30))
                .build();
        gameRepository.save(testGame);

        // 3. 좌석 템플릿 생성
        testSeatTemplate = seatTemplateRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    SeatTemplate newTemplate = SeatTemplate.builder()
                            .seatInfo(SeatInfo.builder()
                                    .seatType(SeatType.ASSIGNED)
                                    .seatSection("1루 레드석")
                                    .seatBlock("329")
                                    .seatRow("1")
                                    .seatNum(1)
                                    .build())
                            .build();
                    return seatTemplateRepository.save(newTemplate);
                });

        // 4. 테스트 좌석 생성
        testSeat = Seat.builder()
                .game(testGame)
                .seatStadium(fetchedStadium)
                .seatTemplate(testSeatTemplate)
                .seatStatus(SeatStatus.AVAILABLE)
                .build();
        seatRepository.save(testSeat);

        // 5. 여러 테스트 사용자 생성
        testMembers = new ArrayList<>();
        int numberOfUsers = 5; // 동시 요청을 보낼 사용자 수
        for (int i = 0; i < numberOfUsers; i++) {
            Member member = Member.builder()
                    .email("user" + i + "@example.com")
                    .password(passwordEncoder.encode("password"))
                    .name("TestUser" + i)
                    .favoriteTeam(fetchedHomeTeam) // Initializer로 생성된 팀 사용
                    .role(MemberRole.USER)
                    .build();
            // 테스트 시에는 이미 존재하는 이메일일 수 있으므로, 조회 후 없으면 저장하는 로직 추가 가능
            testMembers.add(memberRepository.findByEmail(member.getEmail())
                    .orElseGet(() -> memberRepository.save(member)));
        }
    }

    // 테스트 완료 후 생성 데이터 삭제
    @AfterEach
    void tearDown() {
        // Stadium과 Team은 Initializer는 생성하기에 삭제 고려 X
        // 삭제 순서는 생성 순서의 역순 혹은 외래 키 제약 조건을 반드시 고려

        // Seat -> Member와 Game을 참조
        // 1. Seat 삭제 (testSeat만 삭제, 또는 gameId로 해당 게임의 모든 좌석 삭제)
        if (testSeat != null && testSeat.getId() != null) {
            seatRepository.deleteById(testSeat.getId());
        }

        // 2. Member 삭제(testMembers에 있는 사용자들 삭제)
        if (testMembers != null && !testMembers.isEmpty()) {
            List<Long> memberIds = testMembers.stream().map(Member::getId).toList();
            memberRepository.deleteAllByIdInBatch(memberIds);
        }

        // 3. Game 삭제 (testGame 삭제)
        if (testGame != null && testGame.getId() != null) {
            gameRepository.deleteById(testGame.getId());
        }

        // 4. SeatTemplate 삭제 (testSeatTemplate 삭제)
        if (testSeatTemplate != null && testSeatTemplate.getId() != null && seatTemplateRepository.existsById(testSeatTemplate.getId())) {
            seatTemplateRepository.deleteById(testSeatTemplate.getId());
        }
    }

    @Test
    @DisplayName("여러 사용자가 동시에 같은 좌석 선택 시 한 명만 선점에 성공해야 한다 (PESSIMISTIC_WRITE)")
    void 동시에_같은_좌석_선택_한_명만_선점_성공() throws InterruptedException{

        int numberOfThreads = testMembers.size(); // 생성 사용자 수 만큼 스레드 생성
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads); // 모든 스레드가 준비될 때까지 대기
        CountDownLatch startLatch = new CountDownLatch(1); // 모든 스레드가 동시에 시작하도록 제어
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads); // 모든 스레드가 작업 마칠 때까지 대기

        AtomicInteger successCount = new AtomicInteger(0); // 선점 성공 카운트
        AtomicInteger alreadyHeldCount = new AtomicInteger(0); // 이미 선점된 좌석 예외 카운트

        List<Long> targetSeatIds = new ArrayList<>(List.of(testSeat.getId())); // 모든 사용자가 이 좌석을 선택 시도

        for (Member member : testMembers) {
            executorService.submit(() -> {
                try{
                    // 각 스레드 대한 인증 정보 설정
                    UserDetails userDetails = User.builder()
                            .username(member.getEmail())
                            .password(member.getPassword()) // 실제 비밀번호 검증은 SeatService에서 하지 않으므로 "password" 등 더미 값 사용 가능
                            .authorities(Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name())))
                            .build();
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // 스레드 로컬에 SecurityContext 설정 (중요!)
                    SecurityContextHolder.createEmptyContext(); // 새 컨텍스트 생성
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    readyLatch.countDown(); // 준비 완료 알림
                    startLatch.await();     // 시작 신호 대기

                    // 실제 서비스 메소드 호출
                    // SeatService의 selectSeats는 ResultResponse를 반환하므로, 실제 반환값을 확인하거나 예외를 잡아야 합니다.
                    // 여기서는 예외 발생 여부로 간단히 처리
                    try {
                        seatService.selectSeats(SeatSelectRequest.of(targetSeatIds));
                        successCount.incrementAndGet();

                        System.out.println("선점 성공 사용자: "+member.getEmail());

                    } catch (BusinessException e) {
                        if (e.getErrorCode() == ErrorCode.SEAT_ALREADY_HELD) {
                            alreadyHeldCount.incrementAndGet();
                            System.out.println("선점 실패 사용자: "+member.getEmail());

                        } else {
                            // 다른 비즈니스 예외가 발생하면 테스트 실패로 간주하거나 로깅
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        // 예상치 못한 예외 로깅
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown(); // 작업 완료 알림
                    SecurityContextHolder.clearContext(); // 스레드 로컬 정리 (중요!)
                }
            });
        }

        readyLatch.await(); // 모든 스레드 준비까지 대기
        startLatch.countDown(); // 모든 스레드 동시에 시작

        // 모든 스레드가 작업을 완료할 때까지 충분한 시간 동안 대기 (예: 10초)
        // 실제 환경과 서비스 로직의 복잡성에 따라 타임아웃 조절 필요
        boolean allThreadsDone = doneLatch.await(10, TimeUnit.SECONDS);
        assertThat(allThreadsDone).isTrue().withFailMessage("모든 스레드가 시간 내에 작업을 완료하지 못했습니다.");

        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            System.err.println("ExecutorService did not terminate in the specified time.");
            List<Runnable> droppedTasks = executorService.shutdownNow();
            System.err.println("ExecutorService was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
        }

        // 최종 결과 검증
        // 1. 좌석 상태 확인 (DB에서 직접 조회)
        Seat finalSeatState = seatRepository.findById(testSeat.getId())
                .orElseThrow(() -> new AssertionError("테스트 좌석을 찾을 수 없습니다."));

        assertThat(finalSeatState.getSeatStatus()).isEqualTo(SeatStatus.HELD); // 한 명은 선점 성공
        assertThat(finalSeatState.getHoldMember()).isNotNull(); // 선점한 사용자가 있어야 함
        assertThat(successCount.get()).isEqualTo(1).withFailMessage("정확히 한 명의 사용자만 좌석 선점에 성공해야 합니다.");
        System.out.println("선점 성공 스레드 수: " + successCount.get());
        System.out.println("이미 선점됨 예외 발생 수: " + alreadyHeldCount.get());
    }
}
