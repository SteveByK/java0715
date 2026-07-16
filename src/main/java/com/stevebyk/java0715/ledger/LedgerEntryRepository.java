package com.stevebyk.java0715.ledger;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {

    List<LedgerEntryEntity> findByTransactionNoOrderByIdAsc(String transactionNo);
}
