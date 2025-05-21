package com.tickeTeam.domain.ticket.dto.request;

import java.util.List;
import lombok.Getter;

@Getter
public class TicketIssueRequest {

    private List<Long> seatIds;
    private Long gameId;
}
