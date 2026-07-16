package com.stevebyk.java0715.pricing;

import java.math.BigDecimal;

public record QuoteResponse(
        String sourceCurrency,
        String targetCurrency,
        BigDecimal sourceAmount,
        BigDecimal exchangeRate,
        BigDecimal fee,
        BigDecimal targetAmount,
        String feeRuleCode,
        String rateCode
) {
}
