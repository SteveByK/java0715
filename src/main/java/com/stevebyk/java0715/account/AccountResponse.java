package com.stevebyk.java0715.account;

import java.math.BigDecimal;

/**
 * Account read model returned to API clients.
 */
public record AccountResponse(
        String accountNo,
        String customerId,
        String ownerName,
        UserRegion userRegion,
        AccountType accountType,
        String currency,
        BigDecimal availableBalance,
        BigDecimal frozenBalance,
        AccountStatus status
) {

    static AccountResponse from(AccountEntity entity) {
        return new AccountResponse(
                entity.getAccountNo(),
                entity.getCustomerId(),
                entity.getOwnerName(),
                entity.getUserRegion(),
                entity.getAccountType(),
                entity.getCurrency(),
                entity.getAvailableBalance(),
                entity.getFrozenBalance(),
                entity.getStatus());
    }
}
