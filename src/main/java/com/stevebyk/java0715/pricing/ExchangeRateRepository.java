package com.stevebyk.java0715.pricing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findFirstBySourceCurrencyAndTargetCurrencyAndStatusOrderByEffectiveAtDesc(
            String sourceCurrency, String targetCurrency, RuleStatus status);
}
