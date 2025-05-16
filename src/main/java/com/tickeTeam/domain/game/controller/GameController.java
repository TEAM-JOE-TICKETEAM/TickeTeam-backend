package com.tickeTeam.domain.game.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/upcoming")
    public ResponseEntity<ResultResponse> getGamesInNextSevenDays(HttpServletRequest request){
        return ResponseEntity.ok(gameService.getGamesInNextSevenDays(request));
    }
}
