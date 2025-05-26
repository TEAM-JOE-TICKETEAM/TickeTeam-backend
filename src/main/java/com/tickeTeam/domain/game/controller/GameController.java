package com.tickeTeam.domain.game.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@Tag(name = "GameController", description = "경기 일정 API")

public class GameController {

    private final GameService gameService;

    @Operation(
            summary = "일정 조회",
            description = "사용자가 선택한 응원팀의 일주일 뒤 일정까지 조회합니다."
    )
    @GetMapping("/upcoming")
    public ResponseEntity<ResultResponse> getGamesInNextSevenDays(){
        return ResponseEntity.ok(gameService.getGamesInNextSevenDays());
    }
}
