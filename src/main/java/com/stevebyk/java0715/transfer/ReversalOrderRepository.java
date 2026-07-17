package com.stevebyk.java0715.transfer;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
/**
 * Persistence port for transfer reversal orders.
 */
public interface ReversalOrderRepository extends JpaRepository<ReversalOrderEntity, Long> {

    Optional<ReversalOrderEntity> findByOriginalOrderNo(String originalOrderNo);

    Optional<ReversalOrderEntity> findByRequestId(String requestId);
}
