package com.tickeTeam.domain.ticket.repository;

import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.ticket.entity.Reservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByReservedMember(Member member);

    Optional<Reservation> findByReservationCode(String reservationCode);

}
