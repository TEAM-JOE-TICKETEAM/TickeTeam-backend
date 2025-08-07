package com.tickeTeam.domain.seat.repository;

import com.tickeTeam.common.annotation.Trace;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.seat.dto.response.GameSeatsResponse;
import com.tickeTeam.domain.seat.dto.response.SeatInfoResponse;
import com.tickeTeam.domain.seat.dto.response.SeatSummaryResponse;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByGameAndSeatStatus(Game game, SeatStatus seatStatus);

    @Query("SELECT s FROM Seat s " +
            "JOIN FETCH s.seatTemplate st " +
            "JOIN FETCH st.seatInfo " + // seatInfo도 함께 FETCH
            "WHERE s.game = :game AND s.seatStatus = :status")
    List<Seat> findAllByGameAndSeatStatusWithTemplate(@Param("game") Game game, @Param("status") SeatStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :seatIds")
    List<Seat> findAllByIdForUpdate(@Param("seatIds") List<Long> seatIds);

    List<Seat> findBySeatStatus(SeatStatus status);

    List<Seat> findByIdIn(List<Long> seatIds);

    @Query("""
                SELECT new com.tickeTeam.domain.seat.dto.response.SeatSummaryResponse(
                    st.seatInfo.seatSection,
                    st.seatInfo.seatBlock,
                    COUNT(s)
                )
                FROM Seat s
                JOIN s.seatTemplate st
                JOIN s.game g
                WHERE s.seatStatus = 'AVAILABLE' AND g.id = :gameId
                GROUP BY st.seatInfo.seatSection, st.seatInfo.seatBlock
            """)
    List<SeatSummaryResponse> findSeatSummaryByGameId(@Param("gameId") Long gameId);

    @Query("""
            SELECT new com.tickeTeam.domain.seat.dto.response.SeatInfoResponse(
                s.id,
                st.seatInfo.seatType,
                st.seatInfo.seatSection,
                st.seatInfo.seatBlock,
                st.seatInfo.seatRow,
                st.seatInfo.seatNum,
                s.seatStatus
            )
            FROM Seat s
            JOIN s.seatTemplate st
            WHERE s.game = :game
            AND s.seatStatus = :status
            AND st.seatInfo.seatSection = :seatSection
            AND st.seatInfo.seatBlock = :seatBlock
            """)
    List<SeatInfoResponse> findSeatProjectionsByGameAndSectionAndBlock(Game game, SeatStatus status, String seatSection, String seatBlock);
}
