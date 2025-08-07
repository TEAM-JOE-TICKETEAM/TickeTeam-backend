package com.tickeTeam.domain.seat.service.scheduler;

import com.tickeTeam.domain.seat.entity.Seat;
import com.tickeTeam.domain.seat.entity.SeatStatus;
import com.tickeTeam.domain.seat.repository.SeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeatHoldRecoveryScheduler {

    private final SeatRepository seatRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String SEAT_HELD_KEY_PREFIX = "seat:%d:heldBy";

    @Transactional
    @Scheduled(fixedDelay = 10 * 1000)
    public void recoveryExpiredSeatHold(){

        List<Seat> heldSeats = seatRepository.findBySeatStatus(SeatStatus.HELD);

        for (Seat seat : heldSeats) {
            String redisKey = String.format(SEAT_HELD_KEY_PREFIX, seat.getId());

            Boolean isHeldInRedis = redisTemplate.hasKey(redisKey);
            if (Boolean.FALSE.equals(isHeldInRedis)) {
                // Redis에 선점 정보가 없으면 DB 상태 복구
                seat.seatRelease();
                seatRepository.save(seat);
                // 로그 출력
                log.info("Seat ID={} 선점 만료로 AVAILABLE 상태로 복구됨", seat.getId());
            }
        }
    }
}
