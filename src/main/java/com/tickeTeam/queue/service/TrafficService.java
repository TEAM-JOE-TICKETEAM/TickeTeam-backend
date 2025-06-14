package com.tickeTeam.queue.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class  TrafficService {

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicBoolean queueActive = new AtomicBoolean(false);

    private static final long ACTIVATION_THRESHOLD = 1000;
    private static final long DEACTIVATION_THRESHOLD = 500;

    public void incrementRequestCount() {
        requestCount.incrementAndGet();
    }

    public boolean isQueueActive(){
        return queueActive.get();
    }

    @Scheduled(fixedDelay = 1000)
    public void checkTraffic(){
        long currentRps = requestCount.getAndSet(0);

        if(!queueActive.get() && currentRps > ACTIVATION_THRESHOLD){
            queueActive.set(true);
            log.info("대기열 시스템 활성화. RPS: {}", currentRps);
        } else if (queueActive.get() && currentRps < DEACTIVATION_THRESHOLD) {
            queueActive.set(false);
            log.info("대기열 시스템 비활성화, RPS: {}", currentRps);
        }
    }
}
