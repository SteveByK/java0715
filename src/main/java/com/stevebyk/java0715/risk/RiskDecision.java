package com.stevebyk.java0715.risk;

/**
 * Immutable result returned by risk policy checks.
 */
public record RiskDecision(
        boolean approved,
        String code,
        String reason
) {

    public static RiskDecision approve() {
        return new RiskDecision(true, "APPROVED", "risk check passed");
    }

    public static RiskDecision reject(String code, String reason) {
        return new RiskDecision(false, code, reason);
    }
}
