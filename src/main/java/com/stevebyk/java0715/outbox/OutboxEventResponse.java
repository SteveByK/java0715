package com.stevebyk.java0715.outbox;

import java.time.Instant;

public record OutboxEventResponse(
        String eventId,
        String aggregateId,
        String eventType,
        String payload,
        String status,
        Instant createdAt
) {

    public static OutboxEventResponse from(OutboxEventEntity entity) {
        return new OutboxEventResponse(entity.getEventId(), entity.getAggregateId(), entity.getEventType(),
                entity.getPayload(), entity.getStatus(), entity.getCreatedAt());
    }
}
