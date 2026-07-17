package com.stevebyk.java0715.pricing;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

@OutboundPort
public interface RemittanceQuoteRepository extends JpaRepository<RemittanceQuoteEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RemittanceQuoteEntity> findByQuoteId(String quoteId);
}
