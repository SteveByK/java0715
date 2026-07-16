# Database Documentation

Flyway migration file:

```text
src/main/resources/db/migration/V1__init_bank_schema.sql
src/main/resources/db/migration/V2__seed_demo_banking_data.sql
src/main/resources/db/migration/V3__advanced_banking_features.sql
```

## Tables

## Demo Data

`V2__seed_demo_banking_data.sql` creates a complete scenario dataset:

- Domestic CNY accounts: `AC_DEMO_CNY_001`, `AC_DEMO_CNY_002`
- Overseas USD accounts: `AC_DEMO_USD_001`, `AC_DEMO_USD_002`
- High-balance risk account: `AC_DEMO_CNY_HIGH`
- Frozen account: `AC_DEMO_CNY_FROZEN`
- Successful domestic transfer: `TR_DEMO_SUCCESS`
- Risk-rejected domestic transfer: `TR_DEMO_RISK_REJECTED`
- Successful international remittance: `RM_DEMO_SUCCESS`
- Risk-rejected remittance: `RM_DEMO_RISK_REJECTED`

These rows include matching ledger entries, audit logs, idempotency records and outbox events where applicable.

`V3__advanced_banking_features.sql` adds:

- `customer_profile`
- `kyc_profile`
- `fee_rule`
- `exchange_rate`
- `reversal_order`

### account_balance

Stores the current balance snapshot for each account.

Important columns:

- `account_no`: unique business account number.
- `customer_id`: customer identifier.
- `user_region`: `DOMESTIC` or `OVERSEAS`.
- `account_type`: `SAVINGS`, `CHECKING` or `FOREIGN_CURRENCY`.
- `currency`: ISO 4217 currency code.
- `available_balance`: spendable balance.
- `frozen_balance`: reserved balance for future hold scenarios.
- `version`: optimistic locking field.

### ledger_entry

Append-only money movement table.

Important columns:

- `entry_no`: unique ledger entry number.
- `transaction_no`: transfer, remittance or deposit business number.
- `account_no`: account affected by the movement.
- `direction`: `DEBIT` or `CREDIT`.
- `amount`: movement amount.
- `balance_after`: account balance after this entry.
- `entry_type`: deposit, domestic transfer debit/credit or remittance debit/credit.

### transfer_order

Domestic transfer business order.

The order records request, status, risk result and failure reason. It should not be used as the only source of money movement history; use `ledger_entry` for that.

### remittance_order

International remittance business order.

Stores source amount, target amount, exchange rate, fee, destination country and optional SWIFT/IBAN fields.

### idempotency_record

Prevents repeated processing of external requests.

Unique key:

```text
request_id + business_type
```

### audit_log

Stores business audit events such as account creation, deposit, transfer success and risk rejection.

### outbox_event

Stores domain events for future reliable message publishing. Current events include:

- `AccountOpenedEvent`
- `DepositCompletedEvent`
- `TransferSucceededEvent`
- `RemittanceCompletedEvent`

## Index Notes

Indexes are added for common query paths:

- Ledger by transaction number.
- Ledger by account and creation time.
- Transfer by request id, source account and target account.
- Remittance by request id and sender account.
- Audit by business number.
- Outbox by status and creation time.
