package com.tickeTeam.domain.member.service;

import com.tickeTeam.domain.member.dto.request.MemberUpdateRequest;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.common.result.ResultResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface MemberService {

    // 회원 가입
    ResultResponse signUp(MemberSignUpRequest userSignUpRequest);

    // 회원 정보 조회
    ResultResponse myPage(HttpServletRequest request);

    // 회원 정보 수정
    ResultResponse updateMember(MemberUpdateRequest memberUpdateRequest, HttpServletRequest request);
}
