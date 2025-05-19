package com.tickeTeam.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // 도메인별로 구분

    // Member
    MEMBER_CREATE_SUCCESS("M001", "회원가입에 성공했습니다."),
    CHECK_EMAIL("M002", "이메일 중복입니다."),
    LOGIN_FAIL("M003", "로그인에 실패했습니다. 비밀번호를 확인해주세요."),
    LOGIN_SUCCESS("M004", "로그인 성공하였습니다."),
    MY_PAGE("M005", "마이페이지 조회에 성공했습니다."),
    MEMBER_UPDATE_SUCCESS("M006", "회원 정보 수정에 성공했습니다."),
    MEMBER_VERIFICATION_SUCCESS("M007", "예약자 정보 확인에 성공했습니다."),

    // Game
    GET_WEEKLY_GAME_SUCCESS("G001", "일주일 이내 경기 목록 조회에 성공했습니다."),

    // Seat
    GET_GAME_SEAT_SUCCESS("S001", "경기 좌석 조회에 성공했습니다."),
    SEATS_SELECT_SUCCESS("S002", "좌석 선점에 성공했습니다."),
    ;

    private final String code;
    private final String message;
}
