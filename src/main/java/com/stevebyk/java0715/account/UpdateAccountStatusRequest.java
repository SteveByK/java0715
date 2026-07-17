package com.stevebyk.java0715.account;

import jakarta.validation.constraints.NotNull;

/**
 * Command payload for operational account status changes.
 */
public record UpdateAccountStatusRequest(
        @NotNull AccountStatus status,
        String reason
) {
}
