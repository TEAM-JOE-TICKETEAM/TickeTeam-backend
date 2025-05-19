package com.tickeTeam.domain.seat.service;

import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.member.entity.Member;
import com.tickeTeam.domain.member.repository.MemberRepository;
import com.tickeTeam.domain.seat.dto.request.SeatSelectRequest;
import com.tickeTeam.domain.seat.dto.response.GameSeatsResponse;
import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import com.tickeTeam.domain.sectionPrice.repository.SectionPriceRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SectionPriceRepository sectionPriceRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    // 좌석 정보 조회
    public ResultResponse getGameSeats(Long gameId) {
        Game findGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND));
        List<Seat> seats = seatRepository.findAllByGame(findGame);
        return ResultResponse.of(ResultCode.GET_GAME_SEAT_SUCCESS,
                GameSeatsResponse.of(seats, gameId, findGame.getStadium().getStadiumName()));
    }

    // 좌석 선택(다중 선택 가능, 선택 시 해당 좌석에 선점 적용(7분))
    // 한 번에 인당 최대 4석, 같은 구역 내에서만 다중 선택 가능
    @Transactional
    public ResultResponse selectSeats(SeatSelectRequest selectRequest){

        // 교착상태 방지를 위해 가져온 좌석 Id 들에 오름차순 정렬 적용
        List<Long> selectedSeatIds = selectRequest.getSeatIds();
        if (selectedSeatIds.size() >= 4){
            throw new BusinessException(ErrorCode.SEAT_LIMIT_OVER);
        }
        Collections.sort(selectedSeatIds);

        // 선택된 좌석 가져오기
        List<Seat> selectedSeats = seatRepository.findAllByIdIn(selectedSeatIds);

        // 가져온 좌석들의 상태(SeatStatus) 선점 상태로 변경(이미 DB에서 가져올 때 락은 걸려있음) 및 선점 적용자 정보 삽입
        Member selectMember = getMemberByAuthentication();
        selectedSeats.forEach((seat) -> seat.seatHold(selectMember));

        return ResultResponse.of(ResultCode.SEATS_SELECT_SUCCESS);
    }

    private Member getMemberByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }

        String memberEmail = authentication.getName();
        return memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }
}
