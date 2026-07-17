# Lock Design

The project uses layered locking for money movement safety. Locks are not used
as the only consistency mechanism; they work together with database transactions,
idempotency records, ledger entries and order state machines.

## Current Lock Layers

### Local Account Lock

Code:

- `lock/AccountLockExecutor.java`
- `lock/LocalAccountLockExecutor.java`

Purpose:

- Serializes money operations that touch the same account inside one JVM.
- Sorts account numbers before acquiring locks to reduce deadlock risk.
- Uses `tryLock` with timeout instead of waiting forever.

Configuration:

```yaml
bank:
  lock:
    wait-timeout: 3s
    slow-wait-threshold: 200ms
    slow-hold-threshold: 500ms
```

Failure behavior:

- If the lock cannot be acquired before `wait-timeout`, the service throws
  `ACCOUNT_LOCK_TIMEOUT`.
- If a waiting thread is interrupted, the service throws `ACCOUNT_LOCK_INTERRUPTED`
  and restores the interrupt flag.

Metrics:

- `bank.account.lock.wait`: time spent waiting for account locks.
- `bank.account.lock.hold`: time spent holding account locks.
- `bank.account.lock.timeout`: count of lock wait timeouts.
- `bank.account.lock.interrupted`: count of interrupted lock waits.

### Database Account Row Lock

Code:

- `account/AccountRepository.findByAccountNoForUpdate`

Purpose:

- Uses `PESSIMISTIC_WRITE` so the account row is locked in the database during
  balance mutation.
- Protects consistency when multiple backend instances use the same database.

### Optimistic Version

Code:

- `account/AccountEntity.version`

Purpose:

- Adds an extra lost-update guard for account rows.
- Useful for non-hot-path account updates and future refactoring.

### Remittance Quote Lock

Code:

- `pricing/RemittanceQuoteRepository.findByQuoteId`

Purpose:

- Uses `PESSIMISTIC_WRITE` to protect one-time quote consumption.
- Prevents a `quoteId` from being used by two concurrent remittance submissions.

## Business Scenarios

- Domestic transfer locks source and target accounts.
- Transfer reversal locks the original sender and receiver accounts.
- International remittance locks sender and receiver accounts, then consumes a
  locked quote.
- Deposit, hold and release use database row locks for the affected account.

## Production Evolution

For multi-node high availability, the local lock abstraction can be replaced by:

- Redis or Redisson locks.
- Database advisory locks.
- Account-sharded command queues.
- Actor-style per-account command routing.

The `AccountLockExecutor` interface exists so this replacement does not change
transfer or remittance application services.

Recommended future hardening:

- Add finite retry for database deadlock errors.
- Add lock metrics dashboards and alerts.
- Add ArchUnit rules to ensure payment flows use `AccountLockExecutor`.
- Add load tests for hot-account transfer contention.
