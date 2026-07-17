package com.stevebyk.java0715.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Money helper methods for amount validation and currency scale normalization.
 */
public final class MoneyUtils {

    private MoneyUtils() {
    }

    /**
     * Normalizes a money amount to the default fraction digits of the currency.
     */
    public static BigDecimal normalize(BigDecimal amount, String currencyCode) {
        int scale = Currency.getInstance(currencyCode).getDefaultFractionDigits();
        return amount.setScale(Math.max(scale, 0), RoundingMode.HALF_UP);
    }

    /**
     * Ensures a business amount is present and greater than zero.
     */
    public static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "amount must be greater than zero");
        }
    }
}
