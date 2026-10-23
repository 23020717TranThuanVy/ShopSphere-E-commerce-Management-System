export type Product = {
  id: number;
  name: string;
  price: number;
  description: string | null;
  stock: number;
  categoryId: number;
  categoryName: string;
};

export type Category = {
  id: number;
  name: string;
  description: string | null;
};

export type ProductPayload = Omit<Product, 'id' | 'categoryName'>;
export type CategoryPayload = Omit<Category, 'id'>;

export type Account = {
  id: number;
  displayName: string;
  email: string;
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN' | 'SHIPPER';
};
