package com.tickeTeam.domain.member.repository;

import com.tickeTeam.domain.member.entity.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team,Long> {

    Optional<Team> findByTeamName(String teamName);
}
