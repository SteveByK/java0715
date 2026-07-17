package com.stevebyk.java0715.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
/**
 * Service that records and queries business audit evidence.
 */
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records one business action result for later audit review.
     */
    public void record(String businessNo, String action, String result, String detail) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setBusinessNo(businessNo);
        entity.setAction(action);
        entity.setResult(result);
        entity.setDetail(detail);
        entity.setCreatedAt(Instant.now());
        auditLogRepository.save(entity);
    }

    /**
     * Returns audit entries for one business number ordered by newest first.
     */
    public List<AuditLogResponse> findByBusinessNo(String businessNo) {
        return auditLogRepository.findByBusinessNoOrderByCreatedAtDesc(businessNo).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
