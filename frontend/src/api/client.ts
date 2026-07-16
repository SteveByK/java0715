import axios from "axios";
import type {
  Account,
  AccountStatus,
  ApiResponse,
  AuditLog,
  Customer,
  LedgerEntry,
  OutboxEvent,
  Quote,
  RemittanceOrder,
  Reversal,
  TransferOrder
} from "./types";

const api = axios.create({
  baseURL: "/api/v1",
  headers: {
    "X-API-Key": "dev-api-key"
  }
});

async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise;
  return response.data.data;
}

export const bankApi = {
  getAccount: (accountNo: string) => unwrap<Account>(api.get(`/accounts/${accountNo}`)),
  createAccount: (payload: unknown) => unwrap<Account>(api.post("/accounts", payload)),
  deposit: (accountNo: string, payload: unknown) => unwrap<Account>(api.post(`/accounts/${accountNo}/deposits`, payload)),
  holdFunds: (accountNo: string, payload: unknown) => unwrap<Account>(api.post(`/accounts/${accountNo}/holds`, payload)),
  releaseFunds: (accountNo: string, payload: unknown) =>
    unwrap<Account>(api.post(`/accounts/${accountNo}/holds/release`, payload)),
  updateStatus: (accountNo: string, status: AccountStatus, reason: string) =>
    unwrap<Account>(api.patch(`/accounts/${accountNo}/status`, { status, reason })),
  createCustomer: (payload: unknown) => unwrap<Customer>(api.post("/customers", payload)),
  getCustomer: (customerId: string) => unwrap<Customer>(api.get(`/customers/${customerId}`)),
  submitKyc: (customerId: string, payload: unknown) => unwrap<Customer>(api.put(`/customers/${customerId}/kyc`, payload)),
  reviewKyc: (customerId: string, payload: unknown) =>
    unwrap<Customer>(api.post(`/customers/${customerId}/kyc/review`, payload)),
  quoteRemittance: (sourceCurrency: string, targetCurrency: string, sourceAmount: number) =>
    unwrap<Quote>(api.get("/pricing/remittance-quote", { params: { sourceCurrency, targetCurrency, sourceAmount } })),
  domesticTransfer: (payload: unknown) => unwrap<TransferOrder>(api.post("/transfers/domestic", payload)),
  getTransfer: (orderNo: string) => unwrap<TransferOrder>(api.get(`/transfers/${orderNo}`)),
  reverseTransfer: (orderNo: string, payload: unknown) =>
    unwrap<Reversal>(api.post(`/transfers/${orderNo}/reversals`, payload)),
  remit: (payload: unknown) => unwrap<RemittanceOrder>(api.post("/remittances", payload)),
  getRemittance: (orderNo: string) => unwrap<RemittanceOrder>(api.get(`/remittances/${orderNo}`)),
  ledgerByTransaction: (transactionNo: string) => unwrap<LedgerEntry[]>(api.get(`/ledger/transactions/${transactionNo}`)),
  ledgerByAccount: (accountNo: string) => unwrap<LedgerEntry[]>(api.get(`/ledger/accounts/${accountNo}`)),
  auditByBusinessNo: (businessNo: string) => unwrap<AuditLog[]>(api.get(`/audit/${businessNo}`)),
  outboxByAggregateId: (aggregateId: string) => unwrap<OutboxEvent[]>(api.get(`/outbox/${aggregateId}`))
};

export const demoIds = {
  accounts: ["AC_DEMO_CNY_001", "AC_DEMO_CNY_002", "AC_DEMO_USD_001", "AC_DEMO_CNY_FROZEN"],
  transfers: ["TR_DEMO_SUCCESS", "TR_DEMO_RISK_REJECTED"],
  remittances: ["RM_DEMO_SUCCESS", "RM_DEMO_RISK_REJECTED"]
};

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; code?: string } | undefined;
    return data?.message || data?.code || error.message;
  }
  return error instanceof Error ? error.message : "Unknown error";
}
