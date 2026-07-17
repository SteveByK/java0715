package com.stevebyk.java0715.pricing;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@OutboundPort
public interface FeeRuleRepository extends JpaRepository<FeeRuleEntity, Long> {

    Optional<FeeRuleEntity> findFirstByBusinessTypeAndSourceCurrencyAndTargetCurrencyAndStatusOrderByIdDesc(
            String businessType, String sourceCurrency, String targetCurrency, RuleStatus status);
}
