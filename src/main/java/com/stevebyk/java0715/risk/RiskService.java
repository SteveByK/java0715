package com.stevebyk.java0715.risk;

import com.stevebyk.java0715.common.ddd.DomainServiceRole;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Domain policy service for risk decisions.
 *
 * <p>Current rules are intentionally simple but isolated here so later
 * integration with a fraud or compliance system does not leak into payment
 * orchestration code.</p>
 */
@Service
@DomainServiceRole
public class RiskService {

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("IR", "KP", "SY");

    private final BigDecimal domesticHighRiskThreshold;
    private final BigDecimal remittanceHighRiskThreshold;

    public RiskService(
            @Value("${bank.transfer.high-risk-threshold}") BigDecimal domesticHighRiskThreshold,
            @Value("${bank.transfer.remittance-high-risk-threshold}") BigDecimal remittanceHighRiskThreshold) {
        this.domesticHighRiskThreshold = domesticHighRiskThreshold;
        this.remittanceHighRiskThreshold = remittanceHighRiskThreshold;
    }

    /**
     * Applies domestic transfer amount threshold risk policy.
     */
    public RiskDecision checkDomesticTransfer(BigDecimal amount) {
        if (amount.compareTo(domesticHighRiskThreshold) > 0) {
            return RiskDecision.reject("DOMESTIC_HIGH_AMOUNT", "domestic transfer amount requires manual review");
        }
        return RiskDecision.approve();
    }

    /**
     * Applies remittance amount and destination-country risk policy.
     */
    public RiskDecision checkRemittance(BigDecimal sourceAmount, String destinationCountry) {
        if (HIGH_RISK_COUNTRIES.contains(destinationCountry)) {
            return RiskDecision.reject("HIGH_RISK_COUNTRY", "destination country is blocked by risk policy");
        }
        if (sourceAmount.compareTo(remittanceHighRiskThreshold) > 0) {
            return RiskDecision.reject("REMITTANCE_HIGH_AMOUNT", "remittance amount requires manual review");
        }
        return RiskDecision.approve();
    }
}
