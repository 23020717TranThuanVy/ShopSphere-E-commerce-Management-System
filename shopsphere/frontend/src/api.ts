import type { Account, Category, CategoryPayload, PendingSellerRegistration, Product, ProductPayload } from './types';

let csrfToken: string | null = null;
let csrfHeaderName = 'X-CSRF-TOKEN';

export class ApiError extends Error {
  readonly fieldErrors: Record<string, string>;

  constructor(message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
    this.fieldErrors = fieldErrors;
  }
}

async function refreshCsrf(): Promise<void> {
  const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
  if (!response.ok) throw new Error('Unable to initialize secure session.');
  const token = await response.json() as { token: string; headerName: string };
  csrfToken = token.token;
  csrfHeaderName = token.headerName;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const method = (init?.method || 'GET').toUpperCase();
  const headers = new Headers(init?.headers);
  if (init?.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
    if (!csrfToken) await refreshCsrf();
    headers.set(csrfHeaderName, csrfToken!);
  }
  const response = await fetch(path, {
    ...init,
    credentials: 'same-origin',
    headers,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const fieldErrors = (body?.fieldErrors || {}) as Record<string, string>;
    const labels: Record<string, string> = { displayName: 'Display name', email: 'Email', password: 'Password', accountType: 'Account type', storeName: 'Store name' };
    const fieldSummary = Object.entries(fieldErrors).map(([field, message]) => `${labels[field] || field}: ${message}`).join(' · ');
    const readableMessage = (message: string) => message
      .replace(/^must not be blank$/i, 'is required')
      .replace(/^must be a well-formed email address$/i, 'must be a valid email address');
    const readableFieldErrors = Object.fromEntries(Object.entries(fieldErrors).map(([field, message]) => [field, readableMessage(message)]));
    throw new ApiError(fieldSummary || body?.message || `Request failed (${response.status})`, readableFieldErrors);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  initializeCsrf: refreshCsrf,
  me: async () => {
    const response = await fetch('/api/auth/me', { credentials: 'same-origin' });
    if (response.status === 401) return null;
    if (!response.ok) throw new Error('Unable to read the current session.');
    return response.json() as Promise<Account>;
  },
  login: (email: string, password: string) => request<Account>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  register: (email: string, password: string, accountType: 'CUSTOMER' | 'SELLER') => request<Account | PendingSellerRegistration>('/api/auth/register', { method: 'POST', body: JSON.stringify({ email, password, accountType }) }),
  logout: async () => {
    await request<void>('/api/auth/logout', { method: 'POST' });
    csrfToken = null;
  },
  products: () => request<Product[]>('/api/products'),
  createProduct: (payload: ProductPayload) => request<Product>('/api/products', { method: 'POST', body: JSON.stringify(payload) }),
  updateProduct: (id: number, payload: ProductPayload) => request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteProduct: (id: number) => request<void>(`/api/products/${id}`, { method: 'DELETE' }),
  moderateProduct: (id: number, status: 'ACTIVE' | 'REJECTED' | 'SUSPENDED') => request<Product>(`/api/admin/products/${id}/moderation`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  categories: () => request<Category[]>('/api/categories'),
  createCategory: (payload: CategoryPayload) => request<Category>('/api/categories', { method: 'POST', body: JSON.stringify(payload) }),
  deleteCategory: (id: number) => request<void>(`/api/categories/${id}`, { method: 'DELETE' }),
  accounts: () => request<Account[]>('/api/admin/accounts'),
  approveSeller: (id: number) => request<Account>(`/api/admin/accounts/${id}/approve`, { method: 'POST' }),
  rejectSeller: (id: number) => request<Account>(`/api/admin/accounts/${id}/reject`, { method: 'POST' }),
};
