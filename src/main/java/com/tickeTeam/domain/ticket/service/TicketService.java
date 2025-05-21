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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final SectionPriceRepository sectionPriceRepository;
    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    public ResultResponse issueTickets(TicketIssueRequest ticketIssueRequest) {
        Member member = getMemberByAuthentication();

        List<Seat> seats = seatRepository.findByIdIn(ticketIssueRequest.getSeatIds());

        // 티켓 발급 요청 멤버와 좌석을 선점한 멤버가 동일인물인지 확인
        checkHoldMember(seats, member);

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

            seat.seatReserve();
        }

        return ResultResponse.of(ResultCode.TICKET_ISSUE_SUCCESS, ReservationInfoResponse.from(newReservation, member, targetMatch));
    }

    private static void checkHoldMember(List<Seat> seats, Member member) {
        for (Seat seat : seats) {
            if(!seat.getHoldMember().equals(member))
                throw new BusinessException(ErrorCode.HOLD_MEMBER_NOT_MATCH);
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
