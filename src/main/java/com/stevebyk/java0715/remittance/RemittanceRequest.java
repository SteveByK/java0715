package com.stevebyk.java0715.remittance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RemittanceRequest(
        @NotBlank String requestId,
        @NotBlank String senderAccountNo,
        @NotBlank String receiverAccountNo,
        @NotNull @DecimalMin("0.01") BigDecimal sourceAmount,
        @NotBlank String sourceCurrency,
        @NotBlank String targetCurrency,
        @DecimalMin("0.00000001") BigDecimal exchangeRate,
        String quoteId,
        @NotBlank String destinationCountry,
        String swiftCode,
        String iban,
        String remark
) {
}
