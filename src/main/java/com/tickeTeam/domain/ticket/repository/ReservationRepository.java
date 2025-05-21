package com.tickeTeam.domain.ticket.repository;

import com.tickeTeam.domain.ticket.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
