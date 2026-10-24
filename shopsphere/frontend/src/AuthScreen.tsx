import { useState, type FormEvent } from 'react';
import { api, ApiError } from './api';
import type { Account } from './types';

export default function AuthScreen({
  onAuthenticated,
  initialError,
}: {
  onAuthenticated: (account: Account) => Promise<void>;
  initialError: string;
}) {
  const [registering, setRegistering] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [accountType, setAccountType] = useState<'CUSTOMER' | 'SELLER' | null>(null);
  const [error, setError] = useState(initialError);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);

  function clearFieldError(field: string) {
    setFieldErrors((current) => {
      if (!(field in current)) return current;
      const next = { ...current };
      delete next[field];
      return next;
    });
    setError('');
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (registering && !accountType) return;
    setBusy(true);
    setError('');
    setFieldErrors({});
    try {
      if (registering) {
        if (accountType === null) return;
        const selectedAccountType = accountType;
        const result = await api.register(email, password, selectedAccountType);
        if ('message' in result) {
          setSuccess(result.message);
          setRegistering(false);
          setAccountType(null);
          setPassword('');
          return;
        }
        await onAuthenticated(result);
      } else {
        await onAuthenticated(await api.login(email, password));
      }
    } catch (reason) {
      if (reason instanceof ApiError) {
        setFieldErrors(reason.fieldErrors);
        setError(Object.keys(reason.fieldErrors).length ? 'Please check the highlighted fields.' : reason.message);
      } else {
        setError(reason instanceof Error ? reason.message : 'Authentication failed.');
      }
    } finally {
      setBusy(false);
    }
  }

  return <main className="auth-page">
    <section className="auth-card">
      <div className="auth-brand"><span className="brand-mark">S</span><span>shop<span>sphere</span></span></div>
      <div className="eyebrow">INVENTORY MANAGEMENT</div>
      <h1>{registering ? accountType ? `Create a ${accountType === 'CUSTOMER' ? 'Customer' : 'Seller'} account` : 'Join ShopSphere' : 'Welcome back'}</h1>
      <p className="auth-subtitle">{registering ? accountType === 'SELLER' ? 'Enter your email and password. Admin approval is required.' : accountType === 'CUSTOMER' ? 'Create your account and explore the catalog.' : 'Choose how you want to use ShopSphere.' : 'Sign in to continue to your workspace.'}</p>
      {success && <div className="auth-success" role="status">{success}</div>}
      {registering && !accountType && <div className="account-type-grid">
        <button type="button" className="account-type-card" onClick={() => { setAccountType('CUSTOMER'); setError(''); }}>
          <span className="role-card-icon customer-role-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c.8-3.4 3.1-5.2 7-5.2s6.2 1.8 7 5.2"/></svg></span>
          <strong>Customer</strong><span>Explore products and your shopping workspace.</span><small>For personal shopping <b>→</b></small>
        </button>
        <button type="button" className="account-type-card" onClick={() => { setAccountType('SELLER'); setError(''); }}>
          <span className="role-card-icon seller-role-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 10v10h16V10M3 10l2-6h14l2 6"/><path d="M3 10a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0M9 20v-5h6v5"/></svg></span>
          <strong>Seller</strong><span>Apply to sell on ShopSphere.</span><small>Admin approval required <b>→</b></small>
        </button>
      </div>}
      {(!registering || accountType) && <form onSubmit={(event) => void submit(event)}>
        {registering && accountType && <>
          <button type="button" className="auth-back" onClick={() => { setAccountType(null); setError(''); }}>← Change account type</button>
        </>}
        <label className="field">Email<input required type="email" autoComplete="email" value={email} aria-invalid={Boolean(fieldErrors.email)} aria-describedby={fieldErrors.email ? 'email-error' : undefined} className={fieldErrors.email ? 'field-invalid' : undefined} onChange={(event) => { setEmail(event.target.value); clearFieldError('email'); }} />{fieldErrors.email && <span className="field-validation-error" id="email-error">{fieldErrors.email}</span>}</label>
        <label className="field">Password<div className="password-input-wrap"><input required type={showPassword ? 'text' : 'password'} minLength={registering ? 8 : undefined} maxLength={72} autoComplete={registering ? 'new-password' : 'current-password'} value={password} aria-invalid={Boolean(fieldErrors.password)} aria-describedby={fieldErrors.password ? 'password-error' : undefined} className={fieldErrors.password ? 'field-invalid' : undefined} onChange={(event) => { setPassword(event.target.value); clearFieldError('password'); }} /><button type="button" className="password-visibility" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? 'Hide password' : 'Show password'} aria-pressed={showPassword}><svg viewBox="0 0 24 24" aria-hidden="true">{showPassword ? <><path d="M3 3l18 18"/><path d="M10.6 10.6a2 2 0 0 0 2.8 2.8"/><path d="M9.9 5.2A10.8 10.8 0 0 1 12 5c5.2 0 8.8 4.7 9.5 6-.3.6-1.2 1.8-2.6 3"/><path d="M6.2 6.2C4.2 7.5 3 9.3 2.5 11c.7 1.3 4.3 6 9.5 6 1 0 2-.2 2.8-.5"/></> : <><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.5"/></>}</svg></button></div>{fieldErrors.password && <span className="field-validation-error" id="password-error">{fieldErrors.password}</span>}</label>
        <button className="button button-primary auth-submit" disabled={busy}>{busy ? 'Please wait…' : registering ? 'Create account' : 'Sign in'}</button>
      </form>}
      {error && <div className="auth-error auth-error-bottom" role="alert">{error}</div>}
      <p className="auth-switch">{registering ? 'Already have an account?' : 'New to ShopSphere?'}{' '}
        <button type="button" onClick={() => { setRegistering(!registering); setAccountType(null); setError(''); setSuccess(''); }}> {registering ? 'Sign in' : 'Create an account'}</button>
      </p>
    </section>
  </main>;
}
