package com.tickeTeam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TickeTeamApplication {

	public static void main(String[] args) {
		SpringApplication.run(TickeTeamApplication.class, args);
	}

}
