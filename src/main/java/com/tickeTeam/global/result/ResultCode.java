package com.tickeTeam.global.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // 도메인별로 구분

    // Game

    // Seat
    ;

    private final String code;
    private final String message;
}
