package com.tickeTeam.domain.member.service;

import com.tickeTeam.domain.member.dto.response.LoginMemberResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.TokenTypes;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.member.dto.request.MemberSignInRequest;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.auth.jwt.JwtUtil;
import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    // 회원 가입
    @Transactional
    @Override
    public ResultResponse signUp(MemberSignUpRequest memberSignUpRequest) {

        // 이메일 중복 검사
        String email = memberSignUpRequest.getEmail();
        if (memberRepository.existsByEmail(email)) {
            return ResultResponse.of(ResultCode.CHECK_EMAIL, true);
        }

        // 응원 팀 설정
        Team favoriteTeam = teamRepository.findByTeamName(memberSignUpRequest.getFavoriteTeam()).orElseThrow();

        // 비밀번호 인코딩
        String hashedPassword = bCryptPasswordEncoder.encode(memberSignUpRequest.getPassword());

        // 실제 객체 저장
        Member newMember = memberRepository.save(Member.of(memberSignUpRequest, hashedPassword, favoriteTeam));

        return ResultResponse.of(ResultCode.MEMBER_CREATE_SUCCESS, newMember.getId());
    }

    // 로그인
    @Override
    public ResultResponse login(MemberSignInRequest memberSignInRequest, HttpServletResponse response) {

        // 멤버의 email을 아이디로 멤버 검사
        String email = memberSignInRequest.getEmail();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );

        // 패스워드 해싱 후 검사
        if (!bCryptPasswordEncoder.matches(memberSignInRequest.getPassword(), member.getPassword())) {
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
                LoginMemberResponse.builder()
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
