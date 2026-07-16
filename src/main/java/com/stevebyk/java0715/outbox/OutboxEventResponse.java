package com.stevebyk.java0715.outbox;

import java.time.Instant;

public record OutboxEventResponse(
        String eventId,
        String aggregateId,
        String eventType,
        String payload,
        String status,
        Integer retryCount,
        String lastError,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static OutboxEventResponse from(OutboxEventEntity entity) {
        return new OutboxEventResponse(entity.getEventId(), entity.getAggregateId(), entity.getEventType(),
                entity.getPayload(), entity.getStatus(), entity.getRetryCount(), entity.getLastError(),
                entity.getPublishedAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
