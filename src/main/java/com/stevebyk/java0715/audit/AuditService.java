package com.stevebyk.java0715.audit;

import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String businessNo, String action, String result, String detail) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setBusinessNo(businessNo);
        entity.setAction(action);
        entity.setResult(result);
        entity.setDetail(detail);
        entity.setCreatedAt(Instant.now());
        auditLogRepository.save(entity);
    }
}
