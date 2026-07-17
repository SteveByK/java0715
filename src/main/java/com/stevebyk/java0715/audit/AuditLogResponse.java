package com.stevebyk.java0715.audit;

import java.time.Instant;

/**
 * Audit log read model returned by audit APIs.
 */
public record AuditLogResponse(
        String businessNo,
        String action,
        String result,
        String detail,
        Instant createdAt
) {

    public static AuditLogResponse from(AuditLogEntity entity) {
        return new AuditLogResponse(entity.getBusinessNo(), entity.getAction(), entity.getResult(), entity.getDetail(),
                entity.getCreatedAt());
    }
}
