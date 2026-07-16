package com.stevebyk.java0715.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal amount, String currencyCode) {
        int scale = Currency.getInstance(currencyCode).getDefaultFractionDigits();
        return amount.setScale(Math.max(scale, 0), RoundingMode.HALF_UP);
    }

    public static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "amount must be greater than zero");
        }
    }
}
