import { useState } from "react";
import { bankApi } from "../api/client";
import type { RemittanceOrder } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

export function RemittancePage() {
  const [orderNo, setOrderNo] = useState("RM_DEMO_SUCCESS");
  const [remittance, setRemittance] = useState<RemittanceOrder | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function submit(blockedCountry = false) {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.remit({
        requestId: `ui-rm-${Date.now()}`,
        senderAccountNo: blockedCountry ? "AC_DEMO_USD_002" : "AC_DEMO_CNY_001",
        receiverAccountNo: blockedCountry ? "AC_DEMO_CNY_002" : "AC_DEMO_USD_001",
        sourceAmount: blockedCountry ? 60000 : 50,
        sourceCurrency: blockedCountry ? "USD" : "CNY",
        targetCurrency: blockedCountry ? "CNY" : "USD",
        exchangeRate: blockedCountry ? 7.2 : 0.14,
        destinationCountry: blockedCountry ? "IR" : "US",
        swiftCode: blockedCountry ? "DEMOIRXX" : "BOFAUS3N",
        iban: null,
        remark: blockedCountry ? "frontend blocked country case" : "frontend remittance"
      });
      setRemittance(data);
      setOrderNo(data.orderNo);
      setResult(blockedCountry ? "已触发汇款风控拒绝" : "汇款已提交");
    } catch (caught) {
      setError(caught);
    }
  }

  async function query() {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.getRemittance(orderNo);
      setRemittance(data);
      setResult("汇款订单查询完成");
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>国际汇款</h2></div>
        <div className="form-grid">
          <label>
            汇款订单号
            <input value={orderNo} onChange={(event) => setOrderNo(event.target.value)} />
          </label>
          <button onClick={query}>查询汇款</button>
          <button onClick={() => submit(false)}>发起 50 CNY 汇款</button>
          <button className="danger" onClick={() => submit(true)}>发起国家风控案例</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="汇款响应" data={remittance} error={error} />
    </div>
  );
}
