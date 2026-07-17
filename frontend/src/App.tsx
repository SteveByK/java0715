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
import { FormEvent, useState } from "react";
import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { bankApi, clearSession, getErrorMessage, getStoredUser } from "./api/client";
import type { AuthUser } from "./api/types";
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
  const [user, setUser] = useState<AuthUser | null>(() => getStoredUser());

  if (!user) {
    return <LoginScreen onLogin={setUser} />;
  }

  const logout = async () => {
    await bankApi.logout();
    clearSession();
    setUser(null);
  };

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
          <div className="topbar-actions">
            <div className="api-pill">
              <Activity size={16} />
              <span>{user.displayName} · {user.roles.join(", ")}</span>
            </div>
            <button type="button" className="secondary" onClick={logout}>退出</button>
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

function LoginScreen({ onLogin }: { onLogin: (user: AuthUser) => void }) {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await bankApi.login(username, password);
      onLogin({
        userId: response.userId,
        username: response.username,
        displayName: response.displayName,
        roles: response.roles,
        permissions: response.permissions
      });
    } catch (loginError) {
      setError(getErrorMessage(loginError));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="login-layout">
      <section className="login-panel">
        <div className="brand login-brand">
          <div className="brand-mark"><ShieldAlert size={22} /></div>
          <div>
            <strong>java0715</strong>
            <span>Banking Console</span>
          </div>
        </div>
        <form className="form-grid" onSubmit={submit}>
          <label>
            用户名
            <input value={username} onChange={(event) => setUsername(event.target.value)} />
          </label>
          <label>
            密码
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
          </label>
          <button type="submit" disabled={loading}>{loading ? "登录中..." : "登录"}</button>
        </form>
        {error && <div className="notice error">{error}</div>}
        <div className="login-help">
          <span>admin/admin123 全权限</span>
          <span>teller/teller123 柜员权限</span>
          <span>auditor/auditor123 审计只读</span>
        </div>
      </section>
    </main>
  );
}
