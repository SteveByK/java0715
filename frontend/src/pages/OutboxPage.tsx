import { useState } from "react";
import { bankApi } from "../api/client";
import type { OutboxEvent } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

export function OutboxPage() {
  const [aggregateId, setAggregateId] = useState("RM_DEMO_SUCCESS");
  const [events, setEvents] = useState<OutboxEvent[] | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function query() {
    setError(null);
    setResult(null);
    try {
      const data = await bankApi.outboxByAggregateId(aggregateId);
      setEvents(data);
      setResult(`查询到 ${data.length} 条 Outbox 事件`);
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>Outbox 事件</h2></div>
        <div className="form-grid">
          <label>
            聚合 ID
            <input value={aggregateId} onChange={(event) => setAggregateId(event.target.value)} />
          </label>
          <button onClick={query}>查询事件</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="事件数据" data={events} error={error} />
    </div>
  );
}
