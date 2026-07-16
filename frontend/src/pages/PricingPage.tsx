import { useState } from "react";
import { bankApi } from "../api/client";
import type { Quote } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

export function PricingPage() {
  const [quote, setQuote] = useState<Quote | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function query(sourceCurrency: string, targetCurrency: string, amount: number) {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.quoteRemittance(sourceCurrency, targetCurrency, amount);
      setQuote(data);
      setResult("报价完成");
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>汇率 / 手续费报价</h2></div>
        <div className="form-grid">
          <button onClick={() => query("CNY", "USD", 700)}>CNY 700 到 USD</button>
          <button onClick={() => query("USD", "CNY", 60000)}>USD 60000 到 CNY</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="报价响应" data={quote} error={error} />
    </div>
  );
}
