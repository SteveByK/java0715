package com.stevebyk.java0715.ledger;

import com.stevebyk.java0715.common.ddd.ApplicationServiceRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Append-only ledger writer and query service.
 *
 * <p>Every successful money movement should create ledger entries. This service
 * keeps balance mutation traceability separate from mutable balance snapshots.</p>
 */
@Service
@ApplicationServiceRole
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

    public List<LedgerEntryResponse> findByTransactionNo(String transactionNo) {
        return ledgerEntryRepository.findByTransactionNoOrderByIdAsc(transactionNo).stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }

    public List<LedgerEntryResponse> findByAccountNo(String accountNo) {
        return ledgerEntryRepository.findByAccountNoOrderByCreatedAtDesc(accountNo).stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }
}
