package com.tickeTeam.infrastructure.security.authentication;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.TokenTypes;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginRequest;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginResponse;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    // 로그인
    public ResultResponse login(LoginRequest loginRequest, HttpServletResponse response) {

        // 멤버의 email을 아이디로 멤버 검사
        String email = loginRequest.getEmail();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );

        // 패스워드 해싱 후 검사
        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            return ResultResponse.of(ResultCode.LOGIN_FAIL);
        }

        // Access Token, Refresh Token 생성
        String access = jwtUtil.createJwt(TokenTypes.ACCESS.getName(), member.getEmail(), member.getRole(),
                member.getId());
        String refresh = jwtUtil.createJwt(TokenTypes.REFRESH.getName(), member.getEmail(),
                member.getRole(), member.getId());

        // 발급된 두 토큰 헤더에 추가
        response.addHeader(TokenTypes.ACCESS.getType(), "Bearer " + access);
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(refresh).toString());

        return ResultResponse.of(ResultCode.LOGIN_SUCCESS,
                LoginResponse.builder()
                        .name(member.getName())
                        .email(member.getEmail())
                        .favoriteTeam(member.getFavoriteTeam().getTeamName())
                        .build());
    }

    private ResponseCookie createCookie(String value) {
        return ResponseCookie.from(TokenTypes.REFRESH.getType(), value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("None")
                .build();
    }
}
