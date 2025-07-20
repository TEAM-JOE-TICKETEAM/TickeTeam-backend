package com.tickeTeam.domain.seat.dto.response;


import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.entity.SeatType;
import java.io.Serializable;

public record SeatInfoResponse(
        Long id,
        SeatType seatType,
        String seatSection,
        String seatBlock,
        String seatRow,
        Integer seatNum,
        SeatStatus seatStatus
) implements Serializable {}
