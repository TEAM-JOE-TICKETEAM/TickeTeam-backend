package com.tickeTeam.infrastructure.security.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.MemberRole;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.entity.TokenTypes;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginRequest;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginResponse;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private HttpServletResponse response;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    LoginService loginService;

    private static final String BEARER_PREFIX = "Bearer ";

    private LoginRequest loginRequest;
    private Member loginMember;
    private Team testTeam;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        accessToken = "access_token";
        refreshToken = "refresh_token";

        testTeam = Team.of("기아 타이거즈");

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password1234")
                .build();

        loginMember = Member.builder()
                .id(1L)
                .name("tester")
                .email("test@example.com")
                .favoriteTeam(testTeam)
                .password("password1234")
                .role(MemberRole.USER)
                .build();
    }

    @Test
    @DisplayName("로그인 성공 - 토큰을 헤더에 담아 반환합니다.")
    void 로그인_성공() {
        // 준비
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(loginMember));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), loginMember.getPassword())).thenReturn(true);
        when(jwtUtil.createJwt(TokenTypes.ACCESS.getName(), loginMember.getEmail(), loginMember.getRole(), loginMember.getId()))
                .thenReturn(accessToken);
        when(jwtUtil.createJwt(TokenTypes.REFRESH.getName(), loginMember.getEmail(), loginMember.getRole(), loginMember.getId()))
                .thenReturn(refreshToken);

        // 실행
        ResultResponse loginResult = loginService.login(loginRequest, response);

        // 결과 검증
        assertThat(loginResult).isNotNull();
        assertThat(loginResult.getMessage()).isEqualTo(ResultCode.LOGIN_SUCCESS.getMessage());
        assertThat(loginResult.getCode()).isEqualTo(ResultCode.LOGIN_SUCCESS.getCode());

        LoginResponse loginResponse = (LoginResponse) loginResult.getData();
        assertThat(loginResponse.getName()).isEqualTo(loginMember.getName());
        assertThat(loginResponse.getEmail()).isEqualTo(loginMember.getEmail());
        assertThat(loginResponse.getFavoriteTeam()).isEqualTo(loginMember.getFavoriteTeam().getTeamName());

        // 의존성 메서드 호출 검증
        verify(memberRepository).findByEmail(loginRequest.getEmail());
        verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(), loginMember.getPassword());
        verify(jwtUtil).createJwt(TokenTypes.ACCESS.getName(), loginMember.getEmail(), loginMember.getRole(), loginMember.getId());
        verify(jwtUtil).createJwt(TokenTypes.REFRESH.getName(), loginMember.getEmail(), loginMember.getRole(), loginMember.getId());

        // 헤더 삽입 검증 및 쿠키 설정 검증
        verify(response).addHeader(TokenTypes.ACCESS.getType(), BEARER_PREFIX + accessToken);
        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
        String capturedCookie = cookieCaptor.getValue();

        // 쿠키 검증
        assertThat(capturedCookie).startsWith(TokenTypes.REFRESH.getType() + "=" + refreshToken);
        assertThat(capturedCookie).contains("HttpOnly");
        assertThat(capturedCookie).contains("Secure");
        assertThat(capturedCookie).contains("Path=/");
        assertThat(capturedCookie).contains("Max-Age=86400");
        assertThat(capturedCookie).contains("SameSite=None");
    }

    @Test
    @DisplayName("로그인 실패 - 입력한 이메일에 해당하는 사용자를 찾지 못했습니다.")
    void 로그인_실패_사용자_못찾음() {
        // 준비
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // 실행 & 검증
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                loginService.login(loginRequest, response);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        // 의존성 메서드들 검증
        verify(memberRepository).findByEmail(loginRequest.getEmail());

        // 실행되면 안되는 메서드들 검증
        verifyNoInteractions(bCryptPasswordEncoder);
        verifyNoInteractions(jwtUtil);
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 입력한 비밀번호가 맞지 않습니다.")
    void 로그인_실패_비밀번호_틀림() {
        // 준비
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(loginMember));
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(), loginMember.getPassword())).thenReturn(false);

        // 실행
        ResultResponse loginResult = loginService.login(loginRequest, response);

        // 검증
        assertThat(loginResult).isNotNull();
        assertThat(loginResult.getMessage()).isEqualTo(ResultCode.LOGIN_FAIL.getMessage());
        assertThat(loginResult.getCode()).isEqualTo(ResultCode.LOGIN_FAIL.getCode());

        // 의존성 메서드 검증
        verify(memberRepository).findByEmail(loginRequest.getEmail());
        verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(), loginMember.getPassword());

        // 실행되면 안되는 메서드들 검증
        verifyNoInteractions(jwtUtil);
        verify(response, never()).addHeader(anyString(), anyString());
    }
}