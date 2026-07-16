package com.stevebyk.java0715.ledger;

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
@Table(name = "ledger_entry")
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String entryNo;

    @Column(nullable = false, length = 40)
    private String transactionNo;

    @Column(nullable = false, length = 32)
    private String accountNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LedgerDirection direction;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 40)
    private String entryType;

    @Column(nullable = false)
    private Instant createdAt;
}
