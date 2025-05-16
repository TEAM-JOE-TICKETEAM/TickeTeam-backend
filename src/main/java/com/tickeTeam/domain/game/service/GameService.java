package com.tickeTeam.domain.game.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.dto.response.WeeklyGamesResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.entity.Team;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final String ACCESS_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;
    private final JwtUtil jwtUtil;

    // 7일 이내 경기 조회(조회 당일 기준)
    public ResultResponse findGamesInNextSevenDays(HttpServletRequest request) {
        Member findMember = getMemberByRequest(request);
        Team findTeam = findMember.getFavoriteTeam();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        List<Game> upcomingMatches = gameRepository.findGamesByTeamAndDateRange(today, endDate, findTeam);

        WeeklyGamesResponse weeklyGamesResponse = WeeklyGamesResponse.of(upcomingMatches, today, endDate, findTeam.getTeamName());

        return ResultResponse.of(ResultCode.GET_WEEKLY_GAME_SUCCESS, weeklyGamesResponse);
    }

    // 경기 선택(좌석 정보 조회)



    private Member getMemberByRequest(HttpServletRequest request) {
        // jwt 토큰으로부터 추출한 이메일로 사용자 조회
        String memberEmail = jwtUtil.getEmail(extractAccessToken(request));
        Member findMember = memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
        return findMember;
    }

    private String extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ACCESS_HEADER))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""))
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_ACCESS_NOT_EXIST));
    }

}
