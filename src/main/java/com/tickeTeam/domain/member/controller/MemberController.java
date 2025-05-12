package com.tickeTeam.domain.member.controller;

import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.domain.member.service.MemberService;
import com.tickeTeam.common.result.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signUp(@RequestBody MemberSignUpRequest memberSignUpRequest){
        return ResponseEntity.ok(memberService.signUp(memberSignUpRequest));
    }
}
