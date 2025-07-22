package com.tickeTeam.domain.seat.controller;

import com.tickeTeam.common.annotation.Trace;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.seat.dto.request.BlockSeatsRequest;
import com.tickeTeam.domain.seat.dto.request.SeatSelectRequest;
import com.tickeTeam.domain.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seat")
@RequiredArgsConstructor
@Tag(name = "SeatController", description = "좌석 API")
public class SeatController {

    private final SeatService seatService;

    @Operation(
            summary = "블록 좌석 조회",
            description = "특정 경기의 특정 블록 내 좌석 정보를 조회합니다."
    )
    @GetMapping("/detail/{gameId}")
    @Trace
    public ResponseEntity<ResultResponse> blockSeats(@PathVariable("gameId") Long gameId,
                                                     @RequestParam String seatSection,
                                                     @RequestParam String seatBlock){
        ResultResponse resultResponse = ResultResponse.of(ResultCode.GET_BLOCK_SEAT_SUCCESS,
                seatService.getBlockSeats(gameId, seatSection, seatBlock));
        return ResponseEntity.ok(resultResponse);
    }

    @Operation(
            summary = "경기 좌석 현황 조회",
            description = "특정 경기의 각 블록별 남은 예매 가능 좌석 수를 반환합니다."
    )
    @GetMapping("/{gameId}")
    @Trace
    public ResponseEntity<ResultResponse> gameSeats(@PathVariable("gameId") Long gameId){
        ResultResponse resultResponse = ResultResponse.of(ResultCode.GET_GAME_SEAT_SUCCESS,
                seatService.getGameSeats(gameId));
        return ResponseEntity.ok(resultResponse);
    }

    @Operation(
            summary = "좌석 선택(선점)",
            description = "선택한 좌석들을 선점합니다. 선점은 7분간 유지됩니다."
    )
    @PostMapping("/selection")
    public ResponseEntity<ResultResponse> selectSeats(@RequestBody SeatSelectRequest request){
        return ResponseEntity.ok(seatService.selectSeats(request));
    }
}
