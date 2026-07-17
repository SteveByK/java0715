import {
  Activity,
  Banknote,
  BookOpen,
  CircleDollarSign,
  ClipboardList,
  Contact,
  Gauge,
  Landmark,
  RadioTower,
  ShieldAlert
} from "lucide-react";
import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { AccountsPage } from "./pages/AccountsPage";
import { AuditPage } from "./pages/AuditPage";
import { DashboardPage } from "./pages/DashboardPage";
import { LedgerPage } from "./pages/LedgerPage";
import { OutboxPage } from "./pages/OutboxPage";
import { CustomersPage } from "./pages/CustomersPage";
import { PricingPage } from "./pages/PricingPage";
import { RemittancePage } from "./pages/RemittancePage";
import { TransferPage } from "./pages/TransferPage";

// Application shell: navigation, route table and page layout for the banking console.
const navItems = [
  { to: "/dashboard", label: "总览", icon: Gauge },
  { to: "/customers", label: "客户KYC", icon: Contact },
  { to: "/accounts", label: "账户", icon: Landmark },
  { to: "/transfers", label: "国内转账", icon: Banknote },
  { to: "/remittances", label: "国际汇款", icon: CircleDollarSign },
  { to: "/pricing", label: "报价", icon: Activity },
  { to: "/ledger", label: "账本", icon: BookOpen },
  { to: "/audit", label: "审计", icon: ClipboardList },
  { to: "/outbox", label: "Outbox", icon: RadioTower }
];

export function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark"><ShieldAlert size={22} /></div>
          <div>
            <strong>java0715</strong>
            <span>Banking Console</span>
          </div>
        </div>
        <nav className="nav-list">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}>
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>
      <main className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Spring Boot API</p>
            <h1>银行业务控制台</h1>
          </div>
          <div className="api-pill">
            <Activity size={16} />
            <span>Proxy: localhost:8081</span>
          </div>
        </header>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/customers" element={<CustomersPage />} />
          <Route path="/accounts" element={<AccountsPage />} />
          <Route path="/transfers" element={<TransferPage />} />
          <Route path="/remittances" element={<RemittancePage />} />
          <Route path="/pricing" element={<PricingPage />} />
          <Route path="/ledger" element={<LedgerPage />} />
          <Route path="/audit" element={<AuditPage />} />
          <Route path="/outbox" element={<OutboxPage />} />
        </Routes>
      </main>
    </div>
  );
}
