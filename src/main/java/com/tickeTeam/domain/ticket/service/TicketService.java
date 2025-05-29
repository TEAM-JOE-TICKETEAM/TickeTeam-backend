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
import com.tickeTeam.domain.ticket.dto.request.TicketingCancelRequest;
import com.tickeTeam.domain.ticket.dto.response.ReservationInfoResponse;
import com.tickeTeam.domain.ticket.dto.response.ReservationListResponse;
import com.tickeTeam.domain.ticket.entity.Reservation;
import com.tickeTeam.domain.ticket.entity.Ticket;
import com.tickeTeam.domain.ticket.repository.ReservationRepository;
import com.tickeTeam.domain.ticket.repository.TicketRepository;
import java.time.LocalDateTime;
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

        Reservation newReservation = creatAndSaveReservation(targetMatch, member);

        SectionPrice sectionPrice = sectionPriceRepository
                .findBySeatSectionAndSectionStadium(seats.get(0).getSeatTemplate().getSeatInfo().getSeatSection(), targetMatch.getStadium());

        for (Seat seat : seats) {

            Ticket newTicket = Ticket.builder()
                    .seat(seat)
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

        return ResultResponse.of(ResultCode.TICKET_ISSUE_SUCCESS, ReservationInfoResponse.from(newReservation));
    }

    // 티켓팅 시퀀스 도중 취소 요청 처리 메서드
    @Transactional
    public ResultResponse cancelTicketing(TicketingCancelRequest ticketingCancelRequest) {
        Member member = getMemberByAuthentication();

        List<Seat> seats = seatRepository.findByIdIn(ticketingCancelRequest.getSeatIds());

        checkIsHold(seats, member); // 각 좌석들이 해당 사용자에게 선점된 좌석이 맞는지 확인

        for (Seat seat : seats) {
            // 다시 AVAILABLE 상태로 변경
            seat.seatRelease();

            // Redis에서 선점 정보 삭제
            String holdKey = "seat:" + seat.getId() + ":heldBy";
            redissonClient.getBucket(holdKey).delete();
        }

        return ResultResponse.of(ResultCode.TICKETING_CANCEL_SUCCESS);
    }

    // 예약 정보 조회 메서드
    public ReservationListResponse getReservationInfoList(Member member){
        List<Reservation> reservations = reservationRepository.findAllByReservedMember(member);
        return ReservationListResponse.from(reservations);
    }

    // 예매 취소 메서드
    @Transactional
    public ResultResponse cancelReservation(String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode).orElseThrow(
                () -> new NotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        List<Ticket> tickets = reservation.getTickets();
        tickets.forEach(ticket -> ticket.getSeat().seatRelease());

        reservationRepository.delete(reservation);
        return ResultResponse.of(ResultCode.RESERVATION_CANCEL_SUCCESS);
    }

    // 특정 티켓 취소 메서드
    public ResultResponse cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new NotFoundException(ErrorCode.TICKET_NOT_FOUND));

        int remainTicketCount = ticket.getReservation().cancelTicket(ticket);
        ticket.getSeat().seatRelease();
        ticketRepository.delete(ticket);

        // 만약 Reservation 에 남은 티켓이 없다면 Reservation 도 제거
        if (remainTicketCount == 0) reservationRepository.delete(ticket.getReservation());

        return ResultResponse.of(ResultCode.TICKET_CANCEL_SUCCESS);
    }

    private void checkIsHold(List<Seat> seats, Member member) {
        for (Seat seat : seats) {
            String holdKey = "seat:" + seat.getId() + ":heldBy";
            String holderId = (String) redissonClient.getBucket(holdKey).get();
            System.out.println("horderId: "+ holderId);
            if (holderId == null) {
                throw new BusinessException(ErrorCode.SEAT_NOT_HELD);
            }
            if (!holderId.equals(member.getEmail())) {
                throw new BusinessException(ErrorCode.SEAT_HELD_BY_OTHER);
            }
        }
    }

    private Reservation creatAndSaveReservation(Game targetMatch, Member member) {
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
