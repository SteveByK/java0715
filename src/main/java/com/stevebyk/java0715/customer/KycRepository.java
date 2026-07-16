package com.stevebyk.java0715.customer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycRepository extends JpaRepository<KycEntity, Long> {

    Optional<KycEntity> findByCustomerId(String customerId);
}
