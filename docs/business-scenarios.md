# Business Scenarios

## Domestic Account Deposit

Goal:

- Add funds to an active account.

Controls:

- Amount must be positive.
- Currency must match the account.
- `requestId` must be unique for deposit.
- Ledger credit entry is created.
- Audit log is created.

Demo data:

- Account: `AC_DEMO_CNY_001`
- Transaction: `DEMO_DEP_001`
- Ledger: `LE_DEMO_DEP_001`

## Domestic Transfer

Goal:

- Move funds from one domestic account to another account in the same currency.

Controls:

- Source and target accounts cannot be the same.
- Both accounts must be active.
- Both account currencies must match the transfer currency.
- High amount transfer is rejected by risk policy.
- Source account must have enough available balance.
- Debit and credit ledger entries are created.
- Transfer order moves through explicit statuses.

Demo data:

- Successful order: `TR_DEMO_SUCCESS`
- Source account: `AC_DEMO_CNY_001`
- Target account: `AC_DEMO_CNY_002`

## International Remittance

Goal:

- Simulate overseas user transfer or cross-border remittance with exchange rate and fee.

Controls:

- Source and target currencies can differ.
- Sender pays source amount plus fee.
- Receiver gets converted target amount.
- Destination country can be blocked by risk policy.
- SWIFT and IBAN fields are reserved for foreign bank routing data.

Demo data:

- Successful order: `RM_DEMO_SUCCESS`
- Sender account: `AC_DEMO_CNY_001`
- Receiver account: `AC_DEMO_USD_001`
- Risk-rejected order: `RM_DEMO_RISK_REJECTED`

## Account Freeze and Unfreeze

Goal:

- Change account status for operational or risk-control reasons.

Controls:

- Frozen accounts cannot deposit, debit or receive credit because all money operations call `ensureActive`.
- Status changes write audit logs.
- Status changes write outbox events.

Demo data:

- Frozen account: `AC_DEMO_CNY_FROZEN`

API:

- `PATCH /api/v1/accounts/{accountNo}/status`

## Ledger, Audit and Outbox Traceability

Goal:

- Verify all business operations after execution.

Controls:

- Ledger records money movement.
- Audit records operation outcome.
- Outbox records future integration events.

API:

- `GET /api/v1/ledger/transactions/{transactionNo}`
- `GET /api/v1/ledger/accounts/{accountNo}`
- `GET /api/v1/audit/{businessNo}`
- `GET /api/v1/outbox/{aggregateId}`

## Idempotency

A duplicate request with the same `requestId` and business type is rejected before money movement. This avoids repeated deposit, duplicate debit and repeated remittance when clients retry after network failures.

## Future Compensation

The current schema already has states such as `COMPENSATING`, `REVERSED` and `UNKNOWN`. A later job can scan orders stuck after `DEBIT_SUCCESS` and perform reversal or manual handling.
