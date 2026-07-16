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

## Architecture

Main design choices:

- DDD-style bounded modules: account, transfer, remittance, risk, ledger, audit, idempotency and outbox.
- Hexagonal-style separation: controllers call application services; domain rules stay out of controllers.
- Ledger-first thinking: balance is a snapshot, every money movement also writes immutable ledger entries.
- Idempotency by `requestId + businessType`.
- Account locking by deterministic account ordering plus database pessimistic write locks.
- State machine style transaction statuses.
- Outbox table for future Kafka/RocketMQ integration.
- Audit log for every core operation.

## Core Scenarios

- Open domestic or overseas accounts.
- Deposit into a domestic CNY account or foreign currency account.
- Transfer between domestic accounts.
- Remit from a domestic or overseas account to another account with exchange rate and fee.
- Reject high-risk domestic transfers before debit.
- Reject remittance to blocked destination countries.
- Prevent repeated processing through idempotency records.
- Write ledger entries for debit and credit operations.

## Run Locally

Start with the in-memory H2 database:

```bash
mvn spring-boot:run
```

Start with MySQL:

```bash
docker compose up -d
set DB_URL=jdbc:mysql://localhost:3306/java0715?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USERNAME=bank
set DB_PASSWORD=bank123456
set DB_DRIVER=com.mysql.cj.jdbc.Driver
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
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

```bash
curl -X POST http://localhost:8080/api/v1/remittances ^
  -H "Content-Type: application/json" ^
  -H "X-API-Key: dev-api-key" ^
  -d "{\"requestId\":\"rm-1001\",\"senderAccountNo\":\"AC_CNY\",\"receiverAccountNo\":\"AC_USD\",\"sourceAmount\":700.00,\"sourceCurrency\":\"CNY\",\"targetCurrency\":\"USD\",\"exchangeRate\":0.14,\"destinationCountry\":\"US\",\"swiftCode\":\"BOFAUS3N\",\"remark\":\"family support\"}"
```

## Validation

```bash
mvn test
mvn checkstyle:check
```

## Future Microservice Path

This project already avoids cross-module table coupling and defines stable API DTOs. A practical evolution path is:

1. Extract account and ledger into an account service.
2. Move domestic transfer orchestration into a transfer service.
3. Move remittance and exchange-rate logic into a remittance service.
4. Replace local outbox polling with Kafka or RocketMQ.
5. Replace local account locks with Redis/Redisson or database-native advisory locks.
6. Add OpenTelemetry tracing, Prometheus metrics and centralized log search.
