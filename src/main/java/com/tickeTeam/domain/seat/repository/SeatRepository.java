package com.tickeTeam.domain.seat.repository;

import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.seat.entity.Seat;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    public List<Seat> findAllByGame(Game game);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public List<Seat> findAllByIdIn(List<Long> seatIds);

    public List<Seat> findByIdIn(List<Long> seatIds);

}
