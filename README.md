# java0715

Java 21 banking domain project for domestic deposits, domestic transfers and international remittance.

The project is intentionally built as a modular monolith. It keeps deployment simple now, while using domain boundaries that can later evolve into `account-service`, `transfer-service`, `remittance-service`, `risk-service`, `ledger-service` and `audit-service`.

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring Web, Validation, Security, Actuator
- Spring Data JPA
- Flyway database migration
- MySQL 8 for production-like runtime
- H2 in MySQL mode for local tests
- JUnit 5 integration tests
- OpenAPI / Swagger UI
- Maven
- Docker Compose
- Flyway demo data generation
- React + Bun + Vite frontend console

## Architecture

Main design choices:

- DDD-style bounded modules: account, transfer, remittance, risk, ledger, audit, idempotency and outbox.
- Hexagonal-style separation: controllers call application services; domain rules stay out of controllers.
- Ledger-first thinking: balance is a snapshot, every money movement also writes immutable ledger entries.
- Idempotency by `requestId + businessType`; retry returns the original business result where possible.
- Account locking by deterministic account ordering plus database pessimistic write locks.
- State machine style transaction statuses.
- Database-backed remittance quote locking with fee rule and exchange-rate traceability.
- Outbox table with pending publish processing for future Kafka/RocketMQ integration.
- Audit log for every core operation.

## Core Scenarios

- Open domestic or overseas accounts.
- Deposit into a domestic CNY account or foreign currency account.
- Transfer between domestic accounts.
- Remit from a domestic or overseas account to another account with locked exchange rate and fee.
- Reject high-risk domestic transfers before debit.
- Reject remittance to blocked destination countries.
- Freeze and unfreeze accounts.
- Prevent repeated money movement through idempotency records and original-result return.
- Write ledger entries for debit and credit operations.
- Query ledger, audit and outbox records for traceability.

Detailed scenario mapping:

```text
docs/scenario-code-map.md
```

Frontend guide:

```text
docs/frontend.md
```

## Generated Demo Data

Flyway migration `V2__seed_demo_banking_data.sql` creates demo accounts, orders, ledger entries, audit logs and outbox events.
`V3__advanced_banking_features.sql` adds customer/KYC, pricing and reversal tables.
`V4__production_hardening.sql` adds remittance quote locking plus Outbox and idempotency operational fields.

Useful demo identifiers:

```text
AC_DEMO_CNY_001       domestic source account
AC_DEMO_CNY_002       domestic receiver account
AC_DEMO_USD_001       overseas USD receiver account
AC_DEMO_CNY_HIGH      high-balance account for risk rejection
AC_DEMO_CNY_FROZEN    frozen account scenario
TR_DEMO_SUCCESS       successful domestic transfer
TR_DEMO_RISK_REJECTED high-amount transfer rejected by risk
RM_DEMO_SUCCESS       successful international remittance
RM_DEMO_RISK_REJECTED blocked-country remittance rejected by risk
```

## Run Locally

Start with the in-memory H2 database:

```bash
mvn spring-boot:run
```

Start with MySQL:

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Start the complete Docker stack:

```bash
docker compose up --build
```

Services:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8081
MySQL:    localhost:3306
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

React frontend:

```bash
cd frontend
bun install
bun run dev
```

Open:

```text
http://localhost:5173
```

All business APIs require:

```text
X-API-Key: dev-api-key
```

## Example API Calls

Create account:

```bash
curl -X POST http://localhost:8080/api/v1/accounts ^
  -H "Content-Type: application/json" ^
  -H "X-API-Key: dev-api-key" ^
  -d "{\"customerId\":\"C1001\",\"ownerName\":\"Zhang San\",\"userRegion\":\"DOMESTIC\",\"accountType\":\"SAVINGS\",\"currency\":\"CNY\"}"
```

Deposit:

```bash
curl -X POST http://localhost:8080/api/v1/accounts/{accountNo}/deposits ^
  -H "Content-Type: application/json" ^
  -H "X-API-Key: dev-api-key" ^
  -d "{\"requestId\":\"dep-1001\",\"amount\":10000.00,\"currency\":\"CNY\",\"remark\":\"initial deposit\"}"
```

Domestic transfer:

```bash
curl -X POST http://localhost:8080/api/v1/transfers/domestic ^
  -H "Content-Type: application/json" ^
  -H "X-API-Key: dev-api-key" ^
  -d "{\"requestId\":\"tr-1001\",\"fromAccountNo\":\"AC_FROM\",\"toAccountNo\":\"AC_TO\",\"amount\":1200.00,\"currency\":\"CNY\",\"remark\":\"rent\"}"
```

International remittance:

First quote the remittance:

```bash
curl "http://localhost:8080/api/v1/pricing/remittance-quote?sourceCurrency=CNY&targetCurrency=USD&sourceAmount=700" ^
  -H "X-API-Key: dev-api-key"
```

Then submit with the returned `quoteId`:

```bash
curl -X POST http://localhost:8080/api/v1/remittances ^
  -H "Content-Type: application/json" ^
  -H "X-API-Key: dev-api-key" ^
  -d "{\"requestId\":\"rm-1001\",\"senderAccountNo\":\"AC_CNY\",\"receiverAccountNo\":\"AC_USD\",\"sourceAmount\":700.00,\"sourceCurrency\":\"CNY\",\"targetCurrency\":\"USD\",\"quoteId\":\"QT_RETURNED_BY_QUOTE_API\",\"destinationCountry\":\"US\",\"swiftCode\":\"BOFAUS3N\",\"remark\":\"family support\"}"
```

Query seed transfer ledger:

```bash
curl http://localhost:8080/api/v1/ledger/transactions/TR_DEMO_SUCCESS ^
  -H "X-API-Key: dev-api-key"
```

Freeze or reactivate an account:

```bash
curl -X PATCH http://localhost:8080/api/v1/accounts/AC_DEMO_CNY_FROZEN/status ^
  -H "Content-Type: application/json" ^
  -H "X-API-Key: dev-api-key" ^
  -d "{\"status\":\"ACTIVE\",\"reason\":\"manual review passed\"}"
```

## Validation

```bash
mvn test
mvn checkstyle:check
cd frontend
bun run build
```

## Future Microservice Path

This project already avoids cross-module table coupling and defines stable API DTOs. A practical evolution path is:

1. Extract account and ledger into an account service.
2. Move domestic transfer orchestration into a transfer service.
3. Move remittance and exchange-rate logic into a remittance service.
4. Replace local outbox polling with Kafka or RocketMQ.
5. Replace local account locks with Redis/Redisson or database-native advisory locks.
6. Add OpenTelemetry tracing, Prometheus metrics and centralized log search.
