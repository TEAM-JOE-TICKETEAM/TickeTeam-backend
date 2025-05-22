package com.tickeTeam.domain.ticket.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import com.tickeTeam.domain.ticket.dto.request.TicketIssueRequest;
import com.tickeTeam.domain.ticket.dto.response.ReservationInfoResponse;
import com.tickeTeam.domain.ticket.entity.Reservation;
import com.tickeTeam.domain.ticket.entity.Ticket;
import com.tickeTeam.domain.ticket.repository.ReservationRepository;
import com.tickeTeam.domain.ticket.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final SectionPriceRepository sectionPriceRepository;
    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public ResultResponse issueTickets(TicketIssueRequest ticketIssueRequest) {
        Member member = getMemberByAuthentication();

        List<Seat> seats = seatRepository.findByIdIn(ticketIssueRequest.getSeatIds());

        checkIsHold(seats, member); // 각 좌석들이 해당 사용자에게 선점된 좌석이 맞는지 확인

        Game targetMatch = gameRepository.findById(ticketIssueRequest.getGameId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND));

        Reservation newReservation = getNewReservation(targetMatch, member);

        for (Seat seat : seats) {
            SectionPrice sectionPrice = sectionPriceRepository.findBySeatSection(seat.getSeatTemplate().getSeatInfo().getSeatSection());

            Ticket newTicket = Ticket.builder()
                    .seatInfo(seat.getSeatTemplate().getSeatInfo())
                    .reservation(newReservation)
                    .ticketPrice(sectionPrice.getSectionPrice(targetMatch.getMatchDay()))
                    .issuedAt(LocalDateTime.now())
                    .build();

            newReservation.addTicket(ticketRepository.save(newTicket));

            // 티켓이 발행된 좌석은 RESERVED로 상태 변경
            seat.seatReserve();

            // Redis에서 선점 정보 삭제
            String holdKey = "seat:" + seat.getId() + ":heldBy";
            redissonClient.getBucket(holdKey).delete();
        }

        return ResultResponse.of(ResultCode.TICKET_ISSUE_SUCCESS, ReservationInfoResponse.from(newReservation, member, targetMatch));
    }

    private void checkIsHold(List<Seat> seats, Member member) {
        for (Seat seat : seats) {
            String holdKey = "seat:" + seat.getId() + ":heldBy";
            String holderId = (String) redissonClient.getBucket(holdKey).get();
            if (holderId == null) {
                throw new BusinessException(ErrorCode.SEAT_NOT_HELD);
            }
            if (!holderId.equals(member.getEmail())) {
                throw new BusinessException(ErrorCode.SEAT_HELD_BY_OTHER);
            }
        }
    }

    private Reservation getNewReservation(Game targetMatch, Member member) {
        Reservation newReservation = Reservation.builder()
                .reservedGame(targetMatch)
                .reservedMember(member)
                .reservedAt(LocalDateTime.now())
                .reservationCode(UUID.randomUUID().toString().substring(0,10))
                .build();

        reservationRepository.save(newReservation);
        return newReservation;
    }

    private Member getMemberByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }

        String memberEmail = authentication.getName();
        return memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }
}
