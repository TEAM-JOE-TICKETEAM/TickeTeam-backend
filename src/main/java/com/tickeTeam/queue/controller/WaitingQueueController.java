package com.tickeTeam.queue.controller;

import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import com.tickeTeam.infrastructure.security.userdetails.UserDetailsDto;
import com.tickeTeam.queue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @GetMapping("/rank")
    public ResponseEntity<Long> getRank(@AuthenticationPrincipal UserDetailsDto userDetails){
        String memberId = userDetails.getIdentity().toString();
        Long rank = waitingQueueService.getRank(memberId).block();

        if (rank == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(rank);
    }
}
