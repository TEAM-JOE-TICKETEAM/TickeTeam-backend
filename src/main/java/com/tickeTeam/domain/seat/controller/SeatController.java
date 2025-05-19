package com.tickeTeam.domain.seat.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.seat.dto.request.SeatSelectRequest;
import com.tickeTeam.domain.seat.service.SeatService;
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
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/{gameId}")
    public ResponseEntity<ResultResponse> gameSeats(@PathVariable("gameId") Long gameId){
        return ResponseEntity.ok(seatService.getGameSeats(gameId));
    }

    @PostMapping("/selection")
    public ResponseEntity<ResultResponse> selectSeats(@RequestBody SeatSelectRequest request){
        return ResponseEntity.ok(seatService.selectSeats(request));
    }
}
