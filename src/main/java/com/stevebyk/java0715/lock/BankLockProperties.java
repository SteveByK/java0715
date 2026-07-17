package com.stevebyk.java0715.lock;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Runtime tuning for account-level application locks.
 */
@ConfigurationProperties(prefix = "bank.lock")
public class BankLockProperties {

    private Duration waitTimeout = Duration.ofSeconds(3);

    private Duration slowWaitThreshold = Duration.ofMillis(200);

    private Duration slowHoldThreshold = Duration.ofMillis(500);

    public Duration getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(Duration waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public Duration getSlowWaitThreshold() {
        return slowWaitThreshold;
    }

    public void setSlowWaitThreshold(Duration slowWaitThreshold) {
        this.slowWaitThreshold = slowWaitThreshold;
    }

    public Duration getSlowHoldThreshold() {
        return slowHoldThreshold;
    }

    public void setSlowHoldThreshold(Duration slowHoldThreshold) {
        this.slowHoldThreshold = slowHoldThreshold;
    }
}
