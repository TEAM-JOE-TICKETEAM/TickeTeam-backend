package com.tickeTeam.domain.member.dto.response;

import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.ticket.dto.response.ReservationListResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyPageResponse {

    private final String name;

    private final String email;

    private final String favoriteTeam;

    private final ReservationListResponse reservationList;

    public static MyPageResponse from(Member member, ReservationListResponse reservationList) {
        return new MyPageResponse(
                member.getName(),
                member.getEmail(),
                member.getFavoriteTeam().getTeamName(),
                reservationList
        );
    }
}
