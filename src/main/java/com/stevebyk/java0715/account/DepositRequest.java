package com.stevebyk.java0715.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Command payload for crediting funds into an account.
 */
public record DepositRequest(
        @NotBlank String requestId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency,
        String remark
) {
}
