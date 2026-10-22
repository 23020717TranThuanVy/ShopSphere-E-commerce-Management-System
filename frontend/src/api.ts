import type { Category, CategoryPayload, Product, ProductPayload } from './types';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
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
  products: () => request<Product[]>('/api/products'),
  createProduct: (payload: ProductPayload) => request<Product>('/api/products', { method: 'POST', body: JSON.stringify(payload) }),
  updateProduct: (id: number, payload: ProductPayload) => request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteProduct: (id: number) => request<void>(`/api/products/${id}`, { method: 'DELETE' }),
  categories: () => request<Category[]>('/api/categories'),
  createCategory: (payload: CategoryPayload) => request<Category>('/api/categories', { method: 'POST', body: JSON.stringify(payload) }),
  deleteCategory: (id: number) => request<void>(`/api/categories/${id}`, { method: 'DELETE' }),
};
