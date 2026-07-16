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

## International Remittance

Goal:

- Simulate overseas user transfer or cross-border remittance with exchange rate and fee.

Controls:

- Source and target currencies can differ.
- Sender pays source amount plus fee.
- Receiver gets converted target amount.
- Destination country can be blocked by risk policy.
- SWIFT and IBAN fields are reserved for foreign bank routing data.

## Idempotency

A duplicate request with the same `requestId` and business type is rejected before money movement. This avoids repeated deposit, duplicate debit and repeated remittance when clients retry after network failures.

## Future Compensation

The current schema already has states such as `COMPENSATING`, `REVERSED` and `UNKNOWN`. A later job can scan orders stuck after `DEBIT_SUCCESS` and perform reversal or manual handling.
