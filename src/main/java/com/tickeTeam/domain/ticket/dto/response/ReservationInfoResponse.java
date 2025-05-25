package com.tickeTeam.domain.ticket.dto.response;

import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.seat.dto.response.SeatInfoResponse;
import com.tickeTeam.domain.ticket.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationInfoResponse {

    private String reservationCode;

    private String memberName;

    private String stadium;

    private String homeTeam;

    private String awayTeam;

    private LocalDate matchDay;

    private LocalTime matchTime;

    private List<TicketInfoResponse> ticketInfos;

    public static ReservationInfoResponse from(Reservation reservation){
        List<TicketInfoResponse> ticketInfoResponses = reservation.getTickets().stream()
                .map(TicketInfoResponse::from)
                .toList();
        return ReservationInfoResponse.builder()
                .reservationCode(reservation.getReservationCode())
                .memberName(reservation.getReservedMember().getName())
                .stadium(reservation.getReservedGame().getStadium().getStadiumName())
                .homeTeam(reservation.getReservedGame().getHomeTeam().getTeamName())
                .awayTeam(reservation.getReservedGame().getAwayTeam().getTeamName())
                .matchDay(reservation.getReservedGame().getMatchDay())
                .matchTime(reservation.getReservedGame().getMatchTime())
                .ticketInfos(ticketInfoResponses)
                .build();
    }
}
