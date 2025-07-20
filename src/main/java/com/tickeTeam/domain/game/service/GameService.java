package com.tickeTeam.domain.game.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.dto.response.WeeklyGamesResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    // 7일 이내 경기 조회(조회 당일 기준)
    @Cacheable(value = "weeklyGames", key = "#member.favoriteTeam.id")
    @Transactional(readOnly = true)
    public WeeklyGamesResponse getGamesInNextSevenDays(Member member) {
        log.info("===== Cache Miss! Querying DB for weeklyGames. Team ID: {} =====", member.getFavoriteTeam().getId());

        Team findTeam = member.getFavoriteTeam();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        List<Game> upcomingMatches = gameRepository.findGamesByTeamAndDateRange(today, endDate, findTeam);

        return WeeklyGamesResponse.of(upcomingMatches, today, endDate, findTeam.getTeamName());
    }
}
