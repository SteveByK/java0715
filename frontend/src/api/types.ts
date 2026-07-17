// Generic response envelope returned by every Spring Boot API.
export type ApiResponse<T> = {
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

// Authenticated operator profile and RBAC grant set.
export type AuthUser = {
  userId: string;
  username: string;
  displayName: string;
  roles: string[];
  permissions: string[];
};

// Token response returned by login and refresh endpoints.
export type LoginResponse = AuthUser & {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresInSeconds: number;
};

// Domain enum mirrors used by the frontend for status labels and request payloads.
export type AccountStatus = "ACTIVE" | "FROZEN" | "CLOSED";
export type AccountType = "SAVINGS" | "CHECKING" | "FOREIGN_CURRENCY";
export type UserRegion = "DOMESTIC" | "OVERSEAS";
export type KycStatus = "PENDING" | "APPROVED" | "REJECTED";
export type KycLevel = "BASIC" | "STANDARD" | "ENHANCED";
export type TransactionStatus =
  | "CREATED"
  | "RISK_REJECTED"
  | "PROCESSING"
  | "DEBIT_SUCCESS"
  | "CREDIT_SUCCESS"
  | "SUCCESS"
  | "FAILED"
  | "COMPENSATING"
  | "REVERSED"
  | "UNKNOWN";

// Account balance snapshot returned by account APIs.
export type Account = {
  accountNo: string;
  customerId: string;
  ownerName: string;
  userRegion: UserRegion;
  accountType: AccountType;
  currency: string;
  availableBalance: number;
  frozenBalance: number;
  status: AccountStatus;
};

// Domestic transfer order view.
export type TransferOrder = {
  orderNo: string;
  requestId: string;
  fromAccountNo: string;
  toAccountNo: string;
  amount: number;
  fee: number;
  currency: string;
  status: TransactionStatus;
  riskCode?: string | null;
  failureReason?: string | null;
};

// International remittance order view with pricing trace fields.
export type RemittanceOrder = {
  orderNo: string;
  requestId: string;
  senderAccountNo: string;
  receiverAccountNo: string;
  sourceAmount: number;
  exchangeRate: number;
  fee: number;
  targetAmount: number;
  quoteId?: string | null;
  feeRuleCode?: string | null;
  rateCode?: string | null;
  sourceCurrency: string;
  targetCurrency: string;
  destinationCountry: string;
  status: TransactionStatus;
  riskCode?: string | null;
  failureReason?: string | null;
};

// Immutable ledger entry view for transaction and account history.
export type LedgerEntry = {
  entryNo: string;
  transactionNo: string;
  accountNo: string;
  direction: "DEBIT" | "CREDIT";
  amount: number;
  balanceAfter: number;
  currency: string;
  entryType: string;
  createdAt: string;
};

// Business audit event view.
export type AuditLog = {
  businessNo: string;
  action: string;
  result: string;
  detail?: string | null;
  createdAt: string;
};

// Transactional outbox event view for future message relay diagnostics.
export type OutboxEvent = {
  eventId: string;
  aggregateId: string;
  eventType: string;
  payload: string;
  status: string;
  retryCount: number;
  lastError?: string | null;
  publishedAt?: string | null;
  createdAt: string;
  updatedAt?: string | null;
};

// Customer profile view enriched with KYC summary.
export type Customer = {
  customerId: string;
  fullName: string;
  userRegion: UserRegion;
  countryCode: string;
  phone?: string | null;
  email?: string | null;
  riskLevel: "LOW" | "MEDIUM" | "HIGH";
  status: "ACTIVE" | "SUSPENDED" | "CLOSED";
  kycStatus?: KycStatus | null;
  kycLevel?: KycLevel | null;
};

// Locked remittance quote returned before remittance submission.
export type Quote = {
  quoteId: string;
  sourceCurrency: string;
  targetCurrency: string;
  sourceAmount: number;
  exchangeRate: number;
  fee: number;
  targetAmount: number;
  feeRuleCode: string;
  rateCode: string;
  expiresAt: string;
};

// Transfer reversal order view.
export type Reversal = {
  reversalNo: string;
  originalOrderNo: string;
  requestId: string;
  amount: number;
  currency: string;
  status: TransactionStatus;
  reason: string;
};
