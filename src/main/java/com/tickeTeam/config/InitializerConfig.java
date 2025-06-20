package com.tickeTeam.config;

import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.member.repository.TeamRepository;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.seat.repository.SeatTemplateRepository;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import com.tickeTeam.domain.stadium.repository.StadiumRepository;
import com.tickeTeam.initializer.DataInitializer;
import com.tickeTeam.initializer.GameInitializer;
import com.tickeTeam.initializer.MemberInitializer;
import com.tickeTeam.initializer.SeatInitializer;
import com.tickeTeam.initializer.SeatTemplateInitializer;
import com.tickeTeam.initializer.SectionPriceInitializer;
import com.tickeTeam.initializer.StadiumInitializer;
import com.tickeTeam.initializer.TeamInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitializerConfig {

    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;
    private final SeatTemplateRepository seatTemplateRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SectionPriceRepository sectionPriceRepository;
    private final StadiumRepository stadiumRepository;

    @Bean
    @Order(1)
    public DataInitializer stadiumInitializer(){
        return new StadiumInitializer(stadiumRepository);
    }

    @Bean
    @Order(1)
    public DataInitializer teamInitializer(){
        return new TeamInitializer(teamRepository);
    }

    @Bean
    @Order(2)
    @Profile("!test")
    public DataInitializer sectionPriceInitializer(){
        return new SectionPriceInitializer(sectionPriceRepository, stadiumRepository);
    }

    @Bean
    @Order(1)
    @Profile("!test")
    public DataInitializer seatTemplateInitializer(){
        return new SeatTemplateInitializer(seatTemplateRepository);
    }

    @Bean
    @Order(3)
    @Profile("!test")
    public DataInitializer seatInitializer(){
        return new SeatInitializer(seatRepository, gameRepository, seatTemplateRepository);
    }

    @Bean
    @Order(2)
    @Profile("!test")
    public DataInitializer gameInitializer(){
        return new GameInitializer(gameRepository, teamRepository, stadiumRepository);
    }

    @Bean
    @Order(2)
    @Profile("!test")
    public DataInitializer memberInitializer(){
        return new MemberInitializer(memberRepository, teamRepository, bCryptPasswordEncoder);
    }
}
