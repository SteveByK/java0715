package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.common.ddd.AggregateRoot;
import com.stevebyk.java0715.transfer.TransactionStatus;
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
 * International remittance order aggregate.
 *
 * <p>The order preserves source amount, locked target amount, consumed quote,
 * risk outcome and settlement status for future audit and reconciliation.</p>
 */
@Getter
@Setter
@Entity
@Table(name = "remittance_order")
@AggregateRoot
public class RemittanceOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @Column(nullable = false, length = 80)
    private String requestId;

    @Column(nullable = false, length = 32)
    private String senderAccountNo;

    @Column(nullable = false, length = 32)
    private String receiverAccountNo;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sourceAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(length = 40)
    private String quoteId;

    @Column(length = 40)
    private String feeRuleCode;

    @Column(length = 40)
    private String rateCode;

    @Column(nullable = false, length = 3)
    private String sourceCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, length = 2)
    private String destinationCountry;

    @Column(length = 32)
    private String swiftCode;

    @Column(length = 40)
    private String iban;

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
