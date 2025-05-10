package com.tickeTeam.domain.seat.entity;

public enum SeatStatus {
    AVAILABLE, // 예매 가능
    HELD, // 선점됨(7분간 유효)
    RESERVED, // 예매 완료
}
