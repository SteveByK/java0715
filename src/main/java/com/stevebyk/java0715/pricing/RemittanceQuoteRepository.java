package com.stevebyk.java0715.pricing;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RemittanceQuoteRepository extends JpaRepository<RemittanceQuoteEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RemittanceQuoteEntity> findByQuoteId(String quoteId);
}
