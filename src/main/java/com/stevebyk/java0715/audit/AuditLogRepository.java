package com.stevebyk.java0715.audit;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByBusinessNoOrderByCreatedAtDesc(String businessNo);
}
