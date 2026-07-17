package com.stevebyk.java0715.audit;

import com.stevebyk.java0715.common.ddd.AggregateRoot;
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
@Table(name = "audit_log")
@AggregateRoot
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String businessNo;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(nullable = false, length = 32)
    private String result;

    @Column(length = 500)
    private String detail;

    @Column(nullable = false)
    private Instant createdAt;
}
