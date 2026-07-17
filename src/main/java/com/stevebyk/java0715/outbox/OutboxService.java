package com.stevebyk.java0715.outbox;

import com.stevebyk.java0715.common.ddd.ApplicationServiceRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional outbox service.
 *
 * <p>Domain events are persisted with the business transaction and later moved
 * to published state by a relay. This prepares the monolith for reliable message
 * publishing when contexts are split into services.</p>
 */
@Service
@ApplicationServiceRole
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void publish(String aggregateId, String eventType, String payload) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(UUID.randomUUID().toString());
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setPayload(payload);
        entity.setStatus("NEW");
        entity.setRetryCount(0);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        outboxEventRepository.save(entity);
    }

    @Transactional
    public List<OutboxEventResponse> publishPending() {
        return outboxEventRepository.findTop50ByStatusInOrderByCreatedAtAsc(List.of("NEW", "FAILED")).stream()
                .map(this::markPublished)
                .map(OutboxEventResponse::from)
                .toList();
    }

    public List<OutboxEventResponse> findByAggregateId(String aggregateId) {
        return outboxEventRepository.findByAggregateIdOrderByCreatedAtDesc(aggregateId).stream()
                .map(OutboxEventResponse::from)
                .toList();
    }

    private OutboxEventEntity markPublished(OutboxEventEntity entity) {
        entity.setStatus("PUBLISHED");
        entity.setLastError(null);
        entity.setPublishedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
