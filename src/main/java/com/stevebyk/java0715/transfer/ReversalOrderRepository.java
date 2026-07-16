package com.stevebyk.java0715.transfer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReversalOrderRepository extends JpaRepository<ReversalOrderEntity, Long> {

    Optional<ReversalOrderEntity> findByOriginalOrderNo(String originalOrderNo);

    Optional<ReversalOrderEntity> findByRequestId(String requestId);
}
