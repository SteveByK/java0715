package com.stevebyk.java0715.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command payload for opening a domestic or overseas account.
 */
public record CreateAccountRequest(
        @NotBlank String customerId,
        @NotBlank String ownerName,
        @NotNull UserRegion userRegion,
        @NotNull AccountType accountType,
        @NotBlank String currency
) {
}
