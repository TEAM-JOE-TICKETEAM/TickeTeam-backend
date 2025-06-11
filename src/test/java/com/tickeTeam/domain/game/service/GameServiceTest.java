package com.tickeTeam.domain.game.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import com.tickeTeam.domain.game.dto.response.WeeklyGamesResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Team mockTeam;

    @Mock
    private Member mockMember;

    @Mock
    private WeeklyGamesResponse mockWeeklyGamesResponse;

    @InjectMocks
    GameService gameService;

    private final String testEmail = "test@example.com";
    private final LocalDate fixedCurrentDate = LocalDate.of(2025, 5, 16);
    private final LocalDate endDate = fixedCurrentDate.plusDays(7);
    private List<Game> mockGameList;

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
    @DisplayName("현재 날짜 기준 7일 이내의 응원팀 경기 조회 성공")
    void 응원팀_경기_조회_성공() {
        setupMockAuthentication(testEmail, "USER");

        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class);
             MockedStatic<WeeklyGamesResponse> mockedResponse = mockStatic(WeeklyGamesResponse.class)) {

            // 준비
            mockedDate.when(LocalDate::now).thenReturn(fixedCurrentDate);

            when(memberRepository.findByEmailWithTeam(testEmail)).thenReturn(Optional.of(mockMember));
            when(mockMember.getFavoriteTeam()).thenReturn(mockTeam);
            mockGameList = List.of(mock(Game.class), mock(Game.class));
            when(gameRepository.findGamesByTeamAndDateRange(fixedCurrentDate, endDate, mockTeam))
                    .thenReturn(mockGameList);
            when(mockTeam.getTeamName()).thenReturn("기아 타이거즈");
            mockedResponse.when(() -> WeeklyGamesResponse.of(
                    eq(mockGameList), eq(fixedCurrentDate), eq(endDate), eq("기아 타이거즈")
            )).thenReturn(mockWeeklyGamesResponse);

            // 실행
            ResultResponse resultResponse = gameService.getGamesInNextSevenDays();

            // 검증
            assertThat(resultResponse).isNotNull();
            assertThat(resultResponse.getCode()).isEqualTo(ResultCode.GET_WEEKLY_GAME_SUCCESS.getCode());
            assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.GET_WEEKLY_GAME_SUCCESS.getMessage());
            assertThat(resultResponse.getData()).isEqualTo(mockWeeklyGamesResponse);

            verify(memberRepository).findByEmailWithTeam(testEmail);
            verify(mockMember).getFavoriteTeam();
            verify(gameRepository).findGamesByTeamAndDateRange(fixedCurrentDate, endDate, mockTeam);
            mockedResponse.verify(() -> WeeklyGamesResponse.of(
                    mockGameList, fixedCurrentDate, endDate, "기아 타이거즈"
            ));
        }
    }

    @Test
    @DisplayName("경기 목록 조회 - 7일 이내 경기가 존재하지 않으면 빈 리스트를 반환합니다")
    void 경기_조회_성공_경기_없음() {
        setupMockAuthentication(testEmail, "USER");

        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class);
             MockedStatic<WeeklyGamesResponse> mockedResponse = mockStatic(WeeklyGamesResponse.class)) {

            // 준비
            mockedDate.when(LocalDate::now).thenReturn(fixedCurrentDate);

            when(memberRepository.findByEmailWithTeam(testEmail)).thenReturn(Optional.of(mockMember));
            when(mockMember.getFavoriteTeam()).thenReturn(mockTeam);

            mockGameList = Collections.emptyList();
            when(gameRepository.findGamesByTeamAndDateRange(fixedCurrentDate, endDate, mockTeam))
                    .thenReturn(mockGameList);

            when(mockTeam.getTeamName()).thenReturn("기아 타이거즈");
            mockedResponse.when(() -> WeeklyGamesResponse.of(
                    eq(mockGameList), eq(fixedCurrentDate), eq(endDate), eq("기아 타이거즈")
            )).thenReturn(mockWeeklyGamesResponse);
            when(mockWeeklyGamesResponse.getGames()).thenReturn(Collections.emptyList());

            // 실행
            ResultResponse resultResponse = gameService.getGamesInNextSevenDays();

            // 검증
            assertThat(resultResponse).isNotNull();
            assertThat(resultResponse.getCode()).isEqualTo(ResultCode.GET_WEEKLY_GAME_SUCCESS.getCode());
            assertThat(resultResponse.getMessage()).isEqualTo(ResultCode.GET_WEEKLY_GAME_SUCCESS.getMessage());
            assertThat(resultResponse.getData()).isEqualTo(mockWeeklyGamesResponse);

            WeeklyGamesResponse resultResponseData = (WeeklyGamesResponse) resultResponse.getData();
            assertThat(resultResponseData.getGames()).isEqualTo(Collections.emptyList()); // 빈 리스트를 반환하는지 검증

            verify(memberRepository).findByEmailWithTeam(testEmail);
            verify(mockMember).getFavoriteTeam();
            verify(gameRepository).findGamesByTeamAndDateRange(fixedCurrentDate, endDate, mockTeam);
            mockedResponse.verify(() -> WeeklyGamesResponse.of(
                    mockGameList, fixedCurrentDate, endDate, "기아 타이거즈"
            ));
        }
    }

    @Test
    @DisplayName("경기 조회 실패 - 사용자 정보 없음")
    void 경기_조회_실패_사용자_못찾음() {
        setupMockAuthentication(testEmail, "USER");

        // 준비
        when(memberRepository.findByEmailWithTeam(testEmail)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> gameService.getGamesInNextSevenDays())
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());

        verify(memberRepository).findByEmailWithTeam(testEmail);
        verifyNoInteractions(gameRepository); // 사용자 조회 실패 시 게임 조회 로직은 실행 안됨
    }

    @Test
    @DisplayName("경기 조회 실패 - 인증 정보 없음")
    void 경기_조회_실패_인증_정보_없음() {
        // 실행 & 검증
        assertThatThrownBy(() -> gameService.getGamesInNextSevenDays())
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.AUTHENTICATION_NOT_FOUND.getMessage());
    }
}