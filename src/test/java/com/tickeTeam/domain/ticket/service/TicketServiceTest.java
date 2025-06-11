package com.tickeTeam.domain.ticket.service;

import static org.assertj.core.api.Assertions.*;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.MemberRole;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatInfo;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.entity.SeatTemplate;
import com.tickeTeam.domain.seat.entity.SeatType;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.seat.repository.SeatTemplateRepository;
import com.tickeTeam.domain.seat.service.SeatService;
import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import com.tickeTeam.domain.ticket.dto.request.TicketIssueRequest;
import com.tickeTeam.domain.ticket.dto.request.TicketingCancelRequest;
import com.tickeTeam.domain.ticket.dto.response.ReservationInfoResponse;
import com.tickeTeam.domain.ticket.repository.ReservationRepository;
import com.tickeTeam.domain.ticket.repository.TicketRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@SpringBootTest
class TicketServiceTest {

    @Autowired
    TicketService ticketService;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    private SectionPriceRepository sectionPriceRepository;

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

    private TicketIssueRequest ticketIssueRequest;
    private TicketingCancelRequest ticketingCancelRequest;
    private String statusKeyA;
    private String statusKeyB;
    private Seat seatA;
    private Seat seatB;

    @BeforeEach
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

        final Game exGame = Game.builder()
                .homeTeam(exHomeTeam)
                .awayTeam(exAwayTeam)
                .stadium(exStadium)
                .matchDay(LocalDate.now().plusDays(1)) // 오늘 날짜 기준 다음 날
                .matchTime(LocalTime.of(18, 30))
                .build();
        gameRepository.save(exGame);

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

        final SectionPrice exSectionPrice = SectionPrice.of(
                exStadium,
                "1루 레드석",
                12000
        );
        sectionPriceRepository.save(exSectionPrice);

        final Seat exSeatA = Seat.builder()
                .seatStadium(exStadium)
                .seatTemplate(exTemplate)
                .seatStatus(SeatStatus.HELD)
                .game(exGame)
                .build();

        this.seatA = seatRepository.save(exSeatA);
        this.statusKeyA = seatService.keyResolver(seatA.getId());

        final Seat exSeatB = Seat.builder()
                .seatStadium(exStadium)
                .seatTemplate(exTemplate)
                .seatStatus(SeatStatus.HELD)
                .game(exGame)
                .build();

        this.seatB = seatRepository.save(exSeatB);
        this.statusKeyB = seatService.keyResolver(seatB.getId());

        ticketIssueRequest = new TicketIssueRequest(List.of(seatA.getId(), seatB.getId()), exGame.getId());
        ticketingCancelRequest = new TicketingCancelRequest(List.of(seatA.getId(), seatB.getId()));

        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").set("test@example.com", 7, TimeUnit.MINUTES);
        redissonClient.getBucket(seatService.keyResolver(seatB.getId())+":heldBy").set("test@example.com", 7, TimeUnit.MINUTES);
    }

    @AfterEach
    void tearDown(){
        // Redis 데이터 정리
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").delete();
        redissonClient.getBucket(seatService.keyResolver(seatB.getId())+":heldBy").delete();

        sectionPriceRepository.deleteAll();

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
    @DisplayName("티켓 발급 요청 - 성공")
    void 티켓_발급_요청_성공() {
        // given
        setupMockAuthentication("test@example.com", "USER");

        // when
        ResultResponse resultResponse = ticketService.issueTickets(ticketIssueRequest);

        // then
        assertThat(resultResponse).isNotNull();
        assertThat(resultResponse.getCode()).isEqualTo(ResultCode.TICKET_ISSUE_SUCCESS.getCode());
        assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.TICKET_ISSUE_SUCCESS.getMessage());

        ReservationInfoResponse resultResponseData = (ReservationInfoResponse) resultResponse.getData();
        assertThat(resultResponseData.getTicketInfos().size()).isEqualTo(2);
        assertThat(resultResponseData.getMemberName()).isEqualTo("tester");
    }

    @Test
    @DisplayName("티켓 발급 요청 - 실패(선점되어 있지 않음)")
    void 티켓_발급_요청_실패_선점_X() {
        // given
        setupMockAuthentication("test@example.com", "USER");
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").delete();

        // when & then
        assertThatThrownBy(() -> ticketService.issueTickets(ticketIssueRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SEAT_NOT_HELD.getMessage());
    }

    @Test
    @DisplayName("티켓 발급 요청 - 실패(선점 정보와 일치하지 않음)")
    void 티켓_발급_요청_실패_선점_정보_불일치() {
        // given
        setupMockAuthentication("test@example.com", "USER");
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").delete();
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").set("wrong@example.com", 7, TimeUnit.MINUTES);

        System.out.println("티켓_발급_요청_실패_선점_정보_불일치 현재 레디스 캐시 내용: "+
                redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").get());
        // when & then
        assertThatThrownBy(() -> ticketService.issueTickets(ticketIssueRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SEAT_HELD_BY_OTHER.getMessage());
    }

    @Test
    @DisplayName("티켓팅 도중 취소 - 성공")
    void 티켓팅_도중_취소(){
        // given
        setupMockAuthentication("test@example.com", "USER");

        // when
        ResultResponse resultResponse = ticketService.cancelTicketing(ticketingCancelRequest);

        // then
        assertThat(resultResponse).isNotNull();
        assertThat(resultResponse.getCode()).isEqualTo(ResultCode.TICKETING_CANCEL_SUCCESS.getCode());
        assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.TICKETING_CANCEL_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("티켓팅 도중 취소 - 실패(선점되어 있지 않음)")
    void 티켓팅_도중_취소_실패_선점_X() {
        // given
        setupMockAuthentication("test@example.com", "USER");
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").delete();

        // when & then
        assertThatThrownBy(() -> ticketService.cancelTicketing(ticketingCancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SEAT_NOT_HELD.getMessage());
    }

    @Test
    @DisplayName("티켓팅 도중 취소 - 실패(선점 정보와 일치하지 않음)")
    void 티켓팅_도중_취소_실패_선점_정보_불일치() {
        // given
        setupMockAuthentication("test@example.com", "USER");
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").delete();
        redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").set("wrong@example.com", 7, TimeUnit.MINUTES);

        System.out.println("티켓팅_도중_취소_실패_선점_정보_불일치 현재 레디스 캐시 내용: "+
                redissonClient.getBucket(seatService.keyResolver(seatA.getId())+":heldBy").get());

        // when & then
        assertThatThrownBy(() -> ticketService.cancelTicketing(ticketingCancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SEAT_HELD_BY_OTHER.getMessage());
    }
}