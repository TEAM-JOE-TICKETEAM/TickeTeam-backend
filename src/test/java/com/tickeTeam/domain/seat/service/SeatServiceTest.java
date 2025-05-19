package com.tickeTeam.domain.seat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.seat.dto.request.SeatSelectRequest;
import com.tickeTeam.domain.seat.dto.response.GameSeatsResponse;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Game mockGame;

    @Mock
    private GameSeatsResponse mockGameSeatsResponse;

    @InjectMocks
    private SeatService seatService;

    private final Long testGameId = 1L;
    private final String testEmail = "test@example.com";
    private final SeatSelectRequest mockSeatSelectRequest = SeatSelectRequest.of(new ArrayList<>(List.of(1L, 2L, 3L)));
    private List<Seat> mockSeats;
    private final Member mockMember = Member.builder().email(testEmail).build();;
    @BeforeEach
    void setup(){
        mockGame = Game.builder()
                .id(1L)
                .stadium(Stadium.of("잠실 야구장"))
                .build();

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

    @AfterEach
    void tearDown() {
        // 각 테스트 후 SecurityContextHolder를 정리하여 테스트 간 독립성 보장
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("경기 좌석 조회 성공 - 해당 경기에 해당하는 좌석 목록을 반환합니다.")
    void 경기_좌석_조회_성공(){
        try(MockedStatic<GameSeatsResponse> mockedResponse = mockStatic(GameSeatsResponse.class)) {
            // 준비
            when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockMember));
            when(gameRepository.findById(testGameId)).thenReturn(Optional.of(mockGame));
            mockSeats = List.of(mock(Seat.class), mock(Seat.class));
            when(seatRepository.findAllByGame(mockGame)).thenReturn(mockSeats);

            mockedResponse.when(() -> GameSeatsResponse.of(
                    eq(mockSeats), eq(testGameId), eq("잠실 야구장")
            )).thenReturn(mockGameSeatsResponse);

            // 실행
            ResultResponse resultResponse = seatService.getGameSeats(testGameId);

            // 검증
            assertThat(resultResponse).isNotNull();
            assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getMessage());
            assertThat(resultResponse.getCode()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getCode());
            assertThat(resultResponse.getData()).isEqualTo(mockGameSeatsResponse);

            verify(gameRepository).findById(testGameId);
            verify(seatRepository).findAllByGame(mockGame);
            mockedResponse.verify(() -> GameSeatsResponse.of(
                    mockSeats, testGameId, "잠실 야구장"
            ));
        }
    }

    @Test
    @DisplayName("경기 좌석 조회 성공 - 해당 경기에 해당하는 좌석이 없으면 빈 리스트를 반환합니다.")
    void 경기_좌석_조회_성공_좌석_없음(){
        try(MockedStatic<GameSeatsResponse> mockedResponse = mockStatic(GameSeatsResponse.class)) {
            // 준비
            when(gameRepository.findById(testGameId)).thenReturn(Optional.of(mockGame));

            mockSeats = Collections.emptyList();
            when(seatRepository.findAllByGame(mockGame)).thenReturn(mockSeats);

            mockedResponse.when(() -> GameSeatsResponse.of(
                    eq(mockSeats), eq(testGameId), eq("잠실 야구장")
            )).thenReturn(mockGameSeatsResponse);
            when(mockGameSeatsResponse.getSeats()).thenReturn(Collections.emptyList());

            // 실행
            ResultResponse resultResponse = seatService.getGameSeats(testGameId);

            // 검증
            assertThat(resultResponse).isNotNull();
            assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getMessage());
            assertThat(resultResponse.getCode()).isEqualTo(ResultCode.GET_GAME_SEAT_SUCCESS.getCode());
            assertThat(resultResponse.getData()).isEqualTo(mockGameSeatsResponse);

            GameSeatsResponse gameSeatsResponse = (GameSeatsResponse) resultResponse.getData();
            assertThat(gameSeatsResponse.getSeats()).isEqualTo(Collections.emptyList());

            verify(gameRepository).findById(testGameId);
            verify(seatRepository).findAllByGame(mockGame);
            mockedResponse.verify(() -> GameSeatsResponse.of(
                    mockSeats, testGameId, "잠실 야구장"
            ));
        }
    }

    @Test
    @DisplayName("경기 좌석 조회 실패 - ID에 해당하는 경기를 찾지 못함")
    void 경기_좌석_조회_실패_경기_못찾음(){
        // 준비
        when(gameRepository.findById(testGameId)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> seatService.getGameSeats(testGameId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.MATCH_NOT_FOUND.getMessage());

        verify(gameRepository).findById(testGameId);
        verifyNoInteractions(seatRepository);
    }

    @Test
    @DisplayName("좌석 선점 성공, 단일 스레드")
    void 좌석_선점_성공(){
        setupMockAuthentication(testEmail, "USER");

        // 준비
        mockSeats = List.of(mock(Seat.class), mock(Seat.class), mock(Seat.class));
        when(seatRepository.findAllByIdIn(new ArrayList<>(List.of(1L, 2L, 3L)))).thenReturn(mockSeats);
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockMember));

        // 실행
        ResultResponse resultResponse = seatService.selectSeats(mockSeatSelectRequest);

        assertThat(resultResponse).isNotNull();
        assertThat(resultResponse.getCode()).isEqualTo(ResultCode.SEATS_SELECT_SUCCESS);
        assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.SEATS_SELECT_SUCCESS.getMessage());

        // 검증
        verify(memberRepository).findByEmail(testEmail);
        verify(seatRepository).findAllByIdIn(List.of(1L,2L,3L));
    }
}