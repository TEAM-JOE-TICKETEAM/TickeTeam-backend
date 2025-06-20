package com.tickeTeam.domain.seat.repository;

import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    public List<Seat> findAllByGameAndSeatStatus(Game game, SeatStatus seatStatus);

    @Query("SELECT s FROM Seat s JOIN FETCH s.seatTemplate WHERE s.game = :game AND s.seatStatus = :status")
    List<Seat> findAllByGameAndSeatStatusWithTemplate(@Param("game") Game game, @Param("status") SeatStatus status);


    public List<Seat> findAllByIdIn(List<Long> seatIds);

    public List<Seat> findByIdIn(List<Long> seatIds);

}
