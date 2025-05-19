package com.tickeTeam.initializer;

import com.tickeTeam.domain.sectionPrice.entity.SectionPrice;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import java.util.List;
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

        Stadium stadiumJamsil = stadiumRepository.findByStadiumName("잠실 야구장")
                .orElse(stadiumRepository.save(Stadium.of("잠실 야구장")));

        if (sectionPriceRepository.count() == 0) {
            List<SectionPrice> sectionPrices = List.of(
                    SectionPrice.of(stadiumJamsil, "1루 레드석", 12000),
                    SectionPrice.of(stadiumJamsil, "3루 외야 일반석", 7000)
            );

            sectionPriceRepository.saveAll(sectionPrices);
        }
    }
}
