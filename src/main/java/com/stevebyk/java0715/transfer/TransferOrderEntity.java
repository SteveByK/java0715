package com.stevebyk.java0715.transfer;

import com.stevebyk.java0715.common.ddd.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Domestic transfer order aggregate.
 *
 * <p>The order records command identity, risk result and lifecycle status.
 * Ledger entries remain the source of truth for actual debit and credit facts.</p>
 */
@Getter
@Setter
@Entity
@Table(name = "transfer_order")
@AggregateRoot
public class TransferOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @Column(nullable = false, length = 80)
    private String requestId;

    @Column(nullable = false, length = 32)
    private String fromAccountNo;

    @Column(nullable = false, length = 32)
    private String toAccountNo;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransactionStatus status;

    @Column(length = 80)
    private String riskCode;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
