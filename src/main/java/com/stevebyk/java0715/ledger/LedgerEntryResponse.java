package com.stevebyk.java0715.ledger;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Ledger entry read model exposed by ledger query APIs.
 */
public record LedgerEntryResponse(
        String entryNo,
        String transactionNo,
        String accountNo,
        LedgerDirection direction,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String currency,
        String entryType,
        Instant createdAt
) {

    public static LedgerEntryResponse from(LedgerEntryEntity entity) {
        return new LedgerEntryResponse(entity.getEntryNo(), entity.getTransactionNo(), entity.getAccountNo(),
                entity.getDirection(), entity.getAmount(), entity.getBalanceAfter(), entity.getCurrency(),
                entity.getEntryType(), entity.getCreatedAt());
    }
}
