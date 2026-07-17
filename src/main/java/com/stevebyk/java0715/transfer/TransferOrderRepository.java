package com.stevebyk.java0715.transfer;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface TransferOrderRepository extends JpaRepository<TransferOrderEntity, Long> {

    Optional<TransferOrderEntity> findByOrderNo(String orderNo);

    Optional<TransferOrderEntity> findByRequestId(String requestId);
}
