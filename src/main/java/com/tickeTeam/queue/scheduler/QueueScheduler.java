package com.tickeTeam.queue.scheduler;

import com.tickeTeam.queue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final WaitingQueueService waitingQueueService;

    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    public void processWaitingQueue(){
        waitingQueueService.allowUsers()
                .subscribe(count -> {
                    if (count >0){
                        log.info("{}명의 사용자를 대기열에서 허용 명단으로 이동시켰습니다.", count);
                    }
                });
    }
}
