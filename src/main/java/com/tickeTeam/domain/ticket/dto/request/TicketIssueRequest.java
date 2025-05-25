package com.tickeTeam.domain.ticket.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TicketIssueRequest {

    private List<Long> seatIds;
    private Long gameId;
}
