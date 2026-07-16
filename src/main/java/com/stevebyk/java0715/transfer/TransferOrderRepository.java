package com.stevebyk.java0715.transfer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferOrderRepository extends JpaRepository<TransferOrderEntity, Long> {

    Optional<TransferOrderEntity> findByOrderNo(String orderNo);

    Optional<TransferOrderEntity> findByRequestId(String requestId);
}
