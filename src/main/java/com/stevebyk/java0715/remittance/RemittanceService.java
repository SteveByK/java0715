package com.stevebyk.java0715.remittance;

import com.stevebyk.java0715.account.AccountEntity;
import com.stevebyk.java0715.account.AccountService;
import com.stevebyk.java0715.audit.AuditService;
import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.MoneyUtils;
import com.stevebyk.java0715.idempotency.IdempotencyService;
import com.stevebyk.java0715.lock.AccountLockExecutor;
import com.stevebyk.java0715.outbox.OutboxService;
import com.stevebyk.java0715.risk.RiskDecision;
import com.stevebyk.java0715.risk.RiskService;
import com.stevebyk.java0715.transfer.TransactionStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemittanceService {

    private final AccountService accountService;
    private final RemittanceOrderRepository remittanceOrderRepository;
    private final RiskService riskService;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final AccountLockExecutor accountLockExecutor;
    private final BigDecimal defaultFeeRate;

    public RemittanceService(AccountService accountService, RemittanceOrderRepository remittanceOrderRepository,
                             RiskService riskService, IdempotencyService idempotencyService, AuditService auditService,
                             OutboxService outboxService, AccountLockExecutor accountLockExecutor,
                             @Value("${bank.transfer.default-remittance-fee-rate}") BigDecimal defaultFeeRate) {
        this.accountService = accountService;
        this.remittanceOrderRepository = remittanceOrderRepository;
        this.riskService = riskService;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.accountLockExecutor = accountLockExecutor;
        this.defaultFeeRate = defaultFeeRate;
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
        String orderNo = "RM" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        idempotencyService.ensureFirstRequest(request.requestId(), "INTERNATIONAL_REMITTANCE", orderNo);
        BigDecimal sourceAmount = MoneyUtils.normalize(request.sourceAmount(), request.sourceCurrency().toUpperCase());
        BigDecimal fee = sourceAmount.multiply(defaultFeeRate).setScale(sourceAmount.scale(), RoundingMode.HALF_UP);
        BigDecimal debitAmount = sourceAmount.add(fee);
        BigDecimal targetAmount = MoneyUtils.normalize(sourceAmount.multiply(request.exchangeRate()),
                request.targetCurrency().toUpperCase());
        RemittanceOrderEntity order = createOrder(request, orderNo, sourceAmount, fee, targetAmount);
        RiskDecision riskDecision = riskService.checkRemittance(sourceAmount, request.destinationCountry().toUpperCase());
        if (!riskDecision.approved()) {
            order.setStatus(TransactionStatus.RISK_REJECTED);
            order.setRiskCode(riskDecision.code());
            order.setFailureReason(riskDecision.reason());
            auditService.record(orderNo, "INTERNATIONAL_REMITTANCE", "RISK_REJECTED", riskDecision.reason());
            return RemittanceResponse.from(order);
        }
        AccountEntity sender = accountService.loadForUpdate(request.senderAccountNo());
        AccountEntity receiver = accountService.loadForUpdate(request.receiverAccountNo());
        accountService.ensureCurrency(sender, request.sourceCurrency());
        accountService.ensureCurrency(receiver, request.targetCurrency());
        order.setStatus(TransactionStatus.PROCESSING);
        accountService.debit(sender, debitAmount, orderNo, "REMITTANCE_DEBIT");
        order.setStatus(TransactionStatus.DEBIT_SUCCESS);
        accountService.credit(receiver, targetAmount, orderNo, "REMITTANCE_CREDIT");
        order.setStatus(TransactionStatus.SUCCESS);
        order.setUpdatedAt(Instant.now());
        auditService.record(orderNo, "INTERNATIONAL_REMITTANCE", "SUCCESS", request.remark());
        outboxService.publish(orderNo, "RemittanceCompletedEvent", "{\"orderNo\":\"" + orderNo + "\"}");
        return RemittanceResponse.from(order);
    }

    private RemittanceOrderEntity createOrder(RemittanceRequest request, String orderNo, BigDecimal sourceAmount,
                                              BigDecimal fee, BigDecimal targetAmount) {
        RemittanceOrderEntity entity = new RemittanceOrderEntity();
        entity.setOrderNo(orderNo);
        entity.setRequestId(request.requestId());
        entity.setSenderAccountNo(request.senderAccountNo());
        entity.setReceiverAccountNo(request.receiverAccountNo());
        entity.setSourceAmount(sourceAmount);
        entity.setExchangeRate(request.exchangeRate());
        entity.setFee(fee);
        entity.setTargetAmount(targetAmount);
        entity.setSourceCurrency(request.sourceCurrency().toUpperCase());
        entity.setTargetCurrency(request.targetCurrency().toUpperCase());
        entity.setDestinationCountry(request.destinationCountry().toUpperCase());
        entity.setSwiftCode(request.swiftCode());
        entity.setIban(request.iban());
        entity.setStatus(TransactionStatus.CREATED);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return remittanceOrderRepository.save(entity);
    }
}
