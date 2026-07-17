package com.stevebyk.java0715.auth;

import com.stevebyk.java0715.common.ddd.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Login identity aggregate.
 *
 * <p>This entity stores authentication state only. Business customer data
 * remains in the customer bounded context to avoid leaking domain concerns into
 * security code.</p>
 */
@Getter
@Setter
@Entity
@Table(name = "auth_user")
@AggregateRoot
public class AuthUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String userId;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(length = 120)
    private String email;

    @Column(length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthUserStatus status;

    @Column(nullable = false)
    private int failedLoginCount;

    private Instant lockedUntil;

    private Instant lastLoginAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
