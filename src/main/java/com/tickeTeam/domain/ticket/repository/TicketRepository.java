package com.tickeTeam.domain.ticket.repository;

import com.tickeTeam.domain.ticket.entity.Reservation;
import com.tickeTeam.domain.ticket.entity.Ticket;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
