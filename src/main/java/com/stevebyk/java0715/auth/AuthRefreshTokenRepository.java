package com.stevebyk.java0715.auth;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for refresh-token rotation and logout revocation.
 */
@OutboundPort
public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshTokenEntity, Long> {

    Optional<AuthRefreshTokenEntity> findByTokenHash(String tokenHash);
}
