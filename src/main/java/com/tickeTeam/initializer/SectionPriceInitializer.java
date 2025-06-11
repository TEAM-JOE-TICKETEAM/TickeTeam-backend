package com.tickeTeam.initializer;

import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class SectionPriceInitializer implements ApplicationRunner {

    private final SectionPriceRepository sectionPriceRepository;
    private final StadiumRepository stadiumRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Map<String, Integer> sections = new HashMap<>();
        sections.put("1루 블루존", 12000);
        sections.put("3루 레드존", 12000);
        sections.put("중앙 네이비석", 12000);
        sections.put("프리미엄 테이블석", 40000);
        sections.put("스카이박스", 120000);
        sections.put("1루 외야자유석", 9000);
        sections.put("3루 외야자유석", 9000);
        sections.put("그린존", 12000);

        List<Stadium> stadiums = stadiumRepository.findAll();
        List<SectionPrice> sectionPrices = new ArrayList<>();


        for (Stadium stadium : stadiums) {
            for (String section : sections.keySet()) {
                sectionPrices.add(SectionPrice.of(stadium, section, sections.get(section)));
            }
        }

        sectionPriceRepository.saveAll(sectionPrices);
    }
}
