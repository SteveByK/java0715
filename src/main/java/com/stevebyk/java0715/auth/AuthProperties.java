package com.stevebyk.java0715.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunable authentication policy values for token TTLs and account lockout.
 */
@ConfigurationProperties(prefix = "bank.security")
public record AuthProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        int maxFailedLogin,
        Duration lockDuration
) {
}
