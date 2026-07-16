import type { TransactionStatus } from "../api/types";

type Props = {
  value: string | TransactionStatus;
};

export function StatusBadge({ value }: Props) {
  const normalized = value.toLowerCase().replace(/_/g, "-");
  return <span className={`status-badge ${normalized}`}>{value}</span>;
}
