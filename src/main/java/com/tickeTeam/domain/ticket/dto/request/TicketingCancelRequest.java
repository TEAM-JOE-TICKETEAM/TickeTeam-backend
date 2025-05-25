package com.tickeTeam.domain.ticket.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TicketingCancelRequest {

    private List<Long> seatIds;

}
