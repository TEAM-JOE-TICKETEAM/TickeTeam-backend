package com.tickeTeam.domain.seat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(name = "seat_section", nullable = false)
    private String seatSection;

    @Column(name = "seat_block")
    private String seatBlock;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_num")
    private Integer seatNum;

    @Builder
    public SeatInfo(SeatType seatType, String seatSection, String seatBlock, String seatRow, Integer seatNum) {
        this.seatType = seatType;
        this.seatSection = seatSection;
        this.seatBlock = seatBlock;
        this.seatRow = seatRow;
        this.seatNum = seatNum;
    }
}
