package com.tickeTeam.domain.seat.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatSelectRequest {

    private List<Long> seatIds;

    public static SeatSelectRequest of(List<Long> selectedSeatIds){
        return new SeatSelectRequest(selectedSeatIds);
    }
}
