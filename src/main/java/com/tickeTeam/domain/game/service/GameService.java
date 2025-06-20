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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    // 7일 이내 경기 조회(조회 당일 기준)
    @Cacheable(value = "weeklyGames", key = "#member.favoriteTeam.id")
    public WeeklyGamesResponse getGamesInNextSevenDays(Member member) {
        Team findTeam = member.getFavoriteTeam();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        List<Game> upcomingMatches = gameRepository.findGamesByTeamAndDateRange(today, endDate, findTeam);

        return WeeklyGamesResponse.of(upcomingMatches, today, endDate, findTeam.getTeamName());
    }
}
