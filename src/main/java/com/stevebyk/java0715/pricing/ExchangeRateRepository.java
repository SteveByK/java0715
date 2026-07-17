package com.stevebyk.java0715.pricing;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findFirstBySourceCurrencyAndTargetCurrencyAndStatusOrderByEffectiveAtDesc(
            String sourceCurrency, String targetCurrency, RuleStatus status);
}
