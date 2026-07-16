package com.stevebyk.java0715.outbox;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void publish(String aggregateId, String eventType, String payload) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(UUID.randomUUID().toString());
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setPayload(payload);
        entity.setStatus("NEW");
        entity.setCreatedAt(Instant.now());
        outboxEventRepository.save(entity);
    }
}
