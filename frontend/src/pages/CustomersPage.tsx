import { useState } from "react";
import { bankApi } from "../api/client";
import type { Customer } from "../api/types";
import { DataPanel } from "../components/DataPanel";
import { ResultNotice } from "../components/ResultNotice";

export function CustomersPage() {
  const [customerId, setCustomerId] = useState("C_DEMO_001");
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [result, setResult] = useState<string | null>(null);

  async function run(action: () => Promise<Customer>, success: string) {
    setError(null);
    setResult(null);
    try {
      const data = await action();
      setCustomer(data);
      setResult(success);
    } catch (caught) {
      setError(caught);
    }
  }

  return (
    <div className="two-column">
      <section className="panel">
        <div className="panel-heading"><h2>客户 / KYC</h2></div>
        <div className="form-grid">
          <label>
            客户号
            <input value={customerId} onChange={(event) => setCustomerId(event.target.value)} />
          </label>
          <button onClick={() => run(() => bankApi.getCustomer(customerId), "客户查询完成")}>查询客户</button>
          <button onClick={() => {
            const id = `CUI${Date.now()}`;
            setCustomerId(id);
            return run(() => bankApi.createCustomer({
              customerId: id,
              fullName: "Frontend Customer",
              userRegion: "DOMESTIC",
              countryCode: "CN",
              phone: "+8613900000000",
              email: "frontend.customer@example.com"
            }), "客户创建完成");
          }}>创建客户</button>
          <button onClick={() => run(() => bankApi.submitKyc(customerId, {
            documentType: "NATIONAL_ID",
            maskedDocumentNo: "110***********888",
            kycLevel: "STANDARD"
          }), "KYC 已提交")}>提交 KYC</button>
          <button onClick={() => run(() => bankApi.reviewKyc(customerId, {
            status: "APPROVED",
            reviewedBy: "frontend-admin"
          }), "KYC 已审核")}>审核通过</button>
        </div>
        <ResultNotice result={result} error={error} />
      </section>
      <DataPanel title="客户响应" data={customer} error={error} />
    </div>
  );
}
