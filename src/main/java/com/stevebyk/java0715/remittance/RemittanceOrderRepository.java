package com.stevebyk.java0715.remittance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RemittanceOrderRepository extends JpaRepository<RemittanceOrderEntity, Long> {
}
