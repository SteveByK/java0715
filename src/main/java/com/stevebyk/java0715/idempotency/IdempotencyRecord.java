package com.stevebyk.java0715.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "idempotency_record")
/**
 * External command identity record used to prevent duplicate processing.
 */
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String requestId;

    @Column(nullable = false, length = 40)
    private String businessType;

    @Column(nullable = false, length = 32)
    private String businessNo;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(columnDefinition = "clob")
    private String responseSnapshot;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
