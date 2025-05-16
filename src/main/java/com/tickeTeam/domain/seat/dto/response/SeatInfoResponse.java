package com.tickeTeam.domain.seat.dto.response;

import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatInfo;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.entity.SeatType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatInfoResponse {

    private Long id;

    private SeatType seatType;

    private String seatSection;

    private String seatBlock;

    private String seatRow;

    private Integer seatNum;

    private SeatStatus seatStatus;

    public static SeatInfoResponse from(Seat seat) {
        SeatInfo seatInfo = seat.getSeatTemplate().getSeatInfo();

        return new SeatInfoResponse(
                seat.getId(),
                seatInfo.getSeatType(),
                seatInfo.getSeatSection(),
                seatInfo.getSeatBlock(),
                seatInfo.getSeatRow(),
                seatInfo.getSeatNum(),
                seat.getSeatStatus()
        );
    }
}
