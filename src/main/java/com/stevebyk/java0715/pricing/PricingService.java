package com.stevebyk.java0715.pricing;

import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.MoneyUtils;
import com.stevebyk.java0715.common.ddd.DomainServiceRole;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain service for remittance pricing.
 *
 * <p>Pricing combines exchange-rate and fee-rule repositories into a short-lived
 * quote. Quote usage is locked and stateful so settlement cannot use stale or
 * already consumed pricing decisions.</p>
 */
@Service
@DomainServiceRole
public class PricingService {

    public static final String REMITTANCE = "INTERNATIONAL_REMITTANCE";

    private final ExchangeRateRepository exchangeRateRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final RemittanceQuoteRepository remittanceQuoteRepository;

    public PricingService(ExchangeRateRepository exchangeRateRepository, FeeRuleRepository feeRuleRepository,
                          RemittanceQuoteRepository remittanceQuoteRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.remittanceQuoteRepository = remittanceQuoteRepository;
    }

    /**
     * Creates and persists a short-lived remittance quote.
     */
    @Transactional
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
        RemittanceQuoteEntity quote = new RemittanceQuoteEntity();
        quote.setQuoteId("QT" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        quote.setSourceCurrency(normalizedSource);
        quote.setTargetCurrency(normalizedTarget);
        quote.setSourceAmount(normalizedAmount);
        quote.setExchangeRate(rate.getRate());
        quote.setFee(fee);
        quote.setTargetAmount(targetAmount);
        quote.setFeeRuleCode(feeRule.getRuleCode());
        quote.setRateCode(rate.getRateCode());
        quote.setStatus(QuoteStatus.ACTIVE);
        quote.setCreatedAt(Instant.now());
        quote.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        return toResponse(remittanceQuoteRepository.save(quote));
    }

    /**
     * Calculates only the fee for legacy callers that do not need a persisted quote.
     */
    public BigDecimal calculateRemittanceFee(String sourceCurrency, String targetCurrency, BigDecimal sourceAmount) {
        String normalizedSource = sourceCurrency.toUpperCase();
        String normalizedTarget = targetCurrency.toUpperCase();
        FeeRuleEntity feeRule = feeRuleRepository
                .findFirstByBusinessTypeAndSourceCurrencyAndTargetCurrencyAndStatusOrderByIdDesc(
                        REMITTANCE, normalizedSource, normalizedTarget, RuleStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("FEE_RULE_NOT_FOUND", "active fee rule not found"));
        return calculateFee(MoneyUtils.normalize(sourceAmount, normalizedSource), feeRule);
    }

    /**
     * Consumes a persisted quote after locking and validating request attributes.
     */
    @Transactional
    public QuoteResponse useQuote(String quoteId, String sourceCurrency, String targetCurrency, BigDecimal sourceAmount) {
        RemittanceQuoteEntity quote = remittanceQuoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new BusinessException("QUOTE_NOT_FOUND", "remittance quote not found"));
        if (quote.getStatus() != QuoteStatus.ACTIVE || quote.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("QUOTE_EXPIRED", "remittance quote is not active");
        }
        if (!quote.getSourceCurrency().equalsIgnoreCase(sourceCurrency)
                || !quote.getTargetCurrency().equalsIgnoreCase(targetCurrency)
                || quote.getSourceAmount().compareTo(MoneyUtils.normalize(sourceAmount, sourceCurrency.toUpperCase())) != 0) {
            throw new BusinessException("QUOTE_MISMATCH", "quote does not match remittance request");
        }
        quote.setStatus(QuoteStatus.USED);
        return toResponse(quote);
    }

    private QuoteResponse toResponse(RemittanceQuoteEntity quote) {
        return new QuoteResponse(quote.getQuoteId(), quote.getSourceCurrency(), quote.getTargetCurrency(),
                quote.getSourceAmount(), quote.getExchangeRate(), quote.getFee(), quote.getTargetAmount(),
                quote.getFeeRuleCode(), quote.getRateCode(), quote.getExpiresAt());
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
