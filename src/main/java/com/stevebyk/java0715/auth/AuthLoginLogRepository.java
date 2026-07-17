package com.stevebyk.java0715.auth;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for append-only authentication audit events.
 */
@OutboundPort
public interface AuthLoginLogRepository extends JpaRepository<AuthLoginLogEntity, Long> {
}
