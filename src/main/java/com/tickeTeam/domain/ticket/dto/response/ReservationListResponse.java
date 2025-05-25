package com.tickeTeam.domain.ticket.dto.response;

import com.tickeTeam.domain.ticket.entity.Reservation;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationListResponse {

    List<ReservationInfoResponse> reservationInfos;

    public static ReservationListResponse from(List<Reservation> reservations){
        List<ReservationInfoResponse> reservationInfos = reservations.stream()
                .map(ReservationInfoResponse::from)
                .toList();

        return new ReservationListResponse(reservationInfos);
    }
}
