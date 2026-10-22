import { useState, type FormEvent } from 'react';
import { api } from './api';
import type { Account } from './types';

export default function AuthScreen({
  onAuthenticated,
  initialError,
}: {
  onAuthenticated: (account: Account) => Promise<void>;
  initialError: string;
}) {
  const [registering, setRegistering] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(initialError);
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const account = registering
        ? await api.register(displayName, email, password)
        : await api.login(email, password);
      await onAuthenticated(account);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Authentication failed.');
    } finally {
      setBusy(false);
    }
  }

  return <main className="auth-page">
    <section className="auth-card">
      <div className="auth-brand"><span className="brand-mark">S</span><span>shop<span>sphere</span></span></div>
      <div className="eyebrow">INVENTORY MANAGEMENT</div>
      <h1>{registering ? 'Create your account' : 'Welcome back'}</h1>
      <p className="auth-subtitle">{registering ? 'Create a Customer account to get started.' : 'Sign in to continue to your workspace.'}</p>
      {error && <div className="auth-error" role="alert">{error}</div>}
      <form onSubmit={(event) => void submit(event)}>
        {registering && <label className="field">Full name<input required maxLength={120} autoComplete="name" value={displayName} onChange={(event) => setDisplayName(event.target.value)} /></label>}
        <label className="field">Email<input required type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label className="field">Password<input required type="password" minLength={registering ? 8 : undefined} maxLength={72} autoComplete={registering ? 'new-password' : 'current-password'} value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <button className="button button-primary auth-submit" disabled={busy}>{busy ? 'Please wait…' : registering ? 'Create account' : 'Sign in'}</button>
      </form>
      <p className="auth-switch">{registering ? 'Already have an account?' : 'New to ShopSphere?'}{' '}
        <button type="button" onClick={() => { setRegistering(!registering); setError(''); }}> {registering ? 'Sign in' : 'Create an account'}</button>
      </p>
    </section>
  </main>;
}
