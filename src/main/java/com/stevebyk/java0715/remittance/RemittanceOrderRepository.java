package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface RemittanceOrderRepository extends JpaRepository<RemittanceOrderEntity, Long> {

    Optional<RemittanceOrderEntity> findByOrderNo(String orderNo);

    Optional<RemittanceOrderEntity> findByRequestId(String requestId);
}
