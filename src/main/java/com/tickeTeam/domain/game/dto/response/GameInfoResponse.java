package com.tickeTeam.domain.game.dto.response;

import com.tickeTeam.domain.game.entity.Game;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GameInfoResponse {

    private Long gameId;

    private String homeTeamName;

    private String awayTeamName;

    private String stadiumName;

    private LocalDate matchDay;

    private LocalTime matchTime;

    public static GameInfoResponse from(Game game) {
        return new GameInfoResponse(
                game.getId(),
                game.getHomeTeam().getTeamName(),
                game.getAwayTeam().getTeamName(),
                game.getStadium().getStadiumName(),
                game.getMatchDay(),
                game.getMatchTime()
        );
    }
}
