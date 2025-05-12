package com.tickeTeam.domain.member.service;

import com.tickeTeam.infrastructure.security.authentication.dto.LoginResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.TokenTypes;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.infrastructure.security.authentication.dto.LoginRequest;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
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
}
