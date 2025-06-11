package com.tickeTeam.domain.sectionPrice.repository;

import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.stadium.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionPriceRepository extends JpaRepository<SectionPrice, Long> {

    SectionPrice findBySeatSection(String section);
    SectionPrice findBySeatSectionAndSectionStadium(String section, Stadium stadium);
}
