package com.tickeTeam.domain.seat.repository;

import com.tickeTeam.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
