package com.stevebyk.java0715.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command payload for approving or rejecting a KYC record.
 */
public record ReviewKycRequest(
        @NotNull KycStatus status,
        @NotBlank String reviewedBy
) {
}
