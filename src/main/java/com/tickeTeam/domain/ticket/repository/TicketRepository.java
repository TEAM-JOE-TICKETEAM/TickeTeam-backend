package com.tickeTeam.domain.ticket.repository;

import com.tickeTeam.domain.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
