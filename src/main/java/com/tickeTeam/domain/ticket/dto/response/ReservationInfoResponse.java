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

    public static ReservationInfoResponse from(Reservation reservation, Member member, Game game){
        List<TicketInfoResponse> ticketInfoResponses = reservation.getTickets().stream()
                .map(TicketInfoResponse::from)
                .toList();
        return ReservationInfoResponse.builder()
                .reservationCode(reservation.getReservationCode())
                .memberName(member.getName())
                .stadium(game.getStadium().getStadiumName())
                .homeTeam(game.getHomeTeam().getTeamName())
                .awayTeam(game.getAwayTeam().getTeamName())
                .matchDay(game.getMatchDay())
                .matchTime(game.getMatchTime())
                .ticketInfos(ticketInfoResponses)
                .build();
    }
}
