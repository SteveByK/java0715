import axios from "axios";
import type {
  Account,
  AccountStatus,
  ApiResponse,
  AuthUser,
  AuditLog,
  Customer,
  LedgerEntry,
  LoginResponse,
  OutboxEvent,
  Quote,
  RemittanceOrder,
  Reversal,
  TransferOrder
} from "./types";

const ACCESS_TOKEN_KEY = "java0715.accessToken";
const REFRESH_TOKEN_KEY = "java0715.refreshToken";
const AUTH_USER_KEY = "java0715.authUser";

// Centralized backend API adapter. Pages call this object instead of using axios directly.
const api = axios.create({
  baseURL: "/api/v1"
});

// Adds the current bearer token to every protected API call.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Backend responses are wrapped in ApiResponse; this helper returns only the business payload.
async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise;
  return response.data.data;
}

function persistSession(response: LoginResponse): LoginResponse {
  const user: AuthUser = {
    userId: response.userId,
    username: response.username,
    displayName: response.displayName,
    roles: response.roles,
    permissions: response.permissions
  };
  localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
  localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
  return response;
}

export function getStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(AUTH_USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    clearSession();
    return null;
  }
}

export function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USER_KEY);
}

// Typed facade for all banking demo APIs used by the React console.
export const bankApi = {
  login: (username: string, password: string) =>
    unwrap<LoginResponse>(api.post("/auth/login", { username, password })).then(persistSession),
  refresh: () => {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      return Promise.reject(new Error("Missing refresh token"));
    }
    return unwrap<LoginResponse>(api.post("/auth/refresh", { refreshToken })).then(persistSession);
  },
  logout: () => {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    clearSession();
    if (!refreshToken) {
      return Promise.resolve();
    }
    return unwrap<void>(api.post("/auth/logout", { refreshToken })).then(() => undefined);
  },
  me: () => unwrap<AuthUser>(api.get("/auth/me")),
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
  outboxByAggregateId: (aggregateId: string) => unwrap<OutboxEvent[]>(api.get(`/outbox/${aggregateId}`)),
  publishPendingOutbox: () => unwrap<OutboxEvent[]>(api.post("/outbox/publish-pending"))
};

// Stable demo ids seeded by Flyway and reused by the UI.
export const demoIds = {
  accounts: ["AC_DEMO_CNY_001", "AC_DEMO_CNY_002", "AC_DEMO_USD_001", "AC_DEMO_CNY_FROZEN"],
  transfers: ["TR_DEMO_SUCCESS", "TR_DEMO_RISK_REJECTED"],
  remittances: ["RM_DEMO_SUCCESS", "RM_DEMO_RISK_REJECTED"]
};

// Normalizes axios and unknown errors into text that can be rendered by ResultNotice.
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; code?: string } | undefined;
    return data?.message || data?.code || error.message;
  }
  return error instanceof Error ? error.message : "Unknown error";
}
