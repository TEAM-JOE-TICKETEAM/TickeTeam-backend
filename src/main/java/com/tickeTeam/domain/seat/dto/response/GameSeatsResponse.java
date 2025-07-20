package com.tickeTeam.domain.seat.dto.response;

import com.tickeTeam.common.annotation.Trace;
import com.tickeTeam.domain.seat.entity.Seat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GameSeatsResponse implements Serializable {

    private Long gameId;

    private String stadium;

   private List<SeatInfoResponse> seats;

    @Trace
    public static GameSeatsResponse of(List<SeatInfoResponse> seats, Long gameId, String stadium){
        if (seats.isEmpty()) return new GameSeatsResponse(gameId, stadium, new ArrayList<>());
        return new GameSeatsResponse(gameId, stadium, seats);
    }
}
