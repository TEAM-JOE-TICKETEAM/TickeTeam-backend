package com.tickeTeam.global.exception;

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

    // Game
    GAME_DATA_INSERT_ERROR(500,"GA001", "game 데이터 삽입 중 오류 발생"),

    // Team
    TEAM_NOT_FOUND(404,"T001","팀 찾을 수 없음"),

    // Stadium
    STADIUM_NOT_FOUND(404,"ST001","경기장 찾을 수 없음"),

    // Seat

    // Member
    MEMBER_EMAIL_DUPLICATION_ERROR(400, "M001", "중복된 이메일"),
    MEMBER_NOT_FOUND(404, "M002", "해당 사용자를 찾을 수 없습니다.")
    // Reservation
    ;

    private final int status;
    private final String code;
    private final String message;
}
