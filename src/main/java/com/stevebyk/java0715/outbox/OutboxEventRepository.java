package com.stevebyk.java0715.outbox;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByAggregateIdOrderByCreatedAtDesc(String aggregateId);

    List<OutboxEventEntity> findTop50ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
