package com.tickeTeam.domain.game.repository;

import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.member.entity.Team;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByMatchDayBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 기간 내에 주어진 팀이 홈팀이거나 어웨이팀으로 참여하는 모든 경기를 조회
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @param team 조회할 팀
     * @return 조건에 맞는 경기 목록 (없으면 빈 리스트)
     */
    @Query("SELECT g FROM Game g WHERE g.matchDay BETWEEN :startDate AND :endDate AND (g.homeTeam = :team OR g.awayTeam = :team) ORDER BY g.matchDay, g.matchTime")
    List<Game> findGamesByTeamAndDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("team") Team team
    );
}
