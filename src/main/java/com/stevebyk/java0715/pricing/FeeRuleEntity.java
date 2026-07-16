package com.stevebyk.java0715.pricing;

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

@Getter
@Setter
@Entity
@Table(name = "fee_rule")
public class FeeRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String ruleCode;

    @Column(nullable = false, length = 40)
    private String businessType;

    @Column(nullable = false, length = 3)
    private String sourceCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal feeRate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minFee;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal maxFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
