package com.stevebyk.java0715.lock;

import com.stevebyk.java0715.common.BusinessException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
/**
 * JVM-local implementation of account locking with timeout, metrics and slow-lock logs.
 */
public class LocalAccountLockExecutor implements AccountLockExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAccountLockExecutor.class);

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final BankLockProperties properties;
    private final Timer waitTimer;
    private final Timer holdTimer;
    private final Counter timeoutCounter;
    private final Counter interruptedCounter;

    public LocalAccountLockExecutor(BankLockProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.waitTimer = Timer.builder("bank.account.lock.wait")
                .description("Time spent waiting for account locks")
                .register(meterRegistry);
        this.holdTimer = Timer.builder("bank.account.lock.hold")
                .description("Time spent holding account locks")
                .register(meterRegistry);
        this.timeoutCounter = meterRegistry.counter("bank.account.lock.timeout");
        this.interruptedCounter = meterRegistry.counter("bank.account.lock.interrupted");
    }

    @Override
    public <T> T executeWithAccountLocks(Collection<String> accountNos, Supplier<T> action) {
        List<String> orderedAccountNos = accountNos.stream().distinct().sorted().toList();
        List<ReentrantLock> orderedLocks = orderedAccountNos.stream()
                .map(accountNo -> locks.computeIfAbsent(accountNo, ignored -> new ReentrantLock()))
                .toList();
        long waitStartedAt = System.nanoTime();
        List<ReentrantLock> acquiredLocks = acquireLocks(orderedAccountNos, orderedLocks);
        recordWait(orderedAccountNos, waitStartedAt);
        long holdStartedAt = System.nanoTime();
        try {
            return action.get();
        } finally {
            releaseLocks(acquiredLocks);
            recordHold(orderedAccountNos, holdStartedAt);
        }
    }

    private List<ReentrantLock> acquireLocks(List<String> accountNos, List<ReentrantLock> orderedLocks) {
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (ReentrantLock lock : orderedLocks) {
                if (!lock.tryLock(properties.getWaitTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                    releaseLocks(acquiredLocks);
                    timeoutCounter.increment();
                    LOGGER.warn("Account lock wait timeout accounts={} timeoutMs={}",
                            accountNos, properties.getWaitTimeout().toMillis());
                    throw new BusinessException("ACCOUNT_LOCK_TIMEOUT", "account lock wait timeout");
                }
                acquiredLocks.add(lock);
            }
            return acquiredLocks;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            releaseLocks(acquiredLocks);
            interruptedCounter.increment();
            LOGGER.warn("Account lock wait interrupted accounts={}", accountNos);
            throw new BusinessException("ACCOUNT_LOCK_INTERRUPTED", "account lock wait interrupted");
        }
    }

    private void releaseLocks(List<ReentrantLock> acquiredLocks) {
        List<ReentrantLock> reverseLocks = new ArrayList<>(acquiredLocks);
        Collections.reverse(reverseLocks);
        reverseLocks.forEach(ReentrantLock::unlock);
    }

    private void recordWait(List<String> accountNos, long startedAt) {
        Duration duration = Duration.ofNanos(System.nanoTime() - startedAt);
        waitTimer.record(duration);
        if (duration.compareTo(properties.getSlowWaitThreshold()) > 0) {
            LOGGER.warn("Slow account lock wait accounts={} waitMs={}", accountNos, duration.toMillis());
        }
    }

    private void recordHold(List<String> accountNos, long startedAt) {
        Duration duration = Duration.ofNanos(System.nanoTime() - startedAt);
        holdTimer.record(duration);
        if (duration.compareTo(properties.getSlowHoldThreshold()) > 0) {
            LOGGER.warn("Slow account lock hold accounts={} holdMs={}", accountNos, duration.toMillis());
        }
    }
}
