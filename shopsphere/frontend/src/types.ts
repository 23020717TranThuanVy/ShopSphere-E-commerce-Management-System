export type Product = {
  id: number;
  name: string;
  price: number;
  description: string | null;
  stock: number;
  categoryId: number;
  categoryName: string;
  imageUrl: string | null;
  sellerId: number | null;
  sellerName: string | null;
  moderationStatus: 'PENDING_REVIEW' | 'ACTIVE' | 'REJECTED' | 'SUSPENDED';
};

export type Category = {
  id: number;
  name: string;
  description: string | null;
};

export type ProductPayload = {
  name: string;
  price: number;
  description: string | null;
  stock: number;
  categoryId: number;
  imageUrl?: string | null;
};
export type CategoryPayload = Omit<Category, 'id'>;

export type Account = {
  id: number;
  displayName: string;
  email: string;
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN' | 'SHIPPER';
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'SUSPENDED';
  storeName: string | null;
  createdAt: string;
};

export type PendingSellerRegistration = { message: string };
