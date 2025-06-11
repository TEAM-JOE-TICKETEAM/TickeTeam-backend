package com.tickeTeam.domain.seat.dto.response;

import com.tickeTeam.domain.seat.entity.Seat;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GameSeatsResponse {

    private Long gameId;

    private String stadium;

    private List<SeatInfoResponse> seats;

    public static GameSeatsResponse of(List<Seat> seats, Long gameId, String stadium){
        if (seats.isEmpty()) return new GameSeatsResponse(gameId, stadium, new ArrayList<>());

        List<SeatInfoResponse> seatInfoList = seats.stream()
                .map(SeatInfoResponse::from)
                .toList();
        return new GameSeatsResponse(gameId, stadium, seatInfoList);
    }
}
