package com.tickeTeam.domain.member.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.domain.member.dto.request.MemberUpdateRequest;
import com.tickeTeam.domain.member.dto.response.MyPageResponse;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    public static final String ACCESS_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    // 회원 가입
    @Override
    @Transactional
    public ResultResponse signUp(MemberSignUpRequest memberSignUpRequest) {

        // 이메일 중복 검사
        String email = memberSignUpRequest.getEmail();
        if (memberRepository.existsByEmail(email)) {
            return ResultResponse.of(ResultCode.CHECK_EMAIL, true);
        }

        // 응원 팀 설정
        Team favoriteTeam = teamRepository.findByTeamName(memberSignUpRequest.getFavoriteTeam()).orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)
        );

        // 비밀번호 인코딩
        String hashedPassword = bCryptPasswordEncoder.encode(memberSignUpRequest.getPassword());

        // 실제 객체 저장
        Member newMember = memberRepository.save(Member.of(memberSignUpRequest, hashedPassword, favoriteTeam));

        return ResultResponse.of(ResultCode.MEMBER_CREATE_SUCCESS, newMember.getId());
    }

    // 회원 정보 조회
    @Override
    public ResultResponse myPage(HttpServletRequest request) {
        Member findMember = getMemberByRequest(request);

        return ResultResponse.of(ResultCode.MY_PAGE, MyPageResponse.from(findMember));
    }

    // 회원 정보 수정
    @Override
    @Transactional
    public ResultResponse updateMember(MemberUpdateRequest memberUpdateRequest, HttpServletRequest request) {
        Member findMember = getMemberByRequest(request);
        Team findTeam = teamRepository.findByTeamName(memberUpdateRequest.getFavoriteTeam()).orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)
        );

        findMember.update(memberUpdateRequest, findTeam);

        return ResultResponse.of(ResultCode.MEMBER_UPDATE_SUCCESS);
    }

    private Member getMemberByRequest(HttpServletRequest request) {
        // jwt 토큰으로부터 추출한 이메일로 사용자 조회
        String memberEmail = jwtUtil.getEmail(extractAccessToken(request));
        return memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }

    private String extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ACCESS_HEADER))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""))
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_ACCESS_NOT_EXIST));
    }

}
