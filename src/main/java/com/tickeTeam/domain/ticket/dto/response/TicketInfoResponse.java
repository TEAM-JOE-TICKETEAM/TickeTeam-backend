package com.tickeTeam.domain.ticket.dto.response;

import com.tickeTeam.domain.ticket.entity.Ticket;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TicketInfoResponse {

    private Long ticketId;

    private int ticketPrice;

    private LocalDateTime ticketIssuedAt;

    private String seatType;

    private String seatSection;

    private String seatBlock;

    private String seatRow;

    private Integer seatNum;

    public static TicketInfoResponse from(Ticket ticket){
        return TicketInfoResponse.builder()
                .ticketId(ticket.getId())
                .ticketPrice(ticket.getTicketPrice())
                .ticketIssuedAt(ticket.getIssuedAt())
                .seatType(ticket.getSeatInfo().getSeatType().name())
                .seatSection(ticket.getSeatInfo().getSeatSection())
                .seatBlock(ticket.getSeatInfo().getSeatBlock())
                .seatRow(ticket.getSeatInfo().getSeatRow())
                .seatNum(ticket.getSeatInfo().getSeatNum())
                .build();
    }
}
