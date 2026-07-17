package com.stevebyk.java0715.customer;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
/**
 * Persistence port for one-per-customer KYC records.
 */
public interface KycRepository extends JpaRepository<KycEntity, Long> {

    Optional<KycEntity> findByCustomerId(String customerId);
}
