import type { TransactionStatus } from "../api/types";

type Props = {
  value: string | TransactionStatus;
};

// Compact visual status marker for account and transaction states.
export function StatusBadge({ value }: Props) {
  const normalized = value.toLowerCase().replace(/_/g, "-");
  return <span className={`status-badge ${normalized}`}>{value}</span>;
}
