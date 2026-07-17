package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.account.AccountEntity;
import com.stevebyk.java0715.account.AccountService;
import com.stevebyk.java0715.audit.AuditService;
import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.MoneyUtils;
import com.stevebyk.java0715.common.ddd.ApplicationServiceRole;
import com.stevebyk.java0715.idempotency.IdempotencyService;
import com.stevebyk.java0715.lock.AccountLockExecutor;
import com.stevebyk.java0715.outbox.OutboxService;
import com.stevebyk.java0715.pricing.PricingService;
import com.stevebyk.java0715.pricing.QuoteResponse;
import com.stevebyk.java0715.risk.RiskDecision;
import com.stevebyk.java0715.risk.RiskService;
import com.stevebyk.java0715.transfer.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for international remittance orchestration.
 *
 * <p>The service consumes a locked quote, applies remittance risk policy, moves
 * funds through account services and records traceability for exchange-rate and
 * fee decisions.</p>
 */
@Service
@ApplicationServiceRole
public class RemittanceService {

    private final AccountService accountService;
    private final RemittanceOrderRepository remittanceOrderRepository;
    private final RiskService riskService;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final AccountLockExecutor accountLockExecutor;
    private final PricingService pricingService;

    public RemittanceService(AccountService accountService, RemittanceOrderRepository remittanceOrderRepository,
                             RiskService riskService, IdempotencyService idempotencyService, AuditService auditService,
                             OutboxService outboxService, AccountLockExecutor accountLockExecutor,
                             PricingService pricingService) {
        this.accountService = accountService;
        this.remittanceOrderRepository = remittanceOrderRepository;
        this.riskService = riskService;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.accountLockExecutor = accountLockExecutor;
        this.pricingService = pricingService;
    }

    @Transactional
    public RemittanceResponse remit(RemittanceRequest request) {
        return accountLockExecutor.executeWithAccountLocks(
                List.of(request.senderAccountNo(), request.receiverAccountNo()),
                () -> doRemit(request));
    }

    @Transactional(readOnly = true)
    public RemittanceResponse getByOrderNo(String orderNo) {
        return remittanceOrderRepository.findByOrderNo(orderNo)
                .map(RemittanceResponse::from)
                .orElseThrow(() -> new BusinessException("REMITTANCE_NOT_FOUND", "remittance order not found"));
    }

    private RemittanceResponse doRemit(RemittanceRequest request) {
        MoneyUtils.requirePositive(request.sourceAmount());
        RemittanceResponse existing = remittanceOrderRepository.findByRequestId(request.requestId())
                .map(RemittanceResponse::from)
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        String orderNo = "RM" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        idempotencyService.ensureFirstRequest(request.requestId(), "INTERNATIONAL_REMITTANCE", orderNo);
        BigDecimal sourceAmount = MoneyUtils.normalize(request.sourceAmount(), request.sourceCurrency().toUpperCase());
        RiskDecision riskDecision = riskService.checkRemittance(sourceAmount, request.destinationCountry().toUpperCase());
        QuoteResponse quote = null;
        if (riskDecision.approved()) {
            quote = resolveQuote(request, sourceAmount);
        } else if (request.exchangeRate() != null) {
            quote = fallbackRejectedQuote(request, sourceAmount);
        } else {
            quote = pricingService.quoteRemittance(request.sourceCurrency(), request.targetCurrency(), sourceAmount);
        }
        BigDecimal debitAmount = sourceAmount.add(quote.fee());
        RemittanceOrderEntity order = createOrder(request, orderNo, sourceAmount, quote);
        if (!riskDecision.approved()) {
            order.setStatus(TransactionStatus.RISK_REJECTED);
            order.setRiskCode(riskDecision.code());
            order.setFailureReason(riskDecision.reason());
            auditService.record(orderNo, "INTERNATIONAL_REMITTANCE", "RISK_REJECTED", riskDecision.reason());
            RemittanceResponse response = RemittanceResponse.from(order);
            idempotencyService.markCompleted(request.requestId(), "INTERNATIONAL_REMITTANCE", response.toString());
            return response;
        }
        AccountEntity sender = accountService.loadForUpdate(request.senderAccountNo());
        AccountEntity receiver = accountService.loadForUpdate(request.receiverAccountNo());
        accountService.ensureCurrency(sender, request.sourceCurrency());
        accountService.ensureCurrency(receiver, request.targetCurrency());
        order.setStatus(TransactionStatus.PROCESSING);
        accountService.debit(sender, debitAmount, orderNo, "REMITTANCE_DEBIT");
        order.setStatus(TransactionStatus.DEBIT_SUCCESS);
        accountService.credit(receiver, quote.targetAmount(), orderNo, "REMITTANCE_CREDIT");
        order.setStatus(TransactionStatus.SUCCESS);
        order.setUpdatedAt(Instant.now());
        auditService.record(orderNo, "INTERNATIONAL_REMITTANCE", "SUCCESS", request.remark());
        outboxService.publish(orderNo, "RemittanceCompletedEvent", "{\"orderNo\":\"" + orderNo + "\"}");
        RemittanceResponse response = RemittanceResponse.from(order);
        idempotencyService.markCompleted(request.requestId(), "INTERNATIONAL_REMITTANCE", response.toString());
        return response;
    }

    private QuoteResponse resolveQuote(RemittanceRequest request, BigDecimal sourceAmount) {
        if (request.quoteId() == null || request.quoteId().isBlank()) {
            QuoteResponse quote = pricingService.quoteRemittance(request.sourceCurrency(), request.targetCurrency(), sourceAmount);
            return pricingService.useQuote(quote.quoteId(), request.sourceCurrency(), request.targetCurrency(), sourceAmount);
        }
        return pricingService.useQuote(request.quoteId(), request.sourceCurrency(), request.targetCurrency(), sourceAmount);
    }

    private QuoteResponse fallbackRejectedQuote(RemittanceRequest request, BigDecimal sourceAmount) {
        BigDecimal targetAmount = MoneyUtils.normalize(sourceAmount.multiply(request.exchangeRate()),
                request.targetCurrency().toUpperCase());
        return new QuoteResponse(null, request.sourceCurrency().toUpperCase(), request.targetCurrency().toUpperCase(),
                sourceAmount, request.exchangeRate(), BigDecimal.ZERO.setScale(sourceAmount.scale()), targetAmount,
                "RISK_REJECTED_NO_FEE", "CLIENT_SUPPLIED_REJECTED", Instant.now());
    }

    private RemittanceOrderEntity createOrder(RemittanceRequest request, String orderNo, BigDecimal sourceAmount,
                                              QuoteResponse quote) {
        RemittanceOrderEntity entity = new RemittanceOrderEntity();
        entity.setOrderNo(orderNo);
        entity.setRequestId(request.requestId());
        entity.setSenderAccountNo(request.senderAccountNo());
        entity.setReceiverAccountNo(request.receiverAccountNo());
        entity.setSourceAmount(sourceAmount);
        entity.setExchangeRate(quote.exchangeRate());
        entity.setFee(quote.fee());
        entity.setTargetAmount(quote.targetAmount());
        entity.setQuoteId(quote.quoteId());
        entity.setFeeRuleCode(quote.feeRuleCode());
        entity.setRateCode(quote.rateCode());
        entity.setSourceCurrency(quote.sourceCurrency());
        entity.setTargetCurrency(quote.targetCurrency());
        entity.setDestinationCountry(request.destinationCountry().toUpperCase());
        entity.setSwiftCode(request.swiftCode());
        entity.setIban(request.iban());
        entity.setStatus(TransactionStatus.CREATED);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return remittanceOrderRepository.save(entity);
    }
}
