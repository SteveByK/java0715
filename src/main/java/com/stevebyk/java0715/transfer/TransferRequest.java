package com.stevebyk.java0715.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String requestId,
        @NotBlank String fromAccountNo,
        @NotBlank String toAccountNo,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency,
        String remark
) {
}
