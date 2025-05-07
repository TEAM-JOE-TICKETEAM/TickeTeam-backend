package com.tickeTeam.domain.match.repository;

import com.tickeTeam.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
