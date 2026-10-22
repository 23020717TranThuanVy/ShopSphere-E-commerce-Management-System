import { useEffect, useState, type FormEvent } from 'react';
import { api } from './api';
import type { Account } from './types';
import App from './App';
import AuthScreen from './AuthScreen';

export default function AuthGate() {
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void (async () => {
      try {
        await api.initializeCsrf();
        setAccount(await api.me());
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : 'Could not connect to ShopSphere.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function signIn(nextAccount: Account) {
    setAccount(nextAccount);
    await api.initializeCsrf();
  }

  async function signOut(event: FormEvent) {
    event.preventDefault();
    try {
      await api.logout();
      setAccount(null);
      await api.initializeCsrf();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Could not sign out.');
    }
  }

  if (loading) return <div className="auth-loading">Connecting to ShopSphere…</div>;
  if (!account) return <AuthScreen onAuthenticated={signIn} initialError={error} />;
  return <App account={account} onLogout={signOut} />;
}
