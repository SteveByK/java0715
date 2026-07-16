import { useState } from "react";
import { bankApi } from "../api/client";
import type { TransferOrder } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

export function TransferPage() {
  const [orderNo, setOrderNo] = useState("TR_DEMO_SUCCESS");
  const [transfer, setTransfer] = useState<TransferOrder | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function submit(highRisk = false) {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.domesticTransfer({
        requestId: `ui-tr-${Date.now()}`,
        fromAccountNo: highRisk ? "AC_DEMO_CNY_HIGH" : "AC_DEMO_CNY_001",
        toAccountNo: "AC_DEMO_CNY_002",
        amount: highRisk ? 250000 : 88,
        currency: "CNY",
        remark: highRisk ? "frontend high amount risk case" : "frontend domestic transfer"
      });
      setTransfer(data);
      setOrderNo(data.orderNo);
      setResult(highRisk ? "已触发风控拒绝" : "转账已提交");
    } catch (caught) {
      setError(caught);
    }
  }

  async function query() {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.getTransfer(orderNo);
      setTransfer(data);
      setResult("订单查询完成");
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>国内转账</h2></div>
        <div className="form-grid">
          <label>
            订单号
            <input value={orderNo} onChange={(event) => setOrderNo(event.target.value)} />
          </label>
          <button onClick={query}>查询订单</button>
          <button onClick={() => submit(false)}>发起 88 CNY 转账</button>
          <button className="danger" onClick={() => submit(true)}>发起高额风控案例</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="转账响应" data={transfer} error={error} />
    </div>
  );
}
