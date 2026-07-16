import { getErrorMessage } from "../api/client";

type Props<T> = {
  title: string;
  data: T | null;
  error?: unknown;
};

export function DataPanel<T>({ title, data, error }: Props<T>) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>{title}</h2>
      </div>
      {error ? <div className="notice error">{getErrorMessage(error)}</div> : null}
      <pre className="json-view">{data ? JSON.stringify(data, null, 2) : "No data loaded"}</pre>
    </section>
  );
}
