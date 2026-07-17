# DDD Design Notes

This project is organized as a modular monolith with explicit bounded contexts.
The code uses lightweight architecture annotations under `common.ddd` to make
roles visible without changing Spring runtime behavior.

## Architecture Annotations

- `@InboundAdapter`: HTTP controllers and other inbound delivery adapters.
- `@ApplicationServiceRole`: use-case orchestration services.
- `@DomainServiceRole`: domain policy or calculation services.
- `@AggregateRoot`: persistence model that represents a consistency boundary.
- `@OutboundPort`: repository or integration port used by services.

These annotations are intentionally small. They are documentation in code today
and can later become ArchUnit/static-analysis rules.

## Bounded Contexts

- `account`: account aggregate, account status, balance snapshot, deposit, hold and release.
- `transfer`: domestic transfer order and reversal orchestration.
- `remittance`: international remittance order orchestration.
- `pricing`: exchange-rate rules, fee rules and locked remittance quotes.
- `customer`: customer profile and KYC lifecycle.
- `ledger`: append-only money movement records.
- `audit`: business operation evidence.
- `outbox`: reliable event publishing preparation.
- `risk`: risk policy decisions.
- `idempotency`: duplicate command protection.
- `lock`: account lock abstraction for local concurrency.

## Design Rules

- Controllers do not contain business decisions.
- Services orchestrate use cases and transaction boundaries.
- Account balance changes go through `AccountService`.
- Money movement must create ledger entries.
- External commands must carry a `requestId`.
- Remittance settlement must use a backend-created quote.
- Outbox rows are created in business transactions before external publication.
- Persistence entities are not exposed directly as API responses.

## Future Quality Gate

A next step is adding ArchUnit tests that enforce:

- `@InboundAdapter` classes can call application services but not repositories directly.
- Payment services can use account services but cannot update account repositories directly.
- Repository interfaces must be annotated with `@OutboundPort`.
- Persistence entities should stay inside their bounded context packages.
