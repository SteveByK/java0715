package com.stevebyk.java0715.pricing;

import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.MoneyUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public static final String REMITTANCE = "INTERNATIONAL_REMITTANCE";

    private final ExchangeRateRepository exchangeRateRepository;
    private final FeeRuleRepository feeRuleRepository;

    public PricingService(ExchangeRateRepository exchangeRateRepository, FeeRuleRepository feeRuleRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.feeRuleRepository = feeRuleRepository;
    }

    public QuoteResponse quoteRemittance(String sourceCurrency, String targetCurrency, BigDecimal sourceAmount) {
        MoneyUtils.requirePositive(sourceAmount);
        String normalizedSource = sourceCurrency.toUpperCase();
        String normalizedTarget = targetCurrency.toUpperCase();
        ExchangeRateEntity rate = exchangeRateRepository
                .findFirstBySourceCurrencyAndTargetCurrencyAndStatusOrderByEffectiveAtDesc(
                        normalizedSource, normalizedTarget, RuleStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("EXCHANGE_RATE_NOT_FOUND", "active exchange rate not found"));
        FeeRuleEntity feeRule = feeRuleRepository
                .findFirstByBusinessTypeAndSourceCurrencyAndTargetCurrencyAndStatusOrderByIdDesc(
                        REMITTANCE, normalizedSource, normalizedTarget, RuleStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("FEE_RULE_NOT_FOUND", "active fee rule not found"));
        BigDecimal normalizedAmount = MoneyUtils.normalize(sourceAmount, normalizedSource);
        BigDecimal fee = calculateFee(normalizedAmount, feeRule);
        BigDecimal targetAmount = MoneyUtils.normalize(normalizedAmount.multiply(rate.getRate()), normalizedTarget);
        return new QuoteResponse(normalizedSource, normalizedTarget, normalizedAmount, rate.getRate(), fee,
                targetAmount, feeRule.getRuleCode(), rate.getRateCode());
    }

    public BigDecimal calculateRemittanceFee(String sourceCurrency, String targetCurrency, BigDecimal sourceAmount) {
        return quoteRemittance(sourceCurrency, targetCurrency, sourceAmount).fee();
    }

    private BigDecimal calculateFee(BigDecimal sourceAmount, FeeRuleEntity feeRule) {
        BigDecimal fee = sourceAmount.multiply(feeRule.getFeeRate()).setScale(sourceAmount.scale(), RoundingMode.HALF_UP);
        if (fee.compareTo(feeRule.getMinFee()) < 0) {
            return feeRule.getMinFee().setScale(sourceAmount.scale(), RoundingMode.HALF_UP);
        }
        if (fee.compareTo(feeRule.getMaxFee()) > 0) {
            return feeRule.getMaxFee().setScale(sourceAmount.scale(), RoundingMode.HALF_UP);
        }
        return fee;
    }
}
