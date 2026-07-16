package com.stevebyk.java0715.account;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(
        @NotNull AccountStatus status,
        String reason
) {
}
