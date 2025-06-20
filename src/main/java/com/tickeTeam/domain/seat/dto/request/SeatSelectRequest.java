package com.tickeTeam.domain.seat.dto.request;

import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SeatSelectRequest implements Serializable {

    private Long gameId;
    private List<Long> seatIds;

    public static SeatSelectRequest of(Long gameId, List<Long> selectedSeatIds){
        return new SeatSelectRequest(gameId, selectedSeatIds);
    }
}
