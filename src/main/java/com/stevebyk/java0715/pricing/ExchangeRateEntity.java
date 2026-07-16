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
@Table(name = "exchange_rate")
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String rateCode;

    @Column(nullable = false, length = 3)
    private String sourceCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(nullable = false, length = 64)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleStatus status;

    @Column(nullable = false)
    private Instant effectiveAt;

    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;
}
