package com.tickeTeam.domain.seat.dto.response;

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

    private List<SeatSummaryResponse> seatSummaries;

    public static GameSeatsResponse of(List<SeatSummaryResponse> seatSummaries, Long gameId, String stadium){
        return new GameSeatsResponse(gameId, stadium, seatSummaries);
    }
}
