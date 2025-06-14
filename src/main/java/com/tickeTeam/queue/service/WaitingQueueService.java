package com.tickeTeam.queue.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Value("${waiting.key}")
    private String WAITING_QUEUE_KEY;

    @Value("${waiting.allow.key")
    private String ALLOWED_MEMBER_SET_KEY;

    private final long ALLOWED_MEMBER_COUNT_PER_SECOND = 500;

    public Mono<Long> addQueue(String memberId){
        long timeStamp = Instant.now().toEpochMilli();
        return reactiveRedisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, memberId, timeStamp)
                .flatMap(added -> reactiveRedisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, memberId));
    }

    public Mono<Boolean> isAllowed(String memberId){
        return reactiveRedisTemplate.opsForSet().isMember(ALLOWED_MEMBER_SET_KEY, memberId);
    }

    public Mono<Long> getRank(String userId) {
        return reactiveRedisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, userId);
    }

    public Mono<Long> allowUsers() {
        return reactiveRedisTemplate.opsForZSet()
                .popMin(WAITING_QUEUE_KEY, ALLOWED_MEMBER_COUNT_PER_SECOND)
                .map(ZSetOperations.TypedTuple::getValue)
                .collectList()
                .flatMap(allowUsers -> {
                    if (allowUsers.isEmpty()){
                        return Mono.just(0L);
                    }
                    return reactiveRedisTemplate.opsForSet().add(ALLOWED_MEMBER_SET_KEY, allowUsers.toArray(new String[0]))
                            .doOnSuccess(count -> {
                                reactiveRedisTemplate.expire(ALLOWED_MEMBER_SET_KEY, Duration.ofSeconds(5)).subscribe();
                            });
        });
    }

    public Mono<Void> markAsEntered(String memberId){
        return reactiveRedisTemplate.opsForSet().remove(ALLOWED_MEMBER_SET_KEY, memberId).then();
    }
}
