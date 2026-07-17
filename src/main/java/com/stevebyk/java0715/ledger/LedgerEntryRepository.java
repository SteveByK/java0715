package com.stevebyk.java0715.ledger;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
/**
 * Persistence port for ledger transaction and account history queries.
 */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {

    List<LedgerEntryEntity> findByTransactionNoOrderByIdAsc(String transactionNo);

    List<LedgerEntryEntity> findByAccountNoOrderByCreatedAtDesc(String accountNo);
}
