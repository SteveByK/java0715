export type ApiResponse<T> = {
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

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

export type RemittanceOrder = {
  orderNo: string;
  requestId: string;
  senderAccountNo: string;
  receiverAccountNo: string;
  sourceAmount: number;
  exchangeRate: number;
  fee: number;
  targetAmount: number;
  sourceCurrency: string;
  targetCurrency: string;
  destinationCountry: string;
  status: TransactionStatus;
  riskCode?: string | null;
  failureReason?: string | null;
};

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

export type AuditLog = {
  businessNo: string;
  action: string;
  result: string;
  detail?: string | null;
  createdAt: string;
};

export type OutboxEvent = {
  eventId: string;
  aggregateId: string;
  eventType: string;
  payload: string;
  status: string;
  createdAt: string;
};

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

export type Quote = {
  sourceCurrency: string;
  targetCurrency: string;
  sourceAmount: number;
  exchangeRate: number;
  fee: number;
  targetAmount: number;
  feeRuleCode: string;
  rateCode: string;
};

export type Reversal = {
  reversalNo: string;
  originalOrderNo: string;
  requestId: string;
  amount: number;
  currency: string;
  status: TransactionStatus;
  reason: string;
};
