package com.tickeTeam.domain.game.repository;

import com.tickeTeam.domain.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
