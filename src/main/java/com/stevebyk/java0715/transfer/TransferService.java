package com.stevebyk.java0715.transfer;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountService accountService;
    private final TransferOrderRepository transferOrderRepository;
    private final RiskService riskService;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final AccountLockExecutor accountLockExecutor;

    public TransferService(AccountService accountService, TransferOrderRepository transferOrderRepository,
                           RiskService riskService, IdempotencyService idempotencyService, AuditService auditService,
                           OutboxService outboxService, AccountLockExecutor accountLockExecutor) {
        this.accountService = accountService;
        this.transferOrderRepository = transferOrderRepository;
        this.riskService = riskService;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.accountLockExecutor = accountLockExecutor;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.fromAccountNo().equals(request.toAccountNo())) {
            throw new BusinessException("SAME_ACCOUNT", "source and target account cannot be same");
        }
        return accountLockExecutor.executeWithAccountLocks(
                List.of(request.fromAccountNo(), request.toAccountNo()),
                () -> doTransfer(request));
    }

    @Transactional(readOnly = true)
    public TransferResponse getByOrderNo(String orderNo) {
        return transferOrderRepository.findByOrderNo(orderNo)
                .map(TransferResponse::from)
                .orElseThrow(() -> new BusinessException("TRANSFER_NOT_FOUND", "transfer order not found"));
    }

    private TransferResponse doTransfer(TransferRequest request) {
        MoneyUtils.requirePositive(request.amount());
        String orderNo = "TR" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        idempotencyService.ensureFirstRequest(request.requestId(), "DOMESTIC_TRANSFER", orderNo);
        BigDecimal amount = MoneyUtils.normalize(request.amount(), request.currency().toUpperCase());
        TransferOrderEntity order = createOrder(request, orderNo, amount);
        RiskDecision riskDecision = riskService.checkDomesticTransfer(amount);
        if (!riskDecision.approved()) {
            order.setStatus(TransactionStatus.RISK_REJECTED);
            order.setRiskCode(riskDecision.code());
            order.setFailureReason(riskDecision.reason());
            auditService.record(orderNo, "DOMESTIC_TRANSFER", "RISK_REJECTED", riskDecision.reason());
            return TransferResponse.from(order);
        }
        AccountEntity fromAccount = accountService.loadForUpdate(request.fromAccountNo());
        AccountEntity toAccount = accountService.loadForUpdate(request.toAccountNo());
        accountService.ensureCurrency(fromAccount, request.currency());
        accountService.ensureCurrency(toAccount, request.currency());
        order.setStatus(TransactionStatus.PROCESSING);
        accountService.debit(fromAccount, amount, orderNo, "DOMESTIC_TRANSFER");
        order.setStatus(TransactionStatus.DEBIT_SUCCESS);
        accountService.credit(toAccount, amount, orderNo, "DOMESTIC_TRANSFER");
        order.setStatus(TransactionStatus.SUCCESS);
        order.setUpdatedAt(Instant.now());
        auditService.record(orderNo, "DOMESTIC_TRANSFER", "SUCCESS", request.remark());
        outboxService.publish(orderNo, "TransferSucceededEvent", "{\"orderNo\":\"" + orderNo + "\"}");
        return TransferResponse.from(order);
    }

    private TransferOrderEntity createOrder(TransferRequest request, String orderNo, BigDecimal amount) {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setOrderNo(orderNo);
        entity.setRequestId(request.requestId());
        entity.setFromAccountNo(request.fromAccountNo());
        entity.setToAccountNo(request.toAccountNo());
        entity.setAmount(amount);
        entity.setFee(BigDecimal.ZERO.setScale(amount.scale()));
        entity.setCurrency(request.currency().toUpperCase());
        entity.setStatus(TransactionStatus.CREATED);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return transferOrderRepository.save(entity);
    }
}
