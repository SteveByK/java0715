package com.stevebyk.java0715.audit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByBusinessNoOrderByCreatedAtDesc(String businessNo);
}
