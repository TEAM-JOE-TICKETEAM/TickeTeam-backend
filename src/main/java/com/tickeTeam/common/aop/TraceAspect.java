package com.tickeTeam.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Slf4j
public class TraceAspect {

    @Around("@within(com.tickeTeam.common.annotation.Trace) || @annotation(com.tickeTeam.common.annotation.Trace)")
    public Object traceExecuteTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();

        log.info("➡ START [{}] readOnly={}, activeTx={}", methodName, readOnly, actualTransactionActive);

        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            log.info("⬅ END [{}] duration={}ms", methodName, end - start);
        }
    }
}
