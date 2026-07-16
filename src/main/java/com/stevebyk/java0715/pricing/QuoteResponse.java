package com.stevebyk.java0715.pricing;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(
        String quoteId,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal sourceAmount,
        BigDecimal exchangeRate,
        BigDecimal fee,
        BigDecimal targetAmount,
        String feeRuleCode,
        String rateCode,
        Instant expiresAt
) {
}
