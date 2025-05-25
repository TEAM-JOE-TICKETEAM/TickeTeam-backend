package com.tickeTeam.domain.seat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@SpringBootTest
public class SeatSelectTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatTemplateRepository seatTemplateRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedissonClient redissonClient;

    private String statusKeyA;
    private String statusKeyB;
    private Seat seatA;
    private Seat seatB;

    @BeforeEach
    @Transactional
    void setUp() {
        final Team exHomeTeam = teamRepository.findByTeamName("두산 베어스").orElseThrow();
        final Team exAwayTeam = teamRepository.findByTeamName("LG 트윈스").orElseThrow();

        final Stadium exStadium = stadiumRepository.findByStadiumName("잠실 야구장").orElseThrow();

        final Member exMember = Member.builder()
                .name("tester")
                .email("test@example.com")
                .password("password")
                .favoriteTeam(exHomeTeam)
                .role(MemberRole.USER)
                .build();

        if (!memberRepository.existsByEmail("test@example.com")) memberRepository.save(exMember);

        final SeatTemplate exTemplate = SeatTemplate.builder()
                .seatInfo(SeatInfo.builder()
                        .seatType(SeatType.ASSIGNED)
                        .seatSection("1루 레드석")
                        .seatBlock("329")
                        .seatRow("1")
                        .seatNum(1)
                        .build())
                .build();
        seatTemplateRepository.save(exTemplate);

        final Game exGame = Game.builder()
                .homeTeam(exHomeTeam)
                .awayTeam(exAwayTeam)
                .stadium(exStadium)
                .matchDay(LocalDate.now().plusDays(1)) // 오늘 날짜 [2025-05-18] 기준 다음 날
                .matchTime(LocalTime.of(18, 30))
                .build();
        gameRepository.save(exGame);

        final Seat exSeatA = Seat.builder()
                .seatStadium(exStadium)
                .seatTemplate(exTemplate)
                .seatStatus(SeatStatus.AVAILABLE)
                .game(exGame)
                .build();

        this.seatA = seatRepository.save(exSeatA);
        this.statusKeyA = seatService.keyResolver(seatA.getId());

        final Seat exSeatB = Seat.builder()
                .seatStadium(exStadium)
                .seatTemplate(exTemplate)
                .seatStatus(SeatStatus.AVAILABLE)
                .game(exGame)
                .build();

        this.seatB = seatRepository.save(exSeatB);
        this.statusKeyB = seatService.keyResolver(seatB.getId());
    }

    @AfterEach
    void tearDown(){
        // Redis 데이터 정리
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").delete();
        redissonClient.getBucket(seatService.keyResolver(seatB.getId())+":heldBy").delete();

        SecurityContextHolder.clearContext();
    }

    // SecurityContextHolder 설정을 위한 헬퍼 메소드
    private void setupMockAuthentication(String email, String role) {
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void 좌석_선점_단일_좌석_단일_스레드(){
        setupMockAuthentication("test@example.com", "USER");
        Long seatAId = seatA.getId(); // 서비스 호출 전 ID 저장

        seatService.selectSeats(SeatSelectRequest.of(new ArrayList<>(List.of(seatAId))));

        // 서비스 호출 후 DB에서 최신 상태의 Seat 객체를 다시 가져와서 검증
        Seat updatedSeatA = seatRepository.findById(seatAId)
                .orElseThrow(() -> new AssertionError("Seat A를 찾을 수 없습니다. ID: " + seatAId));

        assertEquals(SeatStatus.HELD, updatedSeatA.getSeatStatus(), "Seat A의 상태가 HELD여야 합니다.");
    }

    @Test
    void 좌석_선점_다중_좌석_단일_스레드(){
        setupMockAuthentication("test@example.com", "USER");
        Long seatAId = seatA.getId(); // 서비스 호출 전 ID 저장
        Long seatBId = seatB.getId();

        seatService.selectSeats(SeatSelectRequest.of(new ArrayList<>(List.of(seatAId, seatBId))));

        // 서비스 호출 후 DB에서 최신 상태의 Seat 객체를 다시 가져와서 검증
        Seat updatedSeatA = seatRepository.findById(seatAId)
                .orElseThrow(() -> new AssertionError("Seat A를 찾을 수 없습니다. ID: " + seatAId));
        Seat updatedSeatB = seatRepository.findById(seatBId)
                .orElseThrow(() -> new AssertionError("Seat B를 찾을 수 없습니다. ID: " + seatBId));

        assertEquals(SeatStatus.HELD, updatedSeatA.getSeatStatus(), "Seat A의 상태가 HELD여야 합니다.");
        assertEquals(SeatStatus.HELD, updatedSeatB.getSeatStatus(), "Seat B의 상태가 HELD여야 합니다.");
    }

    @Test
    void 락O_10명_동시_선점시_단_1명만_성공() throws InterruptedException {
        int people = 10;
        CountDownLatch latch = new CountDownLatch(people);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        Long seatAId = seatA.getId();

        for (int i = 0; i < people; i++) {
            new Thread(() -> {
                setupMockAuthentication("test@example.com", "USER");
                try{
                    seatService.selectSeats(SeatSelectRequest.of(new ArrayList<>(List.of(seatAId))));
                } catch (BusinessException e){
                    assertEquals(ErrorCode.SEAT_ALREADY_HELD, e.getErrorCode());
                    exceptionCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        Seat updatedSeatA = seatRepository.findById(seatAId)
                .orElseThrow(() -> new AssertionError("Seat A를 찾을 수 없습니다. ID: " + seatAId));
        log.info("예외 발생 횟수: {}", exceptionCount);
        assertEquals(SeatStatus.HELD, updatedSeatA.getSeatStatus(), "Seat A의 상태가 HELD여야 합니다.");
    }

    @Test
    void 좌석_2개에_각각_5명씩_동시_접근() throws InterruptedException {
        int people = 10;
        CountDownLatch latch = new CountDownLatch(people);
        AtomicInteger exceptionCountA = new AtomicInteger(0);
        AtomicInteger exceptionCountB = new AtomicInteger(0);
        Long seatAId = seatA.getId(); // 서비스 호출 전 ID 저장
        Long seatBId = seatB.getId();

        // 좌석 A에 대해 5명 동시 요청
        for (int i = 1; i <= people; i++) {
            Long seatId;
            if (i % 2 == 0) seatId = seatB.getId();
            else seatId = seatA.getId();

            new Thread(() -> {
                setupMockAuthentication("test@example.com", "USER");
                try{
                    seatService.selectSeats(SeatSelectRequest.of(new ArrayList<>(List.of(seatId))));
                } catch (BusinessException e){
                    assertEquals(ErrorCode.SEAT_ALREADY_HELD, e.getErrorCode());
                    if (seatId == seatAId) exceptionCountA.incrementAndGet();
                    else exceptionCountB.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        // 각각 한 명만 성공해야 하므로 두 좌석 모두 HELD 상태여야 함
        Seat updatedSeatA = seatRepository.findById(seatAId)
                .orElseThrow(() -> new AssertionError("Seat A를 찾을 수 없습니다. ID: " + seatAId));
        Seat updatedSeatB = seatRepository.findById(seatBId)
                .orElseThrow(() -> new AssertionError("Seat B를 찾을 수 없습니다. ID: " + seatBId));

        assertEquals(SeatStatus.HELD, updatedSeatA.getSeatStatus(), "Seat A의 상태가 HELD여야 합니다.");
        assertEquals(SeatStatus.HELD, updatedSeatB.getSeatStatus(), "Seat B의 상태가 HELD여야 합니다.");
        log.info("seatA 선점 시도 예외 발생 횟수: {}", exceptionCountA);
        log.info("seatB 선점 시도 예외 발생 횟수: {}", exceptionCountB);
    }

    @Test
    void 좌석_2개_다중_선택_10명_동시_접근() throws InterruptedException {
        int people = 10;
        CountDownLatch latch = new CountDownLatch(people);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        Long seatAId = seatA.getId();
        Long seatBId = seatB.getId();

        for (int i = 1; i <= people; i++) {
            new Thread(() -> {
                setupMockAuthentication("test@example.com", "USER");
                try{
                    seatService.selectSeats(SeatSelectRequest.of(new ArrayList<>(List.of(seatAId, seatBId))));
                } catch (BusinessException e){
                    assertEquals(ErrorCode.SEAT_ALREADY_HELD, e.getErrorCode());
                    exceptionCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        Seat updatedSeatA = seatRepository.findById(seatAId)
                .orElseThrow(() -> new AssertionError("Seat A를 찾을 수 없습니다. ID: " + seatAId));
        Seat updatedSeatB = seatRepository.findById(seatBId)
                .orElseThrow(() -> new AssertionError("Seat B를 찾을 수 없습니다. ID: " + seatBId));

        assertEquals(SeatStatus.HELD, updatedSeatA.getSeatStatus(), "Seat A의 상태가 HELD여야 합니다.");
        assertEquals(SeatStatus.HELD, updatedSeatB.getSeatStatus(), "Seat B의 상태가 HELD여야 합니다.");
        log.info("선점 시도 예외 발생 횟수: {}", exceptionCount);

    }
}
