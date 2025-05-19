package com.tickeTeam.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 도메인별로 구분

    // Global
    INTERNAL_SERVER_ERROR(500, "G001", "서버 오류"),
    INPUT_INVALID_VALUE(409, "G002", "잘못된 입력"),
    ACCESS_INVALID_VALUE(400, "G003", "잘못된 접근"),
    AUTHENTICATION_NOT_FOUND(404, "G004", "로그인 사용자 정보 찾을 수 없음"),

    // Game
    GAME_DATA_INSERT_ERROR(500,"GA001", "game 데이터 삽입 중 오류 발생"),
    MATCH_NOT_FOUND(404, "GA002", "해당 경기를 찾을 수 없음"),

    // Team
    TEAM_NOT_FOUND(404,"T001","팀 찾을 수 없음"),

    // Stadium
    STADIUM_NOT_FOUND(404,"ST001","경기장 찾을 수 없음"),

    // Seat
    SEAT_ALREADY_HELD(400, "S001", "이미 예약되어있는 좌석"),
    SEAT_CANNOT_BE_HELD(400, "S002","해당 좌석을 선점할 수 없습니다."),
    SEAT_LIMIT_OVER(400, "S002","인당 좌석은 최대 4석까지만 선택 가능합니다."),

    // SectionPrice
    SECTION_PRICE_NOT_FOUND(404, "SE001", "해당 좌석의 구역 가격을 찾을 수 없습니다."),

    // Member
    MEMBER_EMAIL_DUPLICATION_ERROR(400, "M001", "중복된 이메일"),
    MEMBER_NOT_FOUND(404, "M002", "해당 사용자를 찾을 수 없습니다."),
    MEMBER_VERIFICATION_FAIL(403, "M003","예약자 확인 실패"),

    // Reservation
    ;

    private final int status;
    private final String code;
    private final String message;
}
