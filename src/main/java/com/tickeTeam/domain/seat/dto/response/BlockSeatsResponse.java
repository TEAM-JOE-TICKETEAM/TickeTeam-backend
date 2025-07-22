package com.tickeTeam.domain.seat.dto.response;

import com.tickeTeam.common.annotation.Trace;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockSeatsResponse implements Serializable {

    private Long gameId;

    private String stadium;

   private List<SeatInfoResponse> seats;

    @Trace
    public static BlockSeatsResponse of(List<SeatInfoResponse> seats, Long gameId, String stadium){
        if (seats.isEmpty()) return new BlockSeatsResponse(gameId, stadium, new ArrayList<>());
        return new BlockSeatsResponse(gameId, stadium, seats);
    }
}
