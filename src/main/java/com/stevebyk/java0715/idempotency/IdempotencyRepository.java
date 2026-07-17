package com.stevebyk.java0715.idempotency;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByRequestIdAndBusinessType(String requestId, String businessType);
}
