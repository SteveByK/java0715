import { useState } from "react";
import { bankApi } from "../api/client";
import type { AuditLog } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

export function AuditPage() {
  const [businessNo, setBusinessNo] = useState("TR_DEMO_SUCCESS");
  const [logs, setLogs] = useState<AuditLog[] | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function query() {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.auditByBusinessNo(businessNo);
      setLogs(data);
      setResult(`查询到 ${data.length} 条审计记录`);
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>审计查询</h2></div>
        <div className="form-grid">
          <label>
            业务号
            <input value={businessNo} onChange={(event) => setBusinessNo(event.target.value)} />
          </label>
          <button onClick={query}>查询审计</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="审计记录" data={logs} error={error} />
    </div>
  );
}
