package com.tickeTeam.domain.member.controller;

import com.tickeTeam.domain.member.dto.request.MemberSignUpRequest;
import com.tickeTeam.domain.member.dto.request.MemberUpdateRequest;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.member.dto.request.MemberVerificationRequest;
import com.tickeTeam.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "MemberController", description = "사용자 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "회원 가입",
            description = "회원가입을 진행합니다"
    )
    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signUp(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        return ResponseEntity.ok(memberService.signUp(memberSignUpRequest));
    }

    @Operation(
            summary = "마이 페이지",
            description = "사용자 정보 및 예매 내역을 조회합니다."
    )
    @GetMapping("/mypage")
    public ResponseEntity<ResultResponse> myPage() {
        return ResponseEntity.ok(memberService.myPage());
    }

    @Operation(
            summary = "정보 수정",
            description = "사용자 정보를 수정합니다."
    )
    @PutMapping("/update")
    public ResponseEntity<ResultResponse> updateMember(@RequestBody MemberUpdateRequest memberUpdateRequest) {
        return ResponseEntity.ok(memberService.updateMember(memberUpdateRequest));
    }

    @Operation(
            summary = "예매자 검증",
            description = "예매를 시도하는 인원이 입력한 정보와 사용자 정보를 검증합니다."
    )
    @GetMapping("/verification")
    public ResponseEntity<ResultResponse> MemberVerification(@RequestBody MemberVerificationRequest memberVerificationRequest){
        return ResponseEntity.ok(memberService.memberVerification(memberVerificationRequest));
    }
}
