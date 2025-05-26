package com.tickeTeam.domain.seat.controller;

import com.tickeTeam.common.result.ResultResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seat")
@RequiredArgsConstructor
@Tag(name = "SeatController", description = "좌석 API")
public class SeatController {

    private final SeatService seatService;

    @Operation(
            summary = "경기 좌석 조회",
            description = "특정 경기의 좌석 정보를 조회합니다."
    )
    @GetMapping("/{gameId}")
    public ResponseEntity<ResultResponse> gameSeats(@PathVariable("gameId") Long gameId){
        return ResponseEntity.ok(seatService.getGameSeats(gameId));
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
