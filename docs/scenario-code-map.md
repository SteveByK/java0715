# Scenario to Code Map

This document maps each banking scenario to API endpoints, core code and database tables.

## 1. Domestic Account Opening

Purpose:

- Open a domestic CNY account or overseas foreign-currency account.

API:

- `POST /api/v1/accounts`
- `GET /api/v1/accounts/{accountNo}`

Core code:

- `account/AccountController.java`
- `account/AccountService.java`
- `account/AccountEntity.java`
- `account/AccountRepository.java`

Tables:

- `account_balance`
- `audit_log`
- `outbox_event`

Seed data:

- `AC_DEMO_CNY_001`
- `AC_DEMO_CNY_002`
- `AC_DEMO_USD_001`
- `AC_DEMO_USD_002`

## 2. Domestic Account Deposit

Purpose:

- Deposit money into an active account.

API:

- `POST /api/v1/accounts/{accountNo}/deposits`
- `GET /api/v1/ledger/transactions/{transactionNo}`
- `GET /api/v1/audit/{businessNo}`

Core code:

- `account/AccountService.deposit`
- `idempotency/IdempotencyService`
- `ledger/LedgerService`
- `audit/AuditService`
- `outbox/OutboxService`

Tables:

- `account_balance`
- `ledger_entry`
- `idempotency_record`
- `audit_log`
- `outbox_event`

Seed data:

- `DEMO_DEP_001`
- `LE_DEMO_DEP_001`

## 3. Domestic Transfer Success

Purpose:

- Move CNY from one domestic account to another.

API:

- `POST /api/v1/transfers/domestic`
- `GET /api/v1/transfers/{orderNo}`
- `GET /api/v1/ledger/transactions/{transactionNo}`

Core code:

- `transfer/TransferController.java`
- `transfer/TransferService.java`
- `transfer/TransferOrderEntity.java`
- `lock/LocalAccountLockExecutor.java`
- `risk/RiskService.java`
- `account/AccountService.debit`
- `account/AccountService.credit`

Tables:

- `transfer_order`
- `account_balance`
- `ledger_entry`
- `idempotency_record`
- `audit_log`
- `outbox_event`

Seed data:

- `TR_DEMO_SUCCESS`
- `LE_DEMO_TR_001_D`
- `LE_DEMO_TR_001_C`

## 4. Domestic Transfer Risk Rejection

Purpose:

- Reject a high-amount domestic transfer before debit.

API:

- `POST /api/v1/transfers/domestic`
- `GET /api/v1/transfers/TR_DEMO_RISK_REJECTED`
- `GET /api/v1/audit/TR_DEMO_RISK_REJECTED`

Core code:

- `risk/RiskService.checkDomesticTransfer`
- `transfer/TransferService.doTransfer`

Tables:

- `transfer_order`
- `idempotency_record`
- `audit_log`

Seed data:

- `TR_DEMO_RISK_REJECTED`
- `AC_DEMO_CNY_HIGH`

## 5. International Remittance Success

Purpose:

- Debit source currency plus fee and credit target currency after exchange-rate conversion.

API:

- `POST /api/v1/remittances`
- `GET /api/v1/remittances/{orderNo}`
- `GET /api/v1/ledger/transactions/{transactionNo}`

Core code:

- `remittance/RemittanceController.java`
- `remittance/RemittanceService.java`
- `remittance/RemittanceOrderEntity.java`
- `risk/RiskService.checkRemittance`
- `account/AccountService.debit`
- `account/AccountService.credit`

Tables:

- `remittance_order`
- `account_balance`
- `ledger_entry`
- `idempotency_record`
- `audit_log`
- `outbox_event`

Seed data:

- `RM_DEMO_SUCCESS`
- `LE_DEMO_RM_001_D`
- `LE_DEMO_RM_001_C`

## 6. International Remittance Risk Rejection

Purpose:

- Reject a remittance to a blocked destination country or a high-risk amount.

API:

- `POST /api/v1/remittances`
- `GET /api/v1/remittances/RM_DEMO_RISK_REJECTED`
- `GET /api/v1/audit/RM_DEMO_RISK_REJECTED`

Core code:

- `risk/RiskService.checkRemittance`
- `remittance/RemittanceService.doRemit`

Tables:

- `remittance_order`
- `idempotency_record`
- `audit_log`

Seed data:

- `RM_DEMO_RISK_REJECTED`

## 7. Account Freeze and Unfreeze

Purpose:

- Freeze an account so it cannot deposit, debit or receive credit, then reactivate it later.

API:

- `PATCH /api/v1/accounts/{accountNo}/status`
- `GET /api/v1/accounts/{accountNo}`

Core code:

- `account/AccountController.updateStatus`
- `account/AccountService.updateStatus`
- `account/AccountService.ensureActive`

Tables:

- `account_balance`
- `audit_log`
- `outbox_event`

Seed data:

- `AC_DEMO_CNY_FROZEN`

## 8. Ledger, Audit and Outbox Queries

Purpose:

- Verify money movement, operational audit and future event publishing.

API:

- `GET /api/v1/ledger/transactions/{transactionNo}`
- `GET /api/v1/ledger/accounts/{accountNo}`
- `GET /api/v1/audit/{businessNo}`
- `GET /api/v1/outbox/{aggregateId}`

Core code:

- `ledger/LedgerController.java`
- `audit/AuditController.java`
- `outbox/OutboxController.java`

Tables:

- `ledger_entry`
- `audit_log`
- `outbox_event`

Seed data:

- `TR_DEMO_SUCCESS`
- `RM_DEMO_SUCCESS`
- `AC_DEMO_CNY_001`
