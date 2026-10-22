# ShopSphere — Đặc tả REST API hiện tại

> Chỉ liệt kê endpoint được ánh xạ trong các controller hiện tại. API chưa có prefix `/api/v1` và chưa yêu cầu xác thực. Cập nhật: 2026-10-23.

## Thông tin chung

- Base URL Docker: `http://localhost:8081`.
- Base URL khi chạy Spring Boot trực tiếp: `http://localhost:8080`.
- Base path: `/api`.
- Request/response dùng JSON (`Content-Type: application/json`).
- Swagger UI: `/swagger-ui/index.html`; OpenAPI JSON: `/v3/api-docs`.

## Danh mục

| Method | Endpoint | Body | Thành công |
| --- | --- | --- | --- |
| `GET` | `/api/categories` | — | `200`, mảng danh mục |
| `GET` | `/api/categories/{id}` | — | `200`, danh mục |
| `POST` | `/api/categories` | `{ "name": "Electronics", "description": "Devices" }` | `201`, danh mục mới |
| `PUT` | `/api/categories/{id}` | `{ "name": "Electronics", "description": "Devices" }` | `200`, danh mục cập nhật |
| `DELETE` | `/api/categories/{id}` | — | `204` |

Response danh mục có `id`, `name`, `description`.

## Sản phẩm

| Method | Endpoint | Body | Thành công |
| --- | --- | --- | --- |
| `GET` | `/api/products` | — | `200`, mảng sản phẩm |
| `GET` | `/api/products/{id}` | — | `200`, sản phẩm |
| `POST` | `/api/products` | ProductRequest | `200`, sản phẩm mới |
| `PUT` | `/api/products/{id}` | ProductRequest | `200`, sản phẩm cập nhật |
| `DELETE` | `/api/products/{id}` | — | `204` |

Ví dụ request tạo/cập nhật:

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

- `name`: bắt buộc, không chỉ gồm khoảng trắng.
- `price`: bắt buộc, tối thiểu `0.01`.
- `stock`: bắt buộc, từ `0` trở lên.
- `categoryId`: bắt buộc, số dương và phải là ID danh mục tồn tại.
- `400 Bad Request`: body sai validation; JSON có `timestamp`, `status`, `error`, `message` và map `fieldErrors`.
- `404 Not Found`: không tìm thấy sản phẩm/danh mục; body `ErrorResponse` có `timestamp`, `status`, `error`, `message`, `path`.
- `204 No Content`: xóa thành công.
- Lỗi ràng buộc khi xóa danh mục đang được sản phẩm sử dụng chưa được chuẩn hóa thành response nghiệp vụ riêng.

## API chưa triển khai

Không có endpoint auth/users, stores/sellers, cart, checkout, orders, payments, shipments hay admin. Các endpoint `/api/v1/...` được đề cập trong tài liệu cũ không thuộc API hiện tại.
