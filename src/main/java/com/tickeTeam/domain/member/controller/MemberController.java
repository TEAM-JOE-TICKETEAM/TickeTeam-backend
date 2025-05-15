package com.tickeTeam.domain.member.controller;

import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.domain.member.dto.request.MemberUpdateRequest;
import com.tickeTeam.domain.member.service.MemberService;
import com.tickeTeam.common.result.ResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signUp(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        return ResponseEntity.ok(memberService.signUp(memberSignUpRequest));
    }

    @GetMapping("/mypage")
    public ResponseEntity<ResultResponse> myPage(HttpServletRequest request) {
        return ResponseEntity.ok(memberService.myPage(request));
    }

    @PutMapping("/update")
    public ResponseEntity<ResultResponse> updateMember(HttpServletRequest request,
                                                       @RequestBody MemberUpdateRequest memberUpdateRequest) {
        return ResponseEntity.ok(memberService.updateMember(memberUpdateRequest, request));
    }
}
