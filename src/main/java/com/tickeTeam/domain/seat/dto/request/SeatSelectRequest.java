package com.tickeTeam.domain.seat.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SeatSelectRequest {

    private List<Long> seatIds;

    public static SeatSelectRequest of(List<Long> selectedSeatIds){
        return new SeatSelectRequest(selectedSeatIds);
    }
}
