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
@Table(name = "remittance_quote")
public class RemittanceQuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String quoteId;

    @Column(nullable = false, length = 3)
    private String sourceCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sourceAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(nullable = false, length = 40)
    private String feeRuleCode;

    @Column(nullable = false, length = 40)
    private String rateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private QuoteStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;
}
