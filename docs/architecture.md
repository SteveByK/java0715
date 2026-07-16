# Architecture Notes

## Design Principles

- Money correctness is the first priority.
- Balances are snapshots; ledger entries are the source of movement history.
- External requests must be idempotent.
- Failure states must be explicit and auditable.
- Domain modules should be separable without rewriting core business rules.

## Bounded Contexts

- `account`: account lifecycle, status, balance snapshots and deposits.
- `transfer`: domestic transfer order orchestration.
- `remittance`: cross-currency international remittance orchestration.
- `risk`: risk decisions for high amount and destination-country controls.
- `ledger`: immutable money movement entries.
- `audit`: business audit trail.
- `idempotency`: duplicate request protection.
- `outbox`: reliable event publishing preparation.

## Transaction Model

Domestic transfer:

1. Validate request.
2. Create idempotency record.
3. Create transfer order.
4. Run risk check.
5. Lock source and target accounts in sorted order.
6. Lock account rows with `PESSIMISTIC_WRITE`.
7. Debit source account and write debit ledger entry.
8. Credit target account and write credit ledger entry.
9. Mark order as `SUCCESS`.
10. Write audit and outbox event.

International remittance:

1. Validate request and exchange rate.
2. Calculate fee and target amount.
3. Create idempotency record.
4. Create remittance order.
5. Run risk check for country and amount.
6. Debit source account for `sourceAmount + fee`.
7. Credit receiver account for converted target amount.
8. Mark order as `SUCCESS`.
9. Write audit and outbox event.

## State Design

Transaction status values:

- `CREATED`
- `RISK_REJECTED`
- `PROCESSING`
- `DEBIT_SUCCESS`
- `CREDIT_SUCCESS`
- `SUCCESS`
- `FAILED`
- `COMPENSATING`
- `REVERSED`
- `UNKNOWN`

These states are more detailed than a simple success/failure flag so future compensation jobs can safely resume or reverse operations.

## Locking Strategy

The current implementation uses:

- JVM account lock executor to serialize same-account operations in a single instance.
- Sorted account numbers before locking to avoid deadlocks.
- JPA pessimistic row locks to protect account rows in the database.
- Optimistic version field on account rows for additional write conflict detection.

For multi-instance deployment, replace or supplement the local lock executor with Redis/Redisson or a database advisory lock implementation.

## Standards

The code follows these practical rules from Alibaba and international Java service conventions:

- No floating point for money.
- No entity exposure in API contracts.
- Controller layer does not contain business logic.
- Business exceptions use stable error codes.
- Core business data has create/update timestamps.
- SQL objects have explicit names and indexes.
- Ledger and audit records are append-oriented.
- APIs are versioned under `/api/v1`.
