import { useEffect, useMemo, useState, type FormEvent, type ReactNode } from 'react';
import { api } from './api';
import type { Account, Category, Product } from './types';
import AccountManagement from './AccountManagement';

type Page = 'products' | 'categories' | 'accounts';
type ProductDraft = { name: string; price: string; description: string; stock: string; categoryId: string; imageUrl: string };
const emptyDraft: ProductDraft = { name: '', price: '', description: '', stock: '0', categoryId: '', imageUrl: '' };
const money = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 });

function Icon({ name, size = 19 }: { name: string; size?: number }) {
  const common = { width: size, height: size, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const, 'aria-hidden': true as const };
  const paths: Record<string, ReactNode> = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/></>,
    box: <><path d="m3 7 9-4 9 4-9 4-9-4Z"/><path d="M3 7v10l9 4 9-4V7M12 11v10"/></>,
    tag: <><path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8Z"/><circle cx="7.5" cy="7.5" r="1"/></>,
    search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    plus: <><path d="M12 5v14M5 12h14"/></>,
    edit: <><path d="m15 5 4 4M4 20l4-.8L19 8a2.1 2.1 0 0 0-3-3L5 16l-1 4Z"/></>,
    trash: <><path d="M3 6h18M8 6V4h8v2m3 0-1 14H6L5 6m4 4v6m6-6v6"/></>,
    arrow: <><path d="M5 12h14m-6-6 6 6-6 6"/></>,
    chevron: <path d="m9 18 6-6-6-6"/>,
    close: <><path d="m18 6-12 12M6 6l12 12"/></>,
    refresh: <><path d="M20 7v5h-5M4 17v-5h5"/><path d="M5.5 9a7 7 0 0 1 12-2L20 12M4 12l2.5 5a7 7 0 0 0 12-2"/></>,
    bag: <><path d="M5 8h14l1 13H4L5 8Z"/><path d="M9 9V6a3 3 0 0 1 6 0v3"/></>,
    alert: <><path d="M12 3 2.8 19h18.4L12 3Z"/><path d="M12 9v4m0 3h.01"/></>,
  };
  return <svg {...common}>{paths[name] || paths.grid}</svg>;
}

