package com.stevebyk.java0715.pricing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeRuleRepository extends JpaRepository<FeeRuleEntity, Long> {

    Optional<FeeRuleEntity> findFirstByBusinessTypeAndSourceCurrencyAndTargetCurrencyAndStatusOrderByIdDesc(
            String businessType, String sourceCurrency, String targetCurrency, RuleStatus status);
}
