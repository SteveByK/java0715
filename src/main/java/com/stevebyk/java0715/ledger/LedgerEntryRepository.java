package com.stevebyk.java0715.ledger;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {

    List<LedgerEntryEntity> findByTransactionNoOrderByIdAsc(String transactionNo);

    List<LedgerEntryEntity> findByAccountNoOrderByCreatedAtDesc(String accountNo);
}
