package com.stevebyk.java0715.lock;

import com.stevebyk.java0715.common.BusinessException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAccountLockExecutorTest {

    @Test
    void shouldTimeoutWhenAccountLockCannotBeAcquired() throws InterruptedException {
        BankLockProperties properties = new BankLockProperties();
        properties.setWaitTimeout(Duration.ofMillis(100));
        properties.setSlowWaitThreshold(Duration.ofSeconds(10));
        properties.setSlowHoldThreshold(Duration.ofSeconds(10));
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LocalAccountLockExecutor executor = new LocalAccountLockExecutor(properties, meterRegistry);
        CountDownLatch firstActionStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstAction = new CountDownLatch(1);
        AtomicReference<Throwable> firstThreadFailure = new AtomicReference<>();

        Thread firstThread = new Thread(() -> {
            try {
                executor.executeWithAccountLocks(List.of("AC_LOCK_TIMEOUT"), () -> {
                    firstActionStarted.countDown();
                    await(releaseFirstAction);
                    return null;
                });
            } catch (Throwable throwable) {
                firstThreadFailure.set(throwable);
            }
        });
        firstThread.start();

        assertThat(firstActionStarted.await(1, TimeUnit.SECONDS)).isTrue();
        assertThatThrownBy(() -> executor.executeWithAccountLocks(List.of("AC_LOCK_TIMEOUT"), () -> null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("account lock wait timeout");

        releaseFirstAction.countDown();
        firstThread.join(1000);
        assertThat(firstThreadFailure.get()).isNull();
        assertThat(meterRegistry.counter("bank.account.lock.timeout").count()).isEqualTo(1.0D);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
