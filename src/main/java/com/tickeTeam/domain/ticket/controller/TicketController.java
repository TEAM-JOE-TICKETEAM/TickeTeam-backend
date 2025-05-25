package com.tickeTeam.domain.ticket.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.ticket.dto.request.TicketIssueRequest;
import com.tickeTeam.domain.ticket.dto.request.TicketingCancelRequest;
import com.tickeTeam.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/issue")
    public ResponseEntity<ResultResponse> issueTickets(@RequestBody TicketIssueRequest ticketIssueRequest){
        return ResponseEntity.ok(ticketService.issueTickets(ticketIssueRequest));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ResultResponse> cancelTicketing(@RequestBody TicketingCancelRequest ticketingCancelRequest){
        return ResponseEntity.ok(ticketService.cancelTicketing(ticketingCancelRequest));
    }
}
