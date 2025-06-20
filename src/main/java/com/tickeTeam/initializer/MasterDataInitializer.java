package com.tickeTeam.initializer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterDataInitializer implements ApplicationRunner {

    private final RedissonClient redissonClient;
    private final List<DataInitializer> initializers; // Spring이 정렬된 리스트를 주입


    private static final String INIT_LOCK_KEY = "DB_INITIALIZATION_LOCK";

    @Override
    public void run(ApplicationArguments args) {
        RLock lock = redissonClient.getLock(INIT_LOCK_KEY);

        try{
            // 10초 동안 락 획득 시도, 성공 시 2분간 점유
            if (lock.tryLock(10, 120, TimeUnit.SECONDS)){
                log.info(">>>> Acquired lock for DB initialization. <<<<");
                try{
                    for (DataInitializer initializer : initializers) {
                        initializer.run();
                    }

                    log.info(">>>> DB Data Initializing Finished Successfully. <<<<");
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.info(">>>> DB initialization lock released. <<<<");
                    }
                }
            } else {
                log.info(">>>> Could not acquire lock. Another instance is likely initializing data. Skipping. <<<<");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
