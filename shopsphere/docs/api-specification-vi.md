# ShopSphere — Đặc tả REST API hiện tại

> Chỉ liệt kê endpoint đã triển khai. API chưa có prefix `/api/v1`. Cập nhật: 2026-10-23.

## Thông tin chung

- Docker API: `http://localhost:8081`; chạy backend trực tiếp: `http://localhost:8080`.
- Request/response dùng JSON; Swagger UI: `/swagger-ui/index.html`; OpenAPI JSON: `/v3/api-docs`.
- Đăng nhập dùng HTTP session. Trình duyệt giữ cookie HttpOnly; không trả JWT.
- Request `POST`, `PUT`, `DELETE` cần CSRF token. Gọi `GET /api/auth/csrf`, đọc `token` và `headerName`, rồi gửi token trong header tương ứng.

## Authentication

| Method | Endpoint | Body | Thành công |
| --- | --- | --- | --- |
| `GET` | `/api/auth/csrf` | — | `200`, CSRF token gồm `token`, `headerName`, `parameterName` |
| `POST` | `/api/auth/register` | `{ "email": "vy@example.com", "password": "...", "accountType": "CUSTOMER" }` | Customer: `200`, tạo tài khoản và đăng nhập; Seller cũng chỉ cần email và password (`accountType=SELLER`): `202`, gửi hồ sơ chờ duyệt. Tên hiển thị được tạo từ phần email trước dấu `@`. |
| `POST` | `/api/auth/login` | `{ "email": "vy@example.com", "password": "..." }` | `200`, tạo session |
| `GET` | `/api/auth/me` | — | `200`, tài khoản hiện tại; `401` nếu chưa đăng nhập |
| `POST` | `/api/auth/logout` | — | `204`, hủy session |

Đăng ký nhận `accountType` là `CUSTOMER` hoặc `SELLER`; không thể đăng ký công khai thành `ADMIN` hoặc `SHIPPER`. Seller chỉ cần email và mật khẩu; hệ thống tạo hồ sơ `PENDING` và dùng phần trước `@` làm tên hiển thị ban đầu. Seller chưa thể đăng nhập trước khi Admin duyệt; thông tin cửa hàng sẽ được bổ sung ở luồng thiết lập Seller sau. Customer cần thêm tên hiển thị, được kích hoạt và đăng nhập ngay. Mật khẩu tối thiểu 8 ký tự, tối đa 72 ký tự; email phải hợp lệ. Email đã đăng ký trả `409 Conflict`; thông tin đăng nhập không hợp lệ trả `401`.

Response tài khoản gồm `id`, `displayName`, `email`, `role`, `status`, `storeName`, `createdAt`.

### Quản lý tài khoản (ADMIN)

| Method | Endpoint | Body | Thành công/Quyền |
| --- | --- | --- | --- |
| `GET` | `/api/admin/accounts` | — | `200`, danh sách tài khoản |
| `POST` | `/api/admin/accounts/{id}/approve` | — | `200`, duyệt Seller đang chờ |
| `POST` | `/api/admin/accounts/{id}/reject` | — | `200`, từ chối Seller đang chờ |

Chỉ ADMIN truy cập được nhóm endpoint này. Chỉ hồ sơ Seller ở trạng thái `PENDING` mới được duyệt/từ chối. Tài khoản Seller bị từ chối không thể đăng nhập.

## Danh mục

| Method | Endpoint | Body | Thành công/Quyền |
| --- | --- | --- | --- |
| `GET` | `/api/categories` | — | `200`, public |
| `GET` | `/api/categories/{id}` | — | `200`, public |
| `POST` | `/api/categories` | `{ "name": "Electronics", "description": "Devices" }` | `201`, ADMIN |
| `PUT` | `/api/categories/{id}` | CategoryRequest | `200`, ADMIN |
| `DELETE` | `/api/categories/{id}` | — | `204`, ADMIN |

Response danh mục có `id`, `name`, `description`.

## Sản phẩm

| Method | Endpoint | Body | Thành công/Quyền |
| --- | --- | --- | --- |
| `GET` | `/api/products` | — | `200`, public |
| `GET` | `/api/products/{id}` | — | `200`, public |
| `POST` | `/api/products` | ProductRequest | `200`, ADMIN tạo ACTIVE; Seller đã duyệt tạo PENDING_REVIEW |
| `PUT` | `/api/products/{id}` | ProductRequest | `200`, ADMIN hoặc Seller sở hữu; cập nhật bởi Seller đưa về PENDING_REVIEW |
| `DELETE` | `/api/products/{id}` | — | `204`, ADMIN hoặc Seller sở hữu |
| `PATCH` | `/api/admin/products/{id}/moderation` | `{ "status": "ACTIVE" }` | `200`, ADMIN; trạng thái có thể là ACTIVE, REJECTED hoặc SUSPENDED |

Ví dụ ProductRequest:

```json
{
  "name": "Wireless keyboard",
  "price": 1200000,
  "description": "Mechanical keyboard",
  "stock": 20,
  "categoryId": 1,
  "imageUrl": "https://example.com/keyboard.jpg"
}
```

Response sản phẩm có `id`, `name`, `price`, `description`, `stock`, `categoryId`, `categoryName`, `imageUrl`, `sellerId`, `sellerName`, `moderationStatus`. Danh sách public chỉ trả sản phẩm `ACTIVE`; Seller nhận sản phẩm của mình; Admin nhận mọi sản phẩm.

## Validation và lỗi

- Product: tên không rỗng; giá bắt buộc và ít nhất `0.01`; stock bắt buộc, không âm; categoryId bắt buộc, dương và phải tồn tại.
- Category: tên không rỗng.
- Validation lỗi trả `400` với `fieldErrors`.
- Chưa tìm thấy product/category trả `404` với `ErrorResponse` gồm timestamp, status, error, message, path.
- Chưa đăng nhập trả `401`; thiếu quyền ADMIN trả `403`.
- Xóa danh mục đang được sản phẩm tham chiếu chưa có lỗi nghiệp vụ chuẩn hóa.

## Chưa triển khai

Chưa có cart/checkout/order/payment/shipment hoặc giao diện Admin duyệt sản phẩm; API kiểm duyệt đã có. Seller chưa có giao diện CRUD sản phẩm riêng.
