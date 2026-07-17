import { useEffect, useMemo, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { bankApi, demoIds, getErrorMessage } from "../api/client";
import type { Account, RemittanceOrder, TransferOrder } from "../api/types";
import { StatusBadge } from "../components/StatusBadge";

// Dashboard page: summarizes the available banking business scenarios.
export function DashboardPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transfers, setTransfers] = useState<TransferOrder[]>([]);
  const [remittances, setRemittances] = useState<RemittanceOrder[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      Promise.all(demoIds.accounts.map((id) => bankApi.getAccount(id))),
      Promise.all(demoIds.transfers.map((id) => bankApi.getTransfer(id))),
      Promise.all(demoIds.remittances.map((id) => bankApi.getRemittance(id)))
    ])
      .then(([accountRows, transferRows, remittanceRows]) => {
        setAccounts(accountRows);
        setTransfers(transferRows);
        setRemittances(remittanceRows);
      })
      .catch((caught) => setError(getErrorMessage(caught)));
  }, []);

  const chartData = useMemo(
    () =>
      accounts.map((account) => ({
        account: account.accountNo.replace("AC_DEMO_", ""),
        balance: Number(account.availableBalance),
        currency: account.currency
      })),
    [accounts]
  );

  const riskRejected = [...transfers, ...remittances].filter((item) => item.status === "RISK_REJECTED").length;

  return (
    <div className="page-stack">
      {error ? <div className="notice error">{error}</div> : null}
      <section className="metric-grid">
        <div className="metric-card">
          <span>演示账户</span>
          <strong>{accounts.length}</strong>
        </div>
        <div className="metric-card">
          <span>国内转账单</span>
          <strong>{transfers.length}</strong>
        </div>
        <div className="metric-card">
          <span>国际汇款单</span>
          <strong>{remittances.length}</strong>
        </div>
        <div className="metric-card warning">
          <span>风控拒绝</span>
          <strong>{riskRejected}</strong>
        </div>
      </section>

      <section className="panel">
        <div className="panel-heading">
          <h2>账户余额快照</h2>
        </div>
        <div className="chart-box">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="account" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="balance" fill="#2563eb" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </section>

      <section className="panel">
        <div className="panel-heading">
          <h2>核心业务状态</h2>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>业务号</th>
                <th>类型</th>
                <th>金额</th>
                <th>状态</th>
                <th>风控</th>
              </tr>
            </thead>
            <tbody>
              {transfers.map((transfer) => (
                <tr key={transfer.orderNo}>
                  <td>{transfer.orderNo}</td>
                  <td>国内转账</td>
                  <td>{transfer.amount} {transfer.currency}</td>
                  <td><StatusBadge value={transfer.status} /></td>
                  <td>{transfer.riskCode || "-"}</td>
                </tr>
              ))}
              {remittances.map((remittance) => (
                <tr key={remittance.orderNo}>
                  <td>{remittance.orderNo}</td>
                  <td>国际汇款</td>
                  <td>{remittance.sourceAmount} {remittance.sourceCurrency}</td>
                  <td><StatusBadge value={remittance.status} /></td>
                  <td>{remittance.riskCode || "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
