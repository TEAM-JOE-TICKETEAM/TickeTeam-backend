package com.tickeTeam.initializer;

import com.tickeTeam.domain.stadium.entity.Stadium;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@RequiredArgsConstructor
public class StadiumInitializer implements ApplicationRunner {

    private final StadiumRepository stadiumRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (stadiumRepository.count() == 0) {
            List<Stadium> stadiums = List.of(
                    Stadium.of("잠실 야구장"),
                    Stadium.of("대구 삼성 라이온즈 파크"),
                    Stadium. of("대전 한화 생명 볼 파크"),
                    Stadium.of("광주 기아 챔피언스 필드"),
                    Stadium.of("사직 야구장"),
                    Stadium.of("인천 SSG 랜더스 필드"),
                    Stadium.of("고척 스카이 돔"),
                    Stadium.of("창원 NC 베이스볼 파크"),
                    Stadium.of("수원 KT 위즈 파크")
            );

            stadiumRepository.saveAll(stadiums);
        }
    }
}
