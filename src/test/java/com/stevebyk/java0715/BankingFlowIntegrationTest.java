package com.stevebyk.java0715;

import com.stevebyk.java0715.account.AccountResponse;
import com.stevebyk.java0715.account.AccountService;
import com.stevebyk.java0715.account.AccountStatus;
import com.stevebyk.java0715.account.AccountType;
import com.stevebyk.java0715.account.CreateAccountRequest;
import com.stevebyk.java0715.account.DepositRequest;
import com.stevebyk.java0715.account.HoldFundsRequest;
import com.stevebyk.java0715.account.UpdateAccountStatusRequest;
import com.stevebyk.java0715.account.UserRegion;
import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.customer.CreateCustomerRequest;
import com.stevebyk.java0715.customer.CustomerResponse;
import com.stevebyk.java0715.customer.CustomerService;
import com.stevebyk.java0715.customer.KycLevel;
import com.stevebyk.java0715.customer.KycStatus;
import com.stevebyk.java0715.customer.ReviewKycRequest;
import com.stevebyk.java0715.customer.SubmitKycRequest;
import com.stevebyk.java0715.ledger.LedgerService;
import com.stevebyk.java0715.outbox.OutboxEventResponse;
import com.stevebyk.java0715.outbox.OutboxService;
import com.stevebyk.java0715.pricing.PricingService;
import com.stevebyk.java0715.pricing.QuoteResponse;
import com.stevebyk.java0715.remittance.RemittanceRequest;
import com.stevebyk.java0715.remittance.RemittanceResponse;
import com.stevebyk.java0715.remittance.RemittanceService;
import com.stevebyk.java0715.transfer.ReversalRequest;
import com.stevebyk.java0715.transfer.ReversalResponse;
import com.stevebyk.java0715.transfer.TransactionStatus;
import com.stevebyk.java0715.transfer.TransferRequest;
import com.stevebyk.java0715.transfer.TransferResponse;
import com.stevebyk.java0715.transfer.TransferService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BankingFlowIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private RemittanceService remittanceService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private OutboxService outboxService;

    @Test
    void shouldDepositTransferAndRemit() {
        AccountResponse cnySender = accountService.createAccount(new CreateAccountRequest(
                "C1001", "Zhang San", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        AccountResponse cnyReceiver = accountService.createAccount(new CreateAccountRequest(
                "C1002", "Li Si", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        AccountResponse usdReceiver = accountService.createAccount(new CreateAccountRequest(
                "U9001", "Alice Smith", UserRegion.OVERSEAS, AccountType.FOREIGN_CURRENCY, "USD"));

        accountService.deposit(cnySender.accountNo(), new DepositRequest("dep-1001",
                new BigDecimal("10000.00"), "CNY", "initial deposit"));
        TransferResponse transfer = transferService.transfer(new TransferRequest("tr-1001",
                cnySender.accountNo(), cnyReceiver.accountNo(), new BigDecimal("1200.00"), "CNY", "rent"));
        QuoteResponse quote = pricingService.quoteRemittance("CNY", "USD", new BigDecimal("700.00"));
        RemittanceResponse remittance = remittanceService.remit(new RemittanceRequest("rm-1001",
                cnySender.accountNo(), usdReceiver.accountNo(), new BigDecimal("700.00"), "CNY", "USD",
                null, quote.quoteId(), "US", "BOFAUS3N", null, "family support"));

        assertThat(transfer.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(remittance.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(remittance.quoteId()).isEqualTo(quote.quoteId());
        assertThat(accountService.getAccount(cnyReceiver.accountNo()).availableBalance())
                .isEqualByComparingTo("1200.00");
        assertThat(accountService.getAccount(usdReceiver.accountNo()).availableBalance())
                .isEqualByComparingTo("98.00");
    }

    @Test
    void shouldRejectHighRiskDomesticTransferBeforeDebit() {
        AccountResponse sender = accountService.createAccount(new CreateAccountRequest(
                "C2001", "Wang Wu", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        AccountResponse receiver = accountService.createAccount(new CreateAccountRequest(
                "C2002", "Zhao Liu", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        accountService.deposit(sender.accountNo(), new DepositRequest("dep-2001",
                new BigDecimal("300000.00"), "CNY", "large deposit"));

        TransferResponse response = transferService.transfer(new TransferRequest("tr-2001",
                sender.accountNo(), receiver.accountNo(), new BigDecimal("250000.00"), "CNY", "large payment"));

        assertThat(response.status()).isEqualTo(TransactionStatus.RISK_REJECTED);
        assertThat(accountService.getAccount(sender.accountNo()).availableBalance())
                .isEqualByComparingTo("300000.00");
    }

    @Test
    void shouldLoadGeneratedDemoData() {
        AccountResponse demoAccount = accountService.getAccount("AC_DEMO_CNY_001");
        TransferResponse transfer = transferService.getByOrderNo("TR_DEMO_SUCCESS");
        RemittanceResponse remittance = remittanceService.getByOrderNo("RM_DEMO_SUCCESS");

        assertThat(demoAccount.availableBalance()).isEqualByComparingTo("97797.90");
        assertThat(transfer.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(remittance.targetAmount()).isEqualByComparingTo("98.00");
        assertThat(ledgerService.findByTransactionNo("TR_DEMO_SUCCESS")).hasSize(2);
    }

    @Test
    void shouldBlockDepositWhenAccountIsFrozenThenAllowAfterActivation() {
        assertThat(accountService.getAccount("AC_DEMO_CNY_FROZEN").status()).isEqualTo(AccountStatus.FROZEN);

        assertThatThrownBy(() -> accountService.deposit("AC_DEMO_CNY_FROZEN", new DepositRequest("dep-frozen-001",
                new BigDecimal("100.00"), "CNY", "blocked")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("account is not active");

        accountService.updateStatus("AC_DEMO_CNY_FROZEN",
                new UpdateAccountStatusRequest(AccountStatus.ACTIVE, "test activation"));
        AccountResponse updated = accountService.deposit("AC_DEMO_CNY_FROZEN", new DepositRequest("dep-frozen-002",
                new BigDecimal("100.00"), "CNY", "allowed"));

        assertThat(updated.availableBalance()).isEqualByComparingTo("5100.00");
    }

    @Test
    void shouldCreateCustomerSubmitAndReviewKyc() {
        String customerId = "CUI" + System.nanoTime();

        CustomerResponse created = customerService.createCustomer(new CreateCustomerRequest(
                customerId, "Frontend Kyc User", UserRegion.DOMESTIC, "CN", "+8613900000000", "kyc@example.com"));
        CustomerResponse submitted = customerService.submitKyc(customerId, new SubmitKycRequest(
                "NATIONAL_ID", "110***********999", KycLevel.STANDARD));
        CustomerResponse reviewed = customerService.reviewKyc(customerId, new ReviewKycRequest(
                KycStatus.APPROVED, "tester"));

        assertThat(created.kycStatus()).isNull();
        assertThat(submitted.kycStatus()).isEqualTo(KycStatus.PENDING);
        assertThat(reviewed.kycStatus()).isEqualTo(KycStatus.APPROVED);
    }

    @Test
    void shouldQuoteRemittanceUsingSeededRules() {
        QuoteResponse quote = pricingService.quoteRemittance("CNY", "USD", new BigDecimal("700.00"));

        assertThat(quote.quoteId()).isNotBlank();
        assertThat(quote.exchangeRate()).isEqualByComparingTo("0.14000000");
        assertThat(quote.fee()).isEqualByComparingTo("2.10");
        assertThat(quote.targetAmount()).isEqualByComparingTo("98.00");
        assertThat(quote.feeRuleCode()).isEqualTo("FEE_RM_CNY_USD");
        assertThat(quote.expiresAt()).isNotNull();
    }

    @Test
    void shouldHoldAndReleaseFunds() {
        AccountResponse account = accountService.createAccount(new CreateAccountRequest(
                "C3001", "Hold User", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        accountService.deposit(account.accountNo(), new DepositRequest("dep-hold-001",
                new BigDecimal("1000.00"), "CNY", "hold setup"));

        AccountResponse held = accountService.holdFunds(account.accountNo(), new HoldFundsRequest(
                "hold-001", new BigDecimal("300.00"), "CNY", "card authorization"));
        AccountResponse released = accountService.releaseFunds(account.accountNo(), new HoldFundsRequest(
                "release-001", new BigDecimal("120.00"), "CNY", "partial release"));

        assertThat(held.availableBalance()).isEqualByComparingTo("700.00");
        assertThat(held.frozenBalance()).isEqualByComparingTo("300.00");
        assertThat(released.availableBalance()).isEqualByComparingTo("820.00");
        assertThat(released.frozenBalance()).isEqualByComparingTo("180.00");
    }

    @Test
    void shouldReverseSuccessfulDomesticTransferOnlyOnce() {
        AccountResponse sender = accountService.createAccount(new CreateAccountRequest(
                "C4001", "Reverse Sender", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        AccountResponse receiver = accountService.createAccount(new CreateAccountRequest(
                "C4002", "Reverse Receiver", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        accountService.deposit(sender.accountNo(), new DepositRequest("dep-reversal-001",
                new BigDecimal("1000.00"), "CNY", "reversal setup"));
        TransferResponse transfer = transferService.transfer(new TransferRequest("tr-reversal-001",
                sender.accountNo(), receiver.accountNo(), new BigDecimal("250.00"), "CNY", "test transfer"));

        ReversalResponse reversal = transferService.reverse(transfer.orderNo(), new ReversalRequest(
                "rv-reversal-001", "customer dispute"));

        assertThat(reversal.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(accountService.getAccount(sender.accountNo()).availableBalance()).isEqualByComparingTo("1000.00");
        assertThat(accountService.getAccount(receiver.accountNo()).availableBalance()).isEqualByComparingTo("0.00");
        assertThat(transferService.getByOrderNo(transfer.orderNo()).status()).isEqualTo(TransactionStatus.REVERSED);
        assertThat(ledgerService.findByTransactionNo(reversal.reversalNo())).hasSize(2);
        assertThatThrownBy(() -> transferService.reverse(transfer.orderNo(), new ReversalRequest(
                "rv-reversal-002", "duplicate")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("successful transfer");
    }

    @Test
    void shouldReturnOriginalDomesticTransferForDuplicateRequest() {
        AccountResponse sender = accountService.createAccount(new CreateAccountRequest(
                "C5001", "Idempotent Sender", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        AccountResponse receiver = accountService.createAccount(new CreateAccountRequest(
                "C5002", "Idempotent Receiver", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        accountService.deposit(sender.accountNo(), new DepositRequest("dep-idem-001",
                new BigDecimal("1000.00"), "CNY", "idempotency setup"));

        TransferRequest request = new TransferRequest("tr-idem-001",
                sender.accountNo(), receiver.accountNo(), new BigDecimal("120.00"), "CNY", "retry case");
        TransferResponse first = transferService.transfer(request);
        TransferResponse second = transferService.transfer(request);

        assertThat(second.orderNo()).isEqualTo(first.orderNo());
        assertThat(accountService.getAccount(sender.accountNo()).availableBalance()).isEqualByComparingTo("880.00");
        assertThat(accountService.getAccount(receiver.accountNo()).availableBalance()).isEqualByComparingTo("120.00");
    }

    @Test
    void shouldConsumeRemittanceQuoteOnlyOnce() {
        AccountResponse cnySender = accountService.createAccount(new CreateAccountRequest(
                "C6001", "Quote Sender", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));
        AccountResponse usdReceiver = accountService.createAccount(new CreateAccountRequest(
                "U6001", "Quote Receiver", UserRegion.OVERSEAS, AccountType.FOREIGN_CURRENCY, "USD"));
        accountService.deposit(cnySender.accountNo(), new DepositRequest("dep-quote-001",
                new BigDecimal("2000.00"), "CNY", "quote setup"));
        QuoteResponse quote = pricingService.quoteRemittance("CNY", "USD", new BigDecimal("100.00"));

        RemittanceResponse first = remittanceService.remit(new RemittanceRequest("rm-quote-001",
                cnySender.accountNo(), usdReceiver.accountNo(), new BigDecimal("100.00"), "CNY", "USD",
                null, quote.quoteId(), "US", "BOFAUS3N", null, "first quote use"));

        assertThat(first.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThatThrownBy(() -> remittanceService.remit(new RemittanceRequest("rm-quote-002",
                cnySender.accountNo(), usdReceiver.accountNo(), new BigDecimal("100.00"), "CNY", "USD",
                null, quote.quoteId(), "US", "BOFAUS3N", null, "quote reuse")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("remittance quote is not active");
    }

    @Test
    void shouldPublishPendingOutboxEvents() {
        AccountResponse account = accountService.createAccount(new CreateAccountRequest(
                "C7001", "Outbox User", UserRegion.DOMESTIC, AccountType.SAVINGS, "CNY"));

        var beforePublish = outboxService.findByAggregateId(account.accountNo());
        assertThat(beforePublish).extracting(OutboxEventResponse::status).contains("NEW");

        var published = outboxService.publishPending();

        assertThat(published).extracting(OutboxEventResponse::status).contains("PUBLISHED");
        assertThat(outboxService.findByAggregateId(account.accountNo()))
                .extracting(OutboxEventResponse::status)
                .contains("PUBLISHED");
    }
}
