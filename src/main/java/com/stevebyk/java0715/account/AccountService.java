package com.stevebyk.java0715.account;

import com.stevebyk.java0715.audit.AuditService;
import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.MoneyUtils;
import com.stevebyk.java0715.idempotency.IdempotencyService;
import com.stevebyk.java0715.ledger.LedgerDirection;
import com.stevebyk.java0715.ledger.LedgerService;
import com.stevebyk.java0715.outbox.OutboxService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final IdempotencyService idempotencyService;
    private final OutboxService outboxService;

    public AccountService(AccountRepository accountRepository, LedgerService ledgerService, AuditService auditService,
                          IdempotencyService idempotencyService, OutboxService outboxService) {
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.idempotencyService = idempotencyService;
        this.outboxService = outboxService;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        AccountEntity account = new AccountEntity();
        account.setAccountNo("AC" + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
        account.setCustomerId(request.customerId());
        account.setOwnerName(request.ownerName());
        account.setUserRegion(request.userRegion());
        account.setAccountType(request.accountType());
        account.setCurrency(request.currency().toUpperCase());
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        AccountEntity saved = accountRepository.save(account);
        auditService.record(saved.getAccountNo(), "CREATE_ACCOUNT", "SUCCESS", "account opened");
        outboxService.publish(saved.getAccountNo(), "AccountOpenedEvent", "{\"accountNo\":\"" + saved.getAccountNo() + "\"}");
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNo) {
        return accountRepository.findByAccountNo(accountNo)
                .map(AccountResponse::from)
                .orElseThrow(() -> new BusinessException("ACCOUNT_NOT_FOUND", "account not found"));
    }

    @Transactional
    public AccountResponse deposit(String accountNo, DepositRequest request) {
        MoneyUtils.requirePositive(request.amount());
        idempotencyService.ensureFirstRequest(request.requestId(), "DEPOSIT", accountNo);
        AccountEntity account = loadForUpdate(accountNo);
        ensureActive(account);
        ensureCurrency(account, request.currency());
        BigDecimal amount = MoneyUtils.normalize(request.amount(), account.getCurrency());
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        ledgerService.append(request.requestId(), accountNo, LedgerDirection.CREDIT, amount,
                account.getAvailableBalance(), account.getCurrency(), "DEPOSIT");
        auditService.record(request.requestId(), "DEPOSIT", "SUCCESS", request.remark());
        outboxService.publish(request.requestId(), "DepositCompletedEvent", "{\"accountNo\":\"" + accountNo + "\"}");
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse updateStatus(String accountNo, UpdateAccountStatusRequest request) {
        AccountEntity account = loadForUpdate(accountNo);
        account.setStatus(request.status());
        account.setUpdatedAt(Instant.now());
        auditService.record(accountNo, "UPDATE_ACCOUNT_STATUS", "SUCCESS",
                "status=" + request.status() + ", reason=" + request.reason());
        outboxService.publish(accountNo, "AccountStatusChangedEvent",
                "{\"accountNo\":\"" + accountNo + "\",\"status\":\"" + request.status() + "\"}");
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse holdFunds(String accountNo, HoldFundsRequest request) {
        MoneyUtils.requirePositive(request.amount());
        idempotencyService.ensureFirstRequest(request.requestId(), "HOLD_FUNDS", accountNo);
        AccountEntity account = loadForUpdate(accountNo);
        ensureActive(account);
        ensureCurrency(account, request.currency());
        BigDecimal amount = MoneyUtils.normalize(request.amount(), account.getCurrency());
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "available balance is not enough");
        }
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setFrozenBalance(account.getFrozenBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        ledgerService.append(request.requestId(), accountNo, LedgerDirection.DEBIT, amount,
                account.getAvailableBalance(), account.getCurrency(), "HOLD_FUNDS");
        auditService.record(request.requestId(), "HOLD_FUNDS", "SUCCESS", request.reason());
        outboxService.publish(request.requestId(), "FundsHeldEvent", "{\"accountNo\":\"" + accountNo + "\"}");
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse releaseFunds(String accountNo, HoldFundsRequest request) {
        MoneyUtils.requirePositive(request.amount());
        idempotencyService.ensureFirstRequest(request.requestId(), "RELEASE_FUNDS", accountNo);
        AccountEntity account = loadForUpdate(accountNo);
        ensureActive(account);
        ensureCurrency(account, request.currency());
        BigDecimal amount = MoneyUtils.normalize(request.amount(), account.getCurrency());
        if (account.getFrozenBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_FROZEN_BALANCE", "frozen balance is not enough");
        }
        account.setFrozenBalance(account.getFrozenBalance().subtract(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        ledgerService.append(request.requestId(), accountNo, LedgerDirection.CREDIT, amount,
                account.getAvailableBalance(), account.getCurrency(), "RELEASE_FUNDS");
        auditService.record(request.requestId(), "RELEASE_FUNDS", "SUCCESS", request.reason());
        outboxService.publish(request.requestId(), "FundsReleasedEvent", "{\"accountNo\":\"" + accountNo + "\"}");
        return AccountResponse.from(account);
    }

    public AccountEntity loadForUpdate(String accountNo) {
        return accountRepository.findByAccountNoForUpdate(accountNo)
                .orElseThrow(() -> new BusinessException("ACCOUNT_NOT_FOUND", "account not found"));
    }

    public void debit(AccountEntity account, BigDecimal amount, String transactionNo, String entryType) {
        ensureActive(account);
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "available balance is not enough");
        }
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setUpdatedAt(Instant.now());
        ledgerService.append(transactionNo, account.getAccountNo(), LedgerDirection.DEBIT, amount,
                account.getAvailableBalance(), account.getCurrency(), entryType);
    }

    public void credit(AccountEntity account, BigDecimal amount, String transactionNo, String entryType) {
        ensureActive(account);
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        ledgerService.append(transactionNo, account.getAccountNo(), LedgerDirection.CREDIT, amount,
                account.getAvailableBalance(), account.getCurrency(), entryType);
    }

    public void ensureActive(AccountEntity account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "account is not active");
        }
    }

    public void ensureCurrency(AccountEntity account, String currency) {
        if (!account.getCurrency().equalsIgnoreCase(currency)) {
            throw new BusinessException("CURRENCY_MISMATCH", "account currency does not match request currency");
        }
    }
}
