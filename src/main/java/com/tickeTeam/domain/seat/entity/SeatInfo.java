package com.tickeTeam.domain.seat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class SeatInfo {

    @Column(name = "seat_type")
    private String seatType;

    @Column(name = "seat_section")
    private String seatSection;

    @Column(name = "seat_block")
    private String seatBlock;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_num")
    private int seatNum;
}
