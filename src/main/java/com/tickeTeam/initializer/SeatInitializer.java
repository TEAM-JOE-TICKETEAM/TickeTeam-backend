package com.tickeTeam.initializer;

import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.entity.SeatTemplate;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.seat.repository.SeatTemplateRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 실행 당일 기준 7일 이내의 경기들에 해당하는 좌석 정보 생성
 * 기존 생성해둔 926개의 좌석 정보(SeatTemplate) * 잠실 경기장 6경기
 * 총 5556개의 좌석 정보 생성
 */

@Order(3)
@Component
@RequiredArgsConstructor
@Profile("!test")
public class SeatInitializer implements ApplicationRunner {

    public static final int DAYS_TO_END = 7;

    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;
    private final SeatTemplateRepository seatTemplateRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(DAYS_TO_END);   // 7일 뒤

        List<Game> games = gameRepository.findByMatchDayBetween(today, end);
        List<SeatTemplate> seatTemplates = seatTemplateRepository.findAll();

        List<Seat> seats = generateSeatsForGames(games, seatTemplates);
        seatRepository.saveAll(seats);
    }

    private List<Seat> generateSeatsForGames(List<Game> games, List<SeatTemplate> templates) {
        List<Seat> seats = new ArrayList<>();
        for (Game game : games) {
            for (SeatTemplate template : templates) {
                seats.add(
                        Seat.builder()
                                .game(game)
                                .seatStadium(game.getStadium())
                                .seatTemplate(template)
                                .seatStatus(SeatStatus.AVAILABLE)
                                .build()
                );
            }
        }
        return seats;
    }
}
