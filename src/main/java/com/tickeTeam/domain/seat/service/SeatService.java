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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private static final String SEAT_PREFIX = "seat";
    public static final int LOCK_TIME_OUT = 1;
    public static final int LOCK_WAIT_TIME = 1;

    private final SeatRepository seatRepository;
    private final RedissonClient redissonClient;
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
    public ResultResponse selectSeats(SeatSelectRequest selectRequest) {

        List<Long> selectedSeatIds = selectRequest.getSeatIds();
        if (selectedSeatIds.size() >= 4) {
            throw new BusinessException(ErrorCode.SEAT_LIMIT_OVER);
        }
        Collections.sort(selectedSeatIds);  // 교착상태 방지를 위해 오름차순 정렬 적용

        List<Seat> selectedSeats = seatRepository.findAllByIdIn(selectedSeatIds); // 좌석 정보 조회

        // 가져온 좌석들의 상태(SeatStatus) 선점 상태로 변경하며 분산락 획득
        List<RLock> acquiredLocks = new ArrayList<>();
        String memberEmail = getMemberByAuthentication().getEmail();
        try {
            for (Seat seat : selectedSeats) {
                String key = keyResolver(seat.getId());
                RLock lock = redissonClient.getLock(key + ":lock");

                if (!lock.tryLock(LOCK_WAIT_TIME, LOCK_TIME_OUT, TimeUnit.MINUTES)) {
                    throw new BusinessException(ErrorCode.CANNOT_GET_LOCK);
                }

                acquiredLocks.add(lock); // 획득한 락 저장
                // 이미 선점된 좌석인지 확인
                String redisKey = key + ":heldBy";
                String existingUserId = (String) redissonClient.getBucket(redisKey).get();
                if (existingUserId!=null) {
                    throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
                }

                // 선점 정보 Redis에 저장 (7분 TTL)
                redissonClient.getBucket(redisKey).set(memberEmail, 7, TimeUnit.MINUTES);
            }

            holdSeats(selectedSeats);

        } catch (InterruptedException e) {
            // InterruptedException 발생 시 스레드의 인터럽트 상태 -> false
            Thread.currentThread().interrupt();  // 다시 interrupt 상태 확인 가능하도록 다시 true 로 돌려놓기
            throw new BusinessException(ErrorCode.INTERRUPTED_WHILE_LOCKING);
        } finally {
            releaseLocks(acquiredLocks);
        }

        return ResultResponse.of(ResultCode.SEATS_SELECT_SUCCESS);
    }

    public void holdSeats(List<Seat> selectedSeats) {
        selectedSeats.forEach(Seat::seatHold);  // 모든 좌석 락 획득 성공 시 선점 처리 진행(SeatStatus -> HELD)
    }

    private static void releaseLocks(List<RLock> locks) {
        for (RLock lock : locks) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public String keyResolver(Long seatId) {
        return SEAT_PREFIX + ":" + seatId;
    }

    private Member getMemberByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }

        String memberEmail = authentication.getName();
        return memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }

}
