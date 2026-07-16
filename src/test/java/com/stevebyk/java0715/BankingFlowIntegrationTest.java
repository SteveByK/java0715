package com.stevebyk.java0715;

import com.stevebyk.java0715.account.AccountResponse;
import com.stevebyk.java0715.account.AccountService;
import com.stevebyk.java0715.account.AccountStatus;
import com.stevebyk.java0715.account.AccountType;
import com.stevebyk.java0715.account.CreateAccountRequest;
import com.stevebyk.java0715.account.DepositRequest;
import com.stevebyk.java0715.account.UpdateAccountStatusRequest;
import com.stevebyk.java0715.account.UserRegion;
import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.ledger.LedgerService;
import com.stevebyk.java0715.remittance.RemittanceRequest;
import com.stevebyk.java0715.remittance.RemittanceResponse;
import com.stevebyk.java0715.remittance.RemittanceService;
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
        RemittanceResponse remittance = remittanceService.remit(new RemittanceRequest("rm-1001",
                cnySender.accountNo(), usdReceiver.accountNo(), new BigDecimal("700.00"), "CNY", "USD",
                new BigDecimal("0.14000000"), "US", "BOFAUS3N", null, "family support"));

        assertThat(transfer.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(remittance.status()).isEqualTo(TransactionStatus.SUCCESS);
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
}
