package com.tickeTeam.domain.member.controller;

import com.tickeTeam.domain.member.dto.request.MemberSignInRequest;
import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.domain.member.service.MemberService;
import com.tickeTeam.global.result.ResultResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signUp(@RequestBody MemberSignUpRequest memberSignUpRequest){
        return ResponseEntity.ok(memberService.signUp(memberSignUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<ResultResponse> login(@RequestBody MemberSignInRequest memberSignInRequest,
                                                HttpServletResponse response) {
        return ResponseEntity.ok(memberService.login(memberSignInRequest, response));
    }
}
