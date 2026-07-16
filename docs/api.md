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

## Remittance

### International Remittance

`POST /remittances`

```json
{
  "requestId": "rm-1001",
  "senderAccountNo": "AC_CNY",
  "receiverAccountNo": "AC_USD",
  "sourceAmount": 700.00,
  "sourceCurrency": "CNY",
  "targetCurrency": "USD",
  "exchangeRate": 0.14000000,
  "destinationCountry": "US",
  "swiftCode": "BOFAUS3N",
  "iban": null,
  "remark": "family support"
}
```

The service debits `sourceAmount + fee` from the sender and credits `sourceAmount * exchangeRate` to the receiver.
