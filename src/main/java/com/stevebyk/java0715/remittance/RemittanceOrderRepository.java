package com.stevebyk.java0715.remittance;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemittanceOrderRepository extends JpaRepository<RemittanceOrderEntity, Long> {

    Optional<RemittanceOrderEntity> findByOrderNo(String orderNo);
}
