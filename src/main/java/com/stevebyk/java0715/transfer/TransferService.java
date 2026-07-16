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
    private final ReversalOrderRepository reversalOrderRepository;

    public TransferService(AccountService accountService, TransferOrderRepository transferOrderRepository,
                           RiskService riskService, IdempotencyService idempotencyService, AuditService auditService,
                           OutboxService outboxService, AccountLockExecutor accountLockExecutor,
                           ReversalOrderRepository reversalOrderRepository) {
        this.accountService = accountService;
        this.transferOrderRepository = transferOrderRepository;
        this.riskService = riskService;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.accountLockExecutor = accountLockExecutor;
        this.reversalOrderRepository = reversalOrderRepository;
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

    @Transactional
    public ReversalResponse reverse(String orderNo, ReversalRequest request) {
        TransferOrderEntity original = transferOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("TRANSFER_NOT_FOUND", "transfer order not found"));
        if (original.getStatus() != TransactionStatus.SUCCESS) {
            throw new BusinessException("TRANSFER_NOT_REVERSIBLE", "only successful transfer can be reversed");
        }
        if (reversalOrderRepository.findByOriginalOrderNo(orderNo).isPresent()) {
            throw new BusinessException("TRANSFER_ALREADY_REVERSED", "transfer has already been reversed");
        }
        return accountLockExecutor.executeWithAccountLocks(
                List.of(original.getFromAccountNo(), original.getToAccountNo()),
                () -> doReverse(original, request));
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

    private ReversalResponse doReverse(TransferOrderEntity original, ReversalRequest request) {
        String reversalNo = "RV" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        idempotencyService.ensureFirstRequest(request.requestId(), "TRANSFER_REVERSAL", reversalNo);
        AccountEntity originalSender = accountService.loadForUpdate(original.getFromAccountNo());
        AccountEntity originalReceiver = accountService.loadForUpdate(original.getToAccountNo());
        accountService.ensureCurrency(originalSender, original.getCurrency());
        accountService.ensureCurrency(originalReceiver, original.getCurrency());

        ReversalOrderEntity reversal = new ReversalOrderEntity();
        reversal.setReversalNo(reversalNo);
        reversal.setOriginalOrderNo(original.getOrderNo());
        reversal.setRequestId(request.requestId());
        reversal.setAmount(original.getAmount());
        reversal.setCurrency(original.getCurrency());
        reversal.setStatus(TransactionStatus.PROCESSING);
        reversal.setReason(request.reason());
        reversal.setCreatedAt(Instant.now());
        reversal.setUpdatedAt(Instant.now());
        ReversalOrderEntity saved = reversalOrderRepository.save(reversal);

        accountService.debit(originalReceiver, original.getAmount(), reversalNo, "TRANSFER_REVERSAL_DEBIT");
        accountService.credit(originalSender, original.getAmount(), reversalNo, "TRANSFER_REVERSAL_CREDIT");
        saved.setStatus(TransactionStatus.SUCCESS);
        saved.setUpdatedAt(Instant.now());
        original.setStatus(TransactionStatus.REVERSED);
        original.setUpdatedAt(Instant.now());
        auditService.record(reversalNo, "TRANSFER_REVERSAL", "SUCCESS", request.reason());
        outboxService.publish(reversalNo, "TransferReversedEvent", "{\"reversalNo\":\"" + reversalNo + "\"}");
        return ReversalResponse.from(saved);
    }
}
