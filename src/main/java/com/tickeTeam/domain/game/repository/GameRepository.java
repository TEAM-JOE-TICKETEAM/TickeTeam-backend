package com.tickeTeam.domain.game.repository;

import com.tickeTeam.domain.game.entity.Game;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByMatchDayBetweenAndStadium_Id(LocalDate startDate, LocalDate endDate, Long stadium_id);
}
