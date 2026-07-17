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

    /**
     * Maximum time to wait for all requested account locks.
     */
    public Duration getWaitTimeout() {
        return waitTimeout;
    }

    /**
     * Updates the maximum account lock wait time.
     */
    public void setWaitTimeout(Duration waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    /**
     * Threshold for logging slow lock acquisition.
     */
    public Duration getSlowWaitThreshold() {
        return slowWaitThreshold;
    }

    /**
     * Updates the slow lock acquisition threshold.
     */
    public void setSlowWaitThreshold(Duration slowWaitThreshold) {
        this.slowWaitThreshold = slowWaitThreshold;
    }

    /**
     * Threshold for logging long lock holding time.
     */
    public Duration getSlowHoldThreshold() {
        return slowHoldThreshold;
    }

    /**
     * Updates the long lock holding threshold.
     */
    public void setSlowHoldThreshold(Duration slowHoldThreshold) {
        this.slowHoldThreshold = slowHoldThreshold;
    }
}
