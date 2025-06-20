package com.tickeTeam.initializer;

import com.opencsv.CSVReader;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;

@RequiredArgsConstructor
public class GameInitializer implements DataInitializer {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Override
    public void run(){

        // CSV 파일 경로
        ClassPathResource resource = new ClassPathResource("data/matches.csv");

        List<Game> games = new ArrayList<>();

        try(InputStream in = resource.getInputStream();
            CSVReader csvReader = new CSVReader(new InputStreamReader(in))){

            List<String[]> rows = csvReader.readAll();

            // 팀 및 경기장 데이터를 미리 조회해두기 (성능 최적화)
            Map<String, Long> teamMap = teamRepository.findAll().stream()
                    .collect(Collectors.toMap(Team::getTeamName, Team::getId));

            Map<String, Long> stadiumMap = stadiumRepository.findAll().stream()
                    .collect(Collectors.toMap(Stadium::getStadiumName, Stadium::getId));

            // 각 행을 읽어서 Game 엔티티로 변환
            for (String[] row : rows) {
                String matchDate = row[0];
                String homeTeamName = row[1];
                String awayTeamName = row[2];
                String stadiumName = row[3];
                String matchTime = row[4];

                // 팀 ID와 경기장 ID 찾기
                Long homeTeamId = teamMap.get(homeTeamName);
                Long awayTeamId = teamMap.get(awayTeamName);
                Long stadiumId = stadiumMap.get(stadiumName);

                if (homeTeamId != null && awayTeamId != null && stadiumId != null) {
                    // Game 엔티티 생성
                    Game game = Game.builder()
                            .homeTeam(teamRepository.findById(homeTeamId).orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)))
                            .awayTeam(teamRepository.findById(awayTeamId).orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND)))
                            .stadium(stadiumRepository.findById(stadiumId).orElseThrow(() -> new NotFoundException(ErrorCode.STADIUM_NOT_FOUND)))
                            .matchDay(LocalDate.parse(matchDate))
                            .matchTime(LocalTime.parse(matchTime))
                            .build();

                    // 리스트에 추가
                    games.add(game);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GAME_DATA_INSERT_ERROR);
        }

        gameRepository.saveAll(games);
    }
}
