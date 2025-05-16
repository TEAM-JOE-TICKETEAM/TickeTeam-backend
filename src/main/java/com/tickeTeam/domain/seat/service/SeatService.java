package com.tickeTeam.domain.seat.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.seat.dto.response.GameSeatsResponse;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;

    // 좌석 정보 조회
    public ResultResponse getGameSeats(Long gameId) {
        Game findGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND));
        List<Seat> seats = seatRepository.findAllByGame(findGame);
        return ResultResponse.of(ResultCode.GET_GAME_SEAT_SUCCESS,
                GameSeatsResponse.of(seats, gameId, findGame.getStadium().getStadiumName()));
    }
}
