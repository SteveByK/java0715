# Frontend

The frontend is a React + Bun + Vite banking console under `frontend/`.

## Stack

- React 18
- TypeScript
- Bun
- Vite
- React Router
- Axios
- Recharts
- lucide-react

## Run

Start the backend first:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Start the frontend:

```bash
cd frontend
bun install
bun run dev
```

Open:

```text
http://localhost:5173
```

Vite proxies `/api` and `/actuator` to `http://localhost:8081`, so the browser calls real backend APIs without a CORS change during local development.

Docker Compose sets `VITE_API_PROXY_TARGET=http://backend:8080` so the frontend container can proxy API calls to the backend container.

## Pages

- Dashboard: summary, seed account balances and transaction status overview.
- Accounts: account lookup, deposit, freeze, unfreeze and account opening.
- Domestic Transfer: query seed orders and create success or risk-rejected transfers.
- Remittance: query seed orders and create success or risk-rejected cross-border remittances.
- Ledger: query ledger entries by transaction or account.
- Audit: query business audit logs.
- Outbox: query domain events prepared for future MQ publishing.

## Demo IDs

```text
AC_DEMO_CNY_001
AC_DEMO_CNY_002
AC_DEMO_USD_001
AC_DEMO_CNY_FROZEN
TR_DEMO_SUCCESS
TR_DEMO_RISK_REJECTED
RM_DEMO_SUCCESS
RM_DEMO_RISK_REJECTED
```