export default function App({ account, onLogout }: { account: Account; onLogout: (event: FormEvent) => void }) {
  const canManageCatalog = account.role === 'ADMIN';
  const [page, setPage] = useState<Page>('products');
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [query, setQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [productModal, setProductModal] = useState(false);
  const [categoryModal, setCategoryModal] = useState(false);
  const [editing, setEditing] = useState<Product | null>(null);
  const [draft, setDraft] = useState<ProductDraft>(emptyDraft);
  const [categoryDraft, setCategoryDraft] = useState({ name: '', description: '' });
  const [saving, setSaving] = useState(false);

  async function loadData() {
    setLoading(true); setError('');
    try {
      const [productRows, categoryRows] = await Promise.all([api.products(), api.categories()]);
      setProducts(productRows); setCategories(categoryRows);
    } catch (e) { setError(e instanceof Error ? e.message : 'Không tải được dữ liệu.'); }
    finally { setLoading(false); }
  }
  useEffect(() => { void loadData(); }, []);
  useEffect(() => { if (!notice) return; const timer = window.setTimeout(() => setNotice(''), 3200); return () => window.clearTimeout(timer); }, [notice]);

  const visibleProducts = useMemo(() => products.filter((product) => {
    const matchesText = `${product.name} ${product.categoryName} ${product.description || ''}`.toLowerCase().includes(query.toLowerCase());
    return matchesText && (categoryFilter === 'all' || String(product.categoryId) === categoryFilter);
  }), [products, query, categoryFilter]);
  const lowStock = products.filter((product) => product.stock < 10).length;
  const totalStock = products.reduce((sum, product) => sum + product.stock, 0);
  const inventoryValue = products.reduce((sum, product) => sum + product.price * product.stock, 0);

  function openCreateProduct() { setEditing(null); setDraft({ ...emptyDraft, categoryId: categories[0] ? String(categories[0].id) : '' }); setProductModal(true); }
  function openEditProduct(product: Product) { setEditing(product); setDraft({ name: product.name, price: String(product.price), description: product.description || '', stock: String(product.stock), categoryId: String(product.categoryId), imageUrl: product.imageUrl || '' }); setProductModal(true); }

  async function saveProduct(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setError('');
    try {
      const payload = { name: draft.name.trim(), price: Number(draft.price), description: draft.description.trim() || null, stock: Number(draft.stock), categoryId: Number(draft.categoryId), imageUrl: draft.imageUrl.trim() || null };
      if (editing) { await api.updateProduct(editing.id, payload); setNotice('Đã cập nhật sản phẩm.'); }
      else { await api.createProduct(payload); setNotice('Đã thêm sản phẩm mới.'); }
      setProductModal(false); await loadData();
    } catch (e) { setError(e instanceof Error ? e.message : 'Không thể lưu sản phẩm.'); }
    finally { setSaving(false); }
  }
  async function removeProduct(product: Product) {
    if (!window.confirm(`Xóa sản phẩm “${product.name}”?`)) return;
    try { await api.deleteProduct(product.id); setProducts((rows) => rows.filter((row) => row.id !== product.id)); setNotice('Đã xóa sản phẩm.'); }
    catch (e) { setError(e instanceof Error ? e.message : 'Không thể xóa sản phẩm.'); }
  }
  async function saveCategory(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setError('');
    try { await api.createCategory({ name: categoryDraft.name.trim(), description: categoryDraft.description.trim() }); setCategoryDraft({ name: '', description: '' }); setCategoryModal(false); setNotice('Đã thêm danh mục.'); await loadData(); }
    catch (e) { setError(e instanceof Error ? e.message : 'Không thể tạo danh mục.'); }
    finally { setSaving(false); }
  }
  async function removeCategory(category: Category) {
    if (products.some((product) => product.categoryId === category.id)) { setError('Danh mục đang có sản phẩm. Hãy chuyển hoặc xóa sản phẩm trước.'); return; }
    if (!window.confirm(`Xóa danh mục “${category.name}”?`)) return;
    try { await api.deleteCategory(category.id); setCategories((rows) => rows.filter((row) => row.id !== category.id)); setNotice('Đã xóa danh mục.'); }
    catch (e) { setError(e instanceof Error ? e.message : 'Không thể xóa danh mục.'); }
  }

  return <div className="app-shell">
    <aside className="sidebar">
      <a className="brand" href="#top" aria-label="ShopSphere home"><span className="brand-mark"><Icon name="box" size={22}/></span><span>shop<span>sphere</span><small>MANAGEMENT</small></span></a>
      <div className="nav-label">WORKSPACE</div>
      <nav className="main-nav">
        <button className={`nav-item ${page === 'products' ? 'active' : ''}`} onClick={() => setPage('products')}><Icon name="grid"/><span>Products</span><span className="nav-count">{products.length}</span></button>
        <button className={`nav-item ${page === 'categories' ? 'active' : ''}`} onClick={() => setPage('categories')}><Icon name="tag"/><span>Categories</span><span className="nav-count">{categories.length}</span></button>
        {account.role === 'ADMIN' && <button className={`nav-item ${page === 'accounts' ? 'active' : ''}`} onClick={() => setPage('accounts')}><Icon name="grid"/><span>Accounts</span></button>}
      </nav>
      <div className="sidebar-bottom"><div className="connection"><span className="online-dot"/><span><b>{account.displayName}</b><small>{account.role}</small></span></div><div className="sidebar-foot">SHOPSPHERE <span>·</span> v1.0</div></div>
    </aside>

    <main className="main-content" id="top">
      <header className="topbar"><div className="breadcrumbs">Workspace <Icon name="chevron" size={14}/><span>{page === 'products' ? 'Products' : page === 'categories' ? 'Categories' : 'Accounts'}</span></div><div className="topbar-right"><span className="date-label">{account.email}</span><form onSubmit={onLogout}><button className="button button-light" type="submit">Sign out</button></form><div className="avatar">{account.displayName.charAt(0).toUpperCase()}</div></div></header>
      <div className="page-wrap">
        <section className="page-heading"><div><div className="eyebrow">{page === 'accounts' ? 'ADMINISTRATION' : 'YOUR STORE AT A GLANCE'}</div><h1>{page === 'products' ? 'Products' : page === 'categories' ? 'Categories' : 'Accounts'}<span className="heading-dot"/></h1><p>{page === 'products' ? 'Manage your catalog, pricing and stock levels.' : page === 'categories' ? 'Organize products into clear, easy-to-manage groups.' : 'Manage accounts and review Seller applications.'}</p></div>{page !== 'accounts' && <div className="heading-actions"><button className="button button-light" onClick={() => void loadData()} disabled={loading}><Icon name="refresh" size={17}/> Refresh</button>{canManageCatalog && (page === 'products' ? <button className="button button-primary" onClick={openCreateProduct}><Icon name="plus" size={18}/> Add product</button> : <button className="button button-primary" onClick={() => setCategoryModal(true)}><Icon name="plus" size={18}/> Add category</button>)}</div>}</section>

        {page !== 'accounts' && <section className="stats-grid" aria-label="Store summary">
          <article className="stat-card"><div className="stat-top"><span className="stat-label">Total products</span><span className="stat-icon purple"><Icon name="box"/></span></div><div className="stat-number">{products.length}<span className="stat-unit">items</span></div><div className="stat-caption">Across {categories.length} categories</div></article>
          <article className="stat-card"><div className="stat-top"><span className="stat-label">Units in stock</span><span className="stat-icon green"><Icon name="bag"/></span></div><div className="stat-number">{totalStock.toLocaleString('vi-VN')}<span className="stat-unit">units</span></div><div className="stat-caption">Total available inventory</div></article>
          <article className="stat-card"><div className="stat-top"><span className="stat-label">Inventory value</span><span className="stat-icon orange"><Icon name="tag"/></span></div><div className="stat-number compact">{money.format(inventoryValue)}</div><div className="stat-caption">Based on current stock × price</div></article>
          <article className="stat-card"><div className="stat-top"><span className="stat-label">Low stock</span><span className="stat-icon red"><Icon name="alert"/></span></div><div className="stat-number">{lowStock}<span className="stat-unit">items</span></div><div className="stat-caption">Products with fewer than 10 units</div></article>
        </section>}

        {!canManageCatalog && <div className="alert-banner"><Icon name="alert" size={18}/><span>{account.role === 'SELLER' ? 'Your Seller account is active. Seller-owned product management is not available yet.' : `You are signed in as ${account.role}. Catalog changes are currently restricted to administrators.`}</span></div>}
        {error && <div className="alert-banner"><Icon name="alert" size={18}/><span>{error}</span><button onClick={() => setError('')} aria-label="Dismiss"><Icon name="close" size={17}/></button></div>}
        {page === 'accounts' ? <AccountManagement/> : page === 'products' ? <section className="panel">
          <div className="panel-heading"><div><h2>Product catalog</h2><p>Keep track of everything you sell.</p></div><div className="table-tools"><label className="search-box"><Icon name="search" size={17}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search products..." aria-label="Search products"/><kbd>⌘ K</kbd></label><select className="filter-select" value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)} aria-label="Filter by category"><option value="all">All categories</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></div></div>
          <div className="table-scroll"><table><thead><tr><th>PRODUCT</th><th>CATEGORY</th><th>PRICE</th><th>STOCK</th><th>STATUS</th><th><span className="sr-only">Actions</span></th></tr></thead><tbody>
            {loading ? <tr><td className="table-state" colSpan={6}><span className="spinner"/> Loading your catalog...</td></tr> : visibleProducts.length === 0 ? <tr><td className="table-state" colSpan={6}><div className="empty-state"><span className="empty-icon"><Icon name="box" size={25}/></span><b>{products.length ? 'No matching products' : 'Your catalog is empty'}</b><span>{products.length ? 'Try another search or category filter.' : 'Add your first product to get started.'}</span>{canManageCatalog && !products.length && <button className="button button-primary button-small" onClick={openCreateProduct}><Icon name="plus" size={16}/> Add product</button>}</div></td></tr> : visibleProducts.map((product, index) => <tr key={product.id}><td><div className="product-cell"><span className={`product-thumb tone-${index % 5}`}>{product.name.trim().charAt(0).toUpperCase()}</span><span className="product-info"><b>{product.name}</b><small>SKU-{String(product.id).padStart(4, '0')}</small></span></div></td><td><span className="category-pill">{product.categoryName || 'Uncategorized'}</span></td><td className="price-cell">{money.format(product.price)}</td><td className="stock-cell">{product.stock} <span>units</span></td><td><span className={`status-pill ${product.stock < 10 ? 'status-low' : 'status-good'}`}><i/> {product.stock < 10 ? 'Low stock' : 'In stock'}</span></td><td>{canManageCatalog && <div className="row-actions"><button className="icon-button" onClick={() => openEditProduct(product)} aria-label={`Edit ${product.name}`} title="Edit"><Icon name="edit" size={17}/></button><button className="icon-button danger-hover" onClick={() => void removeProduct(product)} aria-label={`Delete ${product.name}`} title="Delete"><Icon name="trash" size={17}/></button></div>}</td></tr>)}
          </tbody></table></div>
          <div className="table-footer"><span>Showing <b>{visibleProducts.length}</b> of <b>{products.length}</b> products</span><span>Stock alerts below <b>10 units</b></span></div>
        </section> : <section className="panel category-panel"><div className="panel-heading"><div><h2>All categories</h2><p>{categories.length} categories in your store</p></div></div>{loading ? <div className="category-loading"><span className="spinner"/> Loading categories...</div> : categories.length === 0 ? <div className="empty-state category-empty"><span className="empty-icon"><Icon name="tag" size={25}/></span><b>No categories yet</b><span>Create a category before adding products.</span>{canManageCatalog && <button className="button button-primary button-small" onClick={() => setCategoryModal(true)}><Icon name="plus" size={16}/> Add category</button>}</div> : <div className="category-grid">{categories.map((category, index) => { const count = products.filter((product) => product.categoryId === category.id).length; return <article className="category-card" key={category.id}><div className="category-card-top"><span className={`category-icon category-tone-${index % 5}`}><Icon name="tag" size={20}/></span>{canManageCatalog && <button className="icon-button danger-hover" onClick={() => void removeCategory(category)} aria-label={`Delete ${category.name}`} title="Delete"><Icon name="trash" size={17}/></button>}</div><h3>{category.name}</h3><p>{category.description || 'No description provided.'}</p><div className="category-card-foot"><span>{count} {count === 1 ? 'product' : 'products'}</span><button onClick={() => { setPage('products'); setCategoryFilter(String(category.id)); }}>View products <Icon name="arrow" size={15}/></button></div></article>; })}</div>}</section>}
        <footer className="page-footer"><span>© 2026 ShopSphere</span><span>Inventory management <i/> All systems operational</span></footer>
      </div>
    </main>

    {productModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setProductModal(false); }}><section className="modal" role="dialog" aria-modal="true" aria-labelledby="product-modal-title"><div className="modal-header"><div><span className="eyebrow">PRODUCT CATALOG</span><h2 id="product-modal-title">{editing ? 'Edit product' : 'Add a product'}</h2></div><button className="icon-button" onClick={() => setProductModal(false)} aria-label="Close"><Icon name="close"/></button></div>{categories.length === 0 ? <div className="modal-empty"><p>Create a category before adding products.</p><button className="button button-primary" onClick={() => { setProductModal(false); setCategoryModal(true); }}><Icon name="plus" size={17}/> Create category</button></div> : <form onSubmit={saveProduct}><label className="field">Product name<input required maxLength={150} autoFocus value={draft.name} onChange={(event) => setDraft({ ...draft, name: event.target.value })} placeholder="e.g. Wireless headphones"/></label><div className="field-row"><label className="field">Price (VND)<input required type="number" min="0.01" step="any" value={draft.price} onChange={(event) => setDraft({ ...draft, price: event.target.value })} placeholder="0"/></label><label className="field">Stock quantity<input required type="number" min="0" step="1" value={draft.stock} onChange={(event) => setDraft({ ...draft, stock: event.target.value })} placeholder="0"/></label></div><label className="field">Category<select required value={draft.categoryId} onChange={(event) => setDraft({ ...draft, categoryId: event.target.value })}>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label><label className="field">Image URL <span className="optional">Optional</span><input type="url" maxLength={2048} value={draft.imageUrl} onChange={(event) => setDraft({ ...draft, imageUrl: event.target.value })} placeholder="https://..."/></label><label className="field">Description <span className="optional">Optional</span><textarea rows={3} maxLength={1000} value={draft.description} onChange={(event) => setDraft({ ...draft, description: event.target.value })} placeholder="Add a few details about this product"/></label><div className="modal-actions"><button type="button" className="button button-light" onClick={() => setProductModal(false)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving...' : editing ? 'Save changes' : 'Create product'} <Icon name="arrow" size={16}/></button></div></form>}</section></div>}

    {categoryModal && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setCategoryModal(false); }}><section className="modal modal-small" role="dialog" aria-modal="true" aria-labelledby="category-modal-title"><div className="modal-header"><div><span className="eyebrow">PRODUCT ORGANIZATION</span><h2 id="category-modal-title">New category</h2></div><button className="icon-button" onClick={() => setCategoryModal(false)} aria-label="Close"><Icon name="close"/></button></div><form onSubmit={saveCategory}><label className="field">Category name<input required maxLength={100} autoFocus value={categoryDraft.name} onChange={(event) => setCategoryDraft({ ...categoryDraft, name: event.target.value })} placeholder="e.g. Accessories"/></label><label className="field">Description <span className="optional">Optional</span><textarea rows={3} value={categoryDraft.description} onChange={(event) => setCategoryDraft({ ...categoryDraft, description: event.target.value })} placeholder="What belongs in this category?"/></label><div className="modal-actions"><button type="button" className="button button-light" onClick={() => setCategoryModal(false)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving...' : 'Create category'} <Icon name="arrow" size={16}/></button></div></form></section></div>}
    {notice && <div className="toast"><span className="toast-check">✓</span>{notice}</div>}
  </div>;
}
