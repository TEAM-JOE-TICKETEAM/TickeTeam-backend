package com.tickeTeam.domain.seat.repository;

import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.seat.entity.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    public List<Seat> findAllByGame(Game game);
}
