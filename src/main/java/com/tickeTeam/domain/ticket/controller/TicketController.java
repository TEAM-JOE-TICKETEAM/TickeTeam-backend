package com.tickeTeam.domain.ticket.controller;

import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.ticket.dto.request.TicketIssueRequest;
import com.tickeTeam.domain.ticket.dto.request.TicketingCancelRequest;
import com.tickeTeam.domain.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket")
@RequiredArgsConstructor
@Tag(name = "TicketController", description = "티켓팅 API")
public class TicketController {

    private final TicketService ticketService;

    @Operation(
            summary = "선택한 좌석(들)의 티켓을 발급받습니다.",
            description = "선택한 좌석(들)의 티켓이 발급되며, 발급된 티켓들은 하나의 Reservation 에 소속됩니다."
    )
    @PostMapping("/issue")
    public ResponseEntity<ResultResponse> issueTickets(@RequestBody TicketIssueRequest ticketIssueRequest){
        return ResponseEntity.ok(ticketService.issueTickets(ticketIssueRequest));
    }

    @Operation(
            summary = "티켓팅 중도 취소",
            description = "예매 시퀀스 도중 취소 요청을 처리합니다."
    )
    @PostMapping("/cancel")
    public ResponseEntity<ResultResponse> cancelTicketing(@RequestBody TicketingCancelRequest ticketingCancelRequest){
        return ResponseEntity.ok(ticketService.cancelTicketing(ticketingCancelRequest));
    }

    @Operation(
            summary = "예매를 취소합니다",
            description = "예매를 취소합니다. 이 경우 발급된 모든 티켓이 취소됩니다."
    )
    @DeleteMapping("/{reservationCode}")
    public ResponseEntity<ResultResponse> cancelReservation(@PathVariable("reservationCode") String reservationCode){
        return ResponseEntity.ok(ticketService.cancelReservation(reservationCode));
    }
}
