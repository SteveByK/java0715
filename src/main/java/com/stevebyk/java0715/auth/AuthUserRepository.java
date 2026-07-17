package com.stevebyk.java0715.auth;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence port for authentication identities.
 */
@OutboundPort
public interface AuthUserRepository extends JpaRepository<AuthUserEntity, Long> {

    Optional<AuthUserEntity> findByUsername(String username);

    Optional<AuthUserEntity> findByUserId(String userId);
}
