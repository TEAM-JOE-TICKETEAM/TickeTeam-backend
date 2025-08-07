package com.tickeTeam.domain.seat.service;

import com.tickeTeam.common.annotation.Trace;
import com.tickeTeam.common.exception.ErrorCode;
import com.tickeTeam.common.exception.customException.BusinessException;
import com.tickeTeam.common.exception.customException.NotFoundException;
import com.tickeTeam.common.result.ResultCode;
import com.tickeTeam.common.result.ResultResponse;
import com.tickeTeam.domain.game.entity.Game;
import com.tickeTeam.domain.game.repository.GameRepository;
import com.tickeTeam.domain.seat.dto.request.BlockSeatsRequest;
import com.tickeTeam.domain.seat.dto.request.SeatSelectRequest;
import com.tickeTeam.domain.seat.dto.response.BlockSeatsResponse;
import com.tickeTeam.domain.seat.dto.response.GameSeatsResponse;
import com.tickeTeam.domain.seat.dto.response.SeatInfoResponse;
import com.tickeTeam.domain.seat.dto.response.SeatSummaryResponse;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLettuceService {

    private final StringRedisTemplate redisTemplate;

    private static final String SEAT_PREFIX = "seat";
    private static final String LOCK_SUFFIX = ":lock";
    private static final String HELD_BY_SUFFIX = ":heldBy";
    public static final int LOCK_TIME_OUT = 5;
    public static final int LOCK_WAIT_TIME = 0;
    private static final long LOCK_EXPIRE = 60;  // 1분
    private static final long HELD_TTL = 7 * 60; // 7분

    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;
    private final SeatTransactionService seatTransactionService;


    // 좌석 선택(다중 선택 가능, 선택 시 해당 좌석에 선점 적용(7분))
    // 한 번에 인당 최대 4석, 같은 구역 내에서만 다중 선택 가능
    @Trace
    public ResultResponse selectSeats(SeatSelectRequest selectRequest) {

        List<Long> selectedSeatIds = selectRequest.getSeatIds();
        if (selectedSeatIds.size() >= 4) {
            throw new BusinessException(ErrorCode.SEAT_LIMIT_OVER);
        }
        Collections.sort(selectedSeatIds);  // 교착상태 방지를 위해 오름차순 정렬 적용

        // 가져온 좌석들의 상태(SeatStatus) 선점 상태로 변경하며 분산락 획득
        List<String> acquiredKeys = new ArrayList<>();
        String memberEmail = getEmailByAuthentication();
        try {
            for (Long seatId : selectedSeatIds) {
                String key = keyResolver(seatId) + LOCK_SUFFIX;
                String lockValue = UUID.randomUUID().toString();

                // 락 획득 시도 (대기 X, 1분 후 자동 해제)
                Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, lockValue, LOCK_EXPIRE, TimeUnit.SECONDS);
                if (!Boolean.TRUE.equals(locked)) {
                    throw new BusinessException(ErrorCode.CANNOT_GET_LOCK);
                }

                acquiredKeys.add(key + "::" + lockValue); // 획득한 락 저장

                // 이미 선점된 좌석인지 확인
                String heldKey = keyResolver(seatId) + HELD_BY_SUFFIX;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(heldKey))) {
                    throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
                }

                // 선점 정보 Redis에 저장 (7분 TTL)
                redisTemplate.opsForValue().set(heldKey, memberEmail, HELD_TTL, TimeUnit.SECONDS);
            }

        } finally {
            for (String fullKey : acquiredKeys) {
                String[] parts = fullKey.split("::");
                String key = parts[0];
                String value = parts[1];
                redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), value);
            }
        }

        seatTransactionService.holdSeatsInNewTransaction(selectedSeatIds);
        return ResultResponse.of(ResultCode.SEATS_SELECT_SUCCESS);
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

    private String getEmailByAuthentication() {
        // Authentication 에서 추출한 이메일로 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null || !authentication.isAuthenticated()) {
            throw new NotFoundException(ErrorCode.AUTHENTICATION_NOT_FOUND);
        }
        return authentication.getName();
    }

}
