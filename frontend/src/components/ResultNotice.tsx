import { getErrorMessage } from "../api/client";

type Props = {
  result?: string | null;
  error?: unknown;
};

export function ResultNotice({ result, error }: Props) {
  if (error) {
    return <div className="notice error">{getErrorMessage(error)}</div>;
  }
  if (result) {
    return <div className="notice success">{result}</div>;
  }
  return null;
}
