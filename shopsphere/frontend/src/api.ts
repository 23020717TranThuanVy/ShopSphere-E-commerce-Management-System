import type { Account, Category, CategoryPayload, Product, ProductPayload } from './types';

let csrfToken: string | null = null;
let csrfHeaderName = 'X-CSRF-TOKEN';

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
    const fieldErrors = body?.fieldErrors ? Object.values(body.fieldErrors).join(' · ') : '';
    throw new Error(fieldErrors || body?.message || `Request failed (${response.status})`);
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
  register: (displayName: string, email: string, password: string) => request<Account>('/api/auth/register', { method: 'POST', body: JSON.stringify({ displayName, email, password }) }),
  logout: async () => {
    await request<void>('/api/auth/logout', { method: 'POST' });
    csrfToken = null;
  },
  products: () => request<Product[]>('/api/products'),
  createProduct: (payload: ProductPayload) => request<Product>('/api/products', { method: 'POST', body: JSON.stringify(payload) }),
  updateProduct: (id: number, payload: ProductPayload) => request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteProduct: (id: number) => request<void>(`/api/products/${id}`, { method: 'DELETE' }),
  categories: () => request<Category[]>('/api/categories'),
  createCategory: (payload: CategoryPayload) => request<Category>('/api/categories', { method: 'POST', body: JSON.stringify(payload) }),
  deleteCategory: (id: number) => request<void>(`/api/categories/${id}`, { method: 'DELETE' }),
};
