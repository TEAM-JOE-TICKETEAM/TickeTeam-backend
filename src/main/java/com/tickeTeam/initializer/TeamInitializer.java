package com.tickeTeam.initializer;

import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Order(1)
@Component
@RequiredArgsConstructor
public class TeamInitializer implements ApplicationRunner {

    private final TeamRepository teamRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (teamRepository.count() == 0) {
            List<Team> teams = List.of(
                    Team.of("LG 트윈스"),
                    Team.of("삼성 라이온즈"),
                    Team.of("두산 베어스"),
                    Team.of("한화 이글스"),
                    Team.of("기아 타이거즈"),
                    Team.of("롯데 자이언츠"),
                    Team.of("SSG 랜더스"),
                    Team.of("키움 히어로즈"),
                    Team.of("NC 다이노스"),
                    Team.of("KT 위즈")
            );

            teamRepository.saveAll(teams);
        }
    }
}
