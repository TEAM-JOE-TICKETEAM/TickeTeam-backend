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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    // 7일 이내 경기 조회(조회 당일 기준)
    public ResultResponse getGamesInNextSevenDays() {
        Member findMember = getMemberByAuthentication();
        Team findTeam = findMember.getFavoriteTeam();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        List<Game> upcomingMatches = gameRepository.findGamesByTeamAndDateRange(today, endDate, findTeam);

        WeeklyGamesResponse weeklyGamesResponse = WeeklyGamesResponse.of(upcomingMatches, today, endDate, findTeam.getTeamName());

        return ResultResponse.of(ResultCode.GET_WEEKLY_GAME_SUCCESS, weeklyGamesResponse);
    }

    private Member getMemberByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }
        String memberEmail = authentication.getName();
        return memberRepository.findByEmailWithTeam(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }

}
