package com.stevebyk.java0715.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByAggregateIdOrderByCreatedAtDesc(String aggregateId);
}
