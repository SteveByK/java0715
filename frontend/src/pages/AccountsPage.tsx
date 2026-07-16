import { useState } from "react";
import { bankApi } from "../api/client";
import type { Account } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";
import { StatusBadge } from "../components/StatusBadge";

export function AccountsPage() {
  const [accountNo, setAccountNo] = useState("AC_DEMO_CNY_001");
  const [account, setAccount] = useState<Account | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function run(action: () => Promise<Account>, success: string) {
    setError(null);
    setResult(null);
    try {
      const data = await action();
      setAccount(data);
      setResult(success);
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>账户操作</h2></div>
        <div className="form-grid">
          <label>
            账户号
            <input value={accountNo} onChange={(event) => setAccountNo(event.target.value)} />
          </label>
          <button onClick={() => run(() => bankApi.getAccount(accountNo), "账户查询完成")}>查询账户</button>
          <button onClick={() => run(() => bankApi.deposit(accountNo, {
            requestId: `ui-dep-${Date.now()}`,
            amount: 100,
            currency: account?.currency || "CNY",
            remark: "frontend deposit"
          }), "存款成功")}>存款 100</button>
          <button onClick={() => run(() => bankApi.updateStatus(accountNo, "FROZEN", "frontend risk hold"), "账户已冻结")}>冻结</button>
          <button onClick={() => run(() => bankApi.updateStatus(accountNo, "ACTIVE", "frontend reactivation"), "账户已激活")}>解冻</button>
          <button onClick={() => run(() => bankApi.createAccount({
            customerId: `UI_${Date.now()}`,
            ownerName: "Frontend Demo User",
            userRegion: "DOMESTIC",
            accountType: "SAVINGS",
            currency: "CNY"
          }), "开户成功")}>新开 CNY 账户</button>
        </div>
        <ResultNotice result={result} error={error} />
        {account ? (
          <div className="summary-card">
            <span>{account.ownerName}</span>
            <strong>{account.availableBalance} {account.currency}</strong>
            <StatusBadge value={account.status} />
          </div>
        ) : null}
      </section>
      <DataPanel title="账户响应" data={account} error={error} />
    </div>
  );
}
