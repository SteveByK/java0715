import { useState } from "react";
import { bankApi } from "../api/client";
import type { LedgerEntry } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";
import { StatusBadge } from "../components/StatusBadge";

// Ledger scenario page: inspect append-only account and transaction ledger entries.
export function LedgerPage() {
  const [lookup, setLookup] = useState("TR_DEMO_SUCCESS");
  const [entries, setEntries] = useState<LedgerEntry[] | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function query(mode: "transaction" | "account") {
    setError(null);
    setResult(null);
    try {
      const data = mode === "transaction"
        ? await bankApi.ledgerByTransaction(lookup)
        : await bankApi.ledgerByAccount(lookup);
      setEntries(data);
      setResult(`查询到 ${data.length} 条账本记录`);
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="page-stack">
      <section className="panel">
        <div className="panel-heading"><h2>账本查询</h2></div>
        <div className="inline-form">
          <input value={lookup} onChange={(event) => setLookup(event.target.value)} />
          <button onClick={() => query("transaction")}>按交易号</button>
          <button onClick={() => query("account")}>按账户号</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <section className="panel">
        <div className="panel-heading"><h2>账本列表</h2></div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>流水号</th>
                <th>交易号</th>
                <th>账户</th>
                <th>方向</th>
                <th>金额</th>
                <th>余额</th>
                <th>类型</th>
              </tr>
            </thead>
            <tbody>
              {(entries || []).map((entry) => (
                <tr key={entry.entryNo}>
                  <td>{entry.entryNo}</td>
                  <td>{entry.transactionNo}</td>
                  <td>{entry.accountNo}</td>
                  <td><StatusBadge value={entry.direction} /></td>
                  <td>{entry.amount} {entry.currency}</td>
                  <td>{entry.balanceAfter}</td>
                  <td>{entry.entryType}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
      <DataPanel title="账本原始数据" data={entries} error={error} />
    </div>
  );
}
