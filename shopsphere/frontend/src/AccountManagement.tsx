import { useEffect, useState } from 'react';
import { api } from './api';
import type { Account } from './types';

const statusLabel: Record<Account['status'], string> = {
  PENDING: 'Pending review',
  ACTIVE: 'Active',
  REJECTED: 'Rejected',
  SUSPENDED: 'Suspended',
};

export default function AccountManagement() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadAccounts() {
    setLoading(true);
    setError('');
    try {
      setAccounts(await api.accounts());
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Could not load accounts.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadAccounts(); }, []);

  async function reviewSeller(account: Account, approve: boolean) {
    setBusyId(account.id);
    setError('');
    setNotice('');
    try {
      const updated = approve ? await api.approveSeller(account.id) : await api.rejectSeller(account.id);
      setAccounts((rows) => rows.map((row) => row.id === updated.id ? updated : row));
      setNotice(approve ? `${account.storeName || account.displayName} approved.` : `${account.storeName || account.displayName} rejected.`);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Could not update Seller application.');
    } finally {
      setBusyId(null);
    }
  }

  return <section className="panel category-panel">
    <div className="panel-heading">
      <div><h2>Accounts</h2><p>Review Seller applications and view account status.</p></div>
      <button className="button button-light" onClick={() => void loadAccounts()} disabled={loading}>Refresh</button>
    </div>
    {error && <div className="alert-banner" role="alert">{error}</div>}
    {notice && <div className="auth-success" role="status">{notice}</div>}
    <div className="table-scroll"><table><thead><tr><th>ACCOUNT</th><th>ROLE</th><th>STATUS</th><th>CREATED</th><th>ACTIONS</th></tr></thead><tbody>
      {loading ? <tr><td className="table-state" colSpan={5}>Loading accounts…</td></tr> : accounts.length === 0 ? <tr><td className="table-state" colSpan={5}>No accounts found.</td></tr> : accounts.map((account) => <tr key={account.id}>
        <td><div className="product-info"><b>{account.displayName}</b><small>{account.email}</small></div></td>
        <td>{account.role}</td>
        <td><span className={`status-pill ${account.status === 'ACTIVE' ? 'status-good' : account.status === 'PENDING' ? 'status-low' : 'status-rejected'}`}><i/> {statusLabel[account.status]}</span></td>
        <td>{new Date(account.createdAt).toLocaleDateString()}</td>
        <td>{account.role === 'SELLER' && account.status === 'PENDING' ? <div className="row-actions">
          <button className="button button-primary button-small" disabled={busyId === account.id} onClick={() => void reviewSeller(account, true)}>{busyId === account.id ? 'Saving…' : 'Approve'}</button>
          <button className="button button-light button-small" disabled={busyId === account.id} onClick={() => void reviewSeller(account, false)}>Reject</button>
        </div> : '—'}</td>
      </tr>)}
    </tbody></table></div>
  </section>;
}
