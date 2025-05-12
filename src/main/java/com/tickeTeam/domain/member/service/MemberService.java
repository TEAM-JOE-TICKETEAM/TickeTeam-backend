package com.tickeTeam.domain.member.service;

import com.tickeTeam.infrastructure.security.authentication.dto.LoginRequest;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.result.ResultResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberService {

    // 회원 가입
    ResultResponse signUp(MemberSignUpRequest userSignUpRequest);

    // 회원 정보 조회
    // 회원 정보 수정
    // 응원팀 변경
}
