import { getErrorMessage } from "../api/client";

type Props = {
  result?: string | null;
  error?: unknown;
};

// Shared feedback component for successful actions and API errors.
export function ResultNotice({ result, error }: Props) {
  if (error) {
    return <div className="notice error">{getErrorMessage(error)}</div>;
  }
  if (result) {
    return <div className="notice success">{result}</div>;
  }
  return null;
}
