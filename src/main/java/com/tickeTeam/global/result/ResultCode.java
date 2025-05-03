package com.tickeTeam.global.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // 도메인별로 구분

    // Member
    MEMBER_CREATE_SUCCESS("M001", "회원가입에 성공했습니다."),
    CHECK_EMAIL("M002", "이메일 중복입니다."),
    LOGIN_FAIL("M003", "로그인에 실패했습니다. 비밀번호를 확인해주세요."),
    LOGIN_SUCCESS("M004", "로그인 성공하였습니다."),

    // Game

    // Seat
    ;

    private final String code;
    private final String message;
}
