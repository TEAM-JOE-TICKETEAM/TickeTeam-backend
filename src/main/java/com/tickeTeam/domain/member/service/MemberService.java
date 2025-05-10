package com.tickeTeam.domain.member.service;

import com.tickeTeam.domain.member.dto.request.MemberSignInRequest;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.result.ResultResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberService {

    // 회원 가입
    ResultResponse signUp(MemberSignUpRequest userSignUpRequest);

    // 로그인
    ResultResponse login(MemberSignInRequest userSignInRequest, HttpServletResponse response);

}
