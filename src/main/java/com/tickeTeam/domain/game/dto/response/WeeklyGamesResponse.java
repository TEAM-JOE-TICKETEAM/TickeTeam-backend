package com.tickeTeam.domain.game.dto.response;

import com.tickeTeam.domain.game.entity.Game;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WeeklyGamesResponse {

    private List<GameInfoResponse> games; // 실제 경기 정보 리스트
    private LocalDate startDate;          // 주간 조회 시작일
    private LocalDate endDate;            // 주간 조회 종료일
    private String team;                  // 조회 대상 응원팀
    private int totalGames;               // 조회된 총 경기 수


    public static WeeklyGamesResponse of(List<Game> matches, LocalDate weekStartDate, LocalDate weekEndDate, String teamName ){
        List<GameInfoResponse> gameInfoResponses = matches.stream()
                .map(GameInfoResponse::from)
                .toList();

        return new WeeklyGamesResponse(gameInfoResponses, weekStartDate, weekEndDate, teamName, gameInfoResponses.size());
    }
}
