package com.tickeTeam.domain.seat.entity;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.stadium.entity.Stadium;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_stadium", nullable = false)
    private Stadium seatStadium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_template", nullable = false)
    private SeatTemplate seatTemplate;

    @Column(name = "seat_status")
    @Enumerated(value = EnumType.STRING)
    private SeatStatus seatStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_member")
    private Member holdMember;

    public void seatHold(Member member){
        if (this.seatStatus == SeatStatus.HELD && this.holdMember != null && !this.holdMember.getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
        }
        if (this.seatStatus != SeatStatus.AVAILABLE){
            throw new BusinessException(ErrorCode.SEAT_CANNOT_BE_HELD);
        }

        this.seatStatus = SeatStatus.HELD;
        this.holdMember = member;
    }
}
