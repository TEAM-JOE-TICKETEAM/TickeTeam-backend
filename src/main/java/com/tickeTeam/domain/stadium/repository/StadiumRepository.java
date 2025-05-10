package com.tickeTeam.domain.stadium.repository;

import com.tickeTeam.domain.stadium.entity.Stadium;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    Optional<Stadium> findByStadiumName(String stadiumName);
}
