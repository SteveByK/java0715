import { useState } from "react";
import { bankApi } from "../api/client";
import type { Quote, RemittanceOrder } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

// Remittance scenario page: quote, submit and query international remittance orders.
export function RemittancePage() {
  const [orderNo, setOrderNo] = useState("RM_DEMO_SUCCESS");
  const [remittance, setRemittance] = useState<RemittanceOrder | null>(null);
  const [quote, setQuote] = useState<Quote | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function quoteCurrent() {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.quoteRemittance("CNY", "USD", 700);
      setQuote(data);
      setResult(`报价已锁定，有效期至 ${new Date(data.expiresAt).toLocaleString()}`);
    } catch (caught) {
      setError(caught);
    }
  }

  async function submit(blockedCountry = false) {
    setError(null);
    setResult(null);
    try {
      const payload = blockedCountry ? {
        requestId: `ui-rm-${Date.now()}`,
        senderAccountNo: "AC_DEMO_USD_002",
        receiverAccountNo: "AC_DEMO_CNY_002",
        sourceAmount: 60000,
        sourceCurrency: "USD",
        targetCurrency: "CNY",
        destinationCountry: "IR",
        swiftCode: "DEMOIRXX",
        iban: null,
        remark: "frontend blocked country case"
      } : await buildQuotedPayload();
      const data = await bankApi.remit(payload);
      setRemittance(data);
      setOrderNo(data.orderNo);
      setResult(blockedCountry ? "已触发汇款风控拒绝" : "汇款已按锁定报价提交");
    } catch (caught) {
      setError(caught);
    }
  }

  async function buildQuotedPayload() {
    const activeQuote = quote ?? await bankApi.quoteRemittance("CNY", "USD", 50);
    return {
        requestId: `ui-rm-${Date.now()}`,
        senderAccountNo: "AC_DEMO_CNY_001",
        receiverAccountNo: "AC_DEMO_USD_001",
        sourceAmount: activeQuote.sourceAmount,
        sourceCurrency: activeQuote.sourceCurrency,
        targetCurrency: activeQuote.targetCurrency,
        quoteId: activeQuote.quoteId,
        destinationCountry: "US",
        swiftCode: "BOFAUS3N",
        iban: null,
        remark: "frontend remittance with locked quote"
    };
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
          <button onClick={quoteCurrent}>锁定 700 CNY 报价</button>
          <button onClick={() => submit(false)}>按报价发起汇款</button>
          <button className="danger" onClick={() => submit(true)}>发起国家风控案例</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="汇款响应" data={{ remittance, quote }} error={error} />
    </div>
  );
}
