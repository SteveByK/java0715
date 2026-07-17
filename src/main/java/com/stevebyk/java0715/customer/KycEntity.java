package com.stevebyk.java0715.customer;

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

@Getter
@Setter
@Entity
@Table(name = "kyc_profile")
@AggregateRoot
/**
 * KYC aggregate containing masked identity document and review status.
 */
public class KycEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String customerId;

    @Column(nullable = false, length = 32)
    private String documentType;

    @Column(nullable = false, length = 64)
    private String maskedDocumentNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KycLevel kycLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KycStatus status;

    @Column(length = 64)
    private String reviewedBy;

    private Instant reviewedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
