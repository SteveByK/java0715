# API Documentation

Base URL:

```text
http://localhost:8080/api/v1
```

Header:

```text
X-API-Key: dev-api-key
```

## Account

### Create Account

`POST /accounts`

```json
{
  "customerId": "C1001",
  "ownerName": "Zhang San",
  "userRegion": "DOMESTIC",
  "accountType": "SAVINGS",
  "currency": "CNY"
}
```

### Get Account

`GET /accounts/{accountNo}`

### Deposit

`POST /accounts/{accountNo}/deposits`

```json
{
  "requestId": "dep-1001",
  "amount": 10000.00,
  "currency": "CNY",
  "remark": "initial deposit"
}
```

### Update Account Status

`PATCH /accounts/{accountNo}/status`

```json
{
  "status": "FROZEN",
  "reason": "risk control hold"
}
```

### Hold Funds

`POST /accounts/{accountNo}/holds`

```json
{
  "requestId": "hold-1001",
  "amount": 50.00,
  "currency": "CNY",
  "reason": "card authorization"
}
```

### Release Held Funds

`POST /accounts/{accountNo}/holds/release`

## Transfer

### Domestic Transfer

`POST /transfers/domestic`

```json
{
  "requestId": "tr-1001",
  "fromAccountNo": "AC_SOURCE",
  "toAccountNo": "AC_TARGET",
  "amount": 1200.00,
  "currency": "CNY",
  "remark": "rent"
}
```

Risk rejection example:

```json
{
  "status": "RISK_REJECTED",
  "riskCode": "DOMESTIC_HIGH_AMOUNT",
  "failureReason": "domestic transfer amount requires manual review"
}
```

### Get Transfer Order

`GET /transfers/{orderNo}`

### Reverse Transfer

`POST /transfers/{orderNo}/reversals`

```json
{
  "requestId": "rv-1001",
  "reason": "customer dispute"
}
```

## Remittance

### International Remittance

`POST /remittances`

Recommended production-like flow:

1. Call `GET /pricing/remittance-quote` and receive a `quoteId`.
2. Submit the remittance with that `quoteId`.
3. The backend consumes the quote once and records `quoteId`, `feeRuleCode` and `rateCode` on the order.

```json
{
  "requestId": "rm-1001",
  "senderAccountNo": "AC_CNY",
  "receiverAccountNo": "AC_USD",
  "sourceAmount": 700.00,
  "sourceCurrency": "CNY",
  "targetCurrency": "USD",
  "quoteId": "QT_RETURNED_BY_QUOTE_API",
  "destinationCountry": "US",
  "swiftCode": "BOFAUS3N",
  "iban": null,
  "remark": "family support"
}
```

The service debits `sourceAmount + fee` from the sender and credits the locked `targetAmount` to the receiver.
`exchangeRate` is kept optional only for older demo-style requests; new business flow should use `quoteId`.

Duplicate requests with the same `requestId` return the existing remittance order instead of performing another debit.

### Get Remittance Order

`GET /remittances/{orderNo}`

## Customer and KYC

### Create Customer

`POST /customers`

### Get Customer

`GET /customers/{customerId}`

### Submit KYC

`PUT /customers/{customerId}/kyc`

### Review KYC

`POST /customers/{customerId}/kyc/review`

## Pricing

### Remittance Quote

`GET /pricing/remittance-quote?sourceCurrency=CNY&targetCurrency=USD&sourceAmount=700`

Response data includes:

```json
{
  "quoteId": "QT...",
  "sourceCurrency": "CNY",
  "targetCurrency": "USD",
  "sourceAmount": 700.00,
  "exchangeRate": 0.14000000,
  "fee": 2.10,
  "targetAmount": 98.00,
  "feeRuleCode": "FEE_RM_CNY_USD",
  "rateCode": "FX_CNY_USD_DEMO",
  "expiresAt": "2026-07-17T00:00:00Z"
}
```

## Ledger

### Query Ledger by Transaction

`GET /ledger/transactions/{transactionNo}`

Demo:

```text
GET /ledger/transactions/TR_DEMO_SUCCESS
```

### Query Ledger by Account

`GET /ledger/accounts/{accountNo}`

Demo:

```text
GET /ledger/accounts/AC_DEMO_CNY_001
```

## Audit

### Query Audit by Business Number

`GET /audit/{businessNo}`

Demo:

```text
GET /audit/TR_DEMO_SUCCESS
```

## Outbox

### Query Outbox Events by Aggregate

`GET /outbox/{aggregateId}`

Demo:

```text
GET /outbox/RM_DEMO_SUCCESS
```

### Publish Pending Outbox Events

`POST /outbox/publish-pending`

This endpoint simulates a reliable message relay by moving `NEW` or `FAILED` rows to `PUBLISHED`.
