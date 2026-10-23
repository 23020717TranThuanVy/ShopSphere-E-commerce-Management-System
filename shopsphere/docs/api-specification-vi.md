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
| `POST` | `/api/auth/register` | `{ "displayName": "Vy", "email": "vy@example.com", "password": "..." }` | `200`, tạo Customer và đăng nhập |
| `POST` | `/api/auth/login` | `{ "email": "vy@example.com", "password": "..." }` | `200`, tạo session |
| `GET` | `/api/auth/me` | — | `200`, tài khoản hiện tại; `401` nếu chưa đăng nhập |
| `POST` | `/api/auth/logout` | — | `204`, hủy session |

Response tài khoản gồm `id`, `displayName`, `email`, `role`. Đăng ký công khai luôn cấp role `CUSTOMER`; role không nhận từ request. Mật khẩu tối thiểu 8 ký tự, tối đa 72 ký tự; email phải hợp lệ. Email đã đăng ký trả `409 Conflict`; thông tin đăng nhập không hợp lệ trả `401`.

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
| `POST` | `/api/products` | ProductRequest | `200`, ADMIN |
| `PUT` | `/api/products/{id}` | ProductRequest | `200`, ADMIN |
| `DELETE` | `/api/products/{id}` | — | `204`, ADMIN |

Ví dụ ProductRequest:

```json
{
  "name": "Wireless keyboard",
  "price": 1200000,
  "description": "Mechanical keyboard",
  "stock": 20,
  "categoryId": 1
}
```

Response sản phẩm có `id`, `name`, `price`, `description`, `stock`, `categoryId`, `categoryName`.

## Validation và lỗi

- Product: tên không rỗng; giá bắt buộc và ít nhất `0.01`; stock bắt buộc, không âm; categoryId bắt buộc, dương và phải tồn tại.
- Category: tên không rỗng.
- Validation lỗi trả `400` với `fieldErrors`.
- Chưa tìm thấy product/category trả `404` với `ErrorResponse` gồm timestamp, status, error, message, path.
- Chưa đăng nhập trả `401`; thiếu quyền ADMIN trả `403`.
- Xóa danh mục đang được sản phẩm tham chiếu chưa có lỗi nghiệp vụ chuẩn hóa.

## Chưa triển khai

Chưa có API duyệt Seller, quản lý cửa hàng, cart/checkout/order/payment/shipment hoặc quản trị tài khoản và phân role. Các role `SELLER` và `SHIPPER` hiện chưa được cấp qua luồng ứng dụng.
