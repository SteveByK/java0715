package com.stevebyk.java0715.ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public void append(String transactionNo, String accountNo, LedgerDirection direction, BigDecimal amount,
                       BigDecimal balanceAfter, String currency, String entryType) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setEntryNo("LE" + UUID.randomUUID().toString().replace("-", "").substring(0, 30));
        entity.setTransactionNo(transactionNo);
        entity.setAccountNo(accountNo);
        entity.setDirection(direction);
        entity.setAmount(amount);
        entity.setBalanceAfter(balanceAfter);
        entity.setCurrency(currency);
        entity.setEntryType(entryType);
        entity.setCreatedAt(Instant.now());
        ledgerEntryRepository.save(entity);
    }
}
