# ShopSphere — Quyết định kỹ thuật đang áp dụng

> Ghi nhận stack và cách làm thể hiện trong mã nguồn. Cập nhật: 2026-10-23.

## Stack hiện tại

| Khu vực | Đang dùng |
| --- | --- |
| Backend language | Java 17 |
| Backend | Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Spring Security, Jakarta Validation |
| API docs | springdoc OpenAPI UI 2.8.13 |
| Database | Microsoft SQL Server; Compose dùng SQL Server 2022 Express |
| Schema | Hibernate `ddl-auto=update`; chưa có migration tool |
| Frontend | React 19, TypeScript 6, Vite 8 |
| Authentication | Server-side HTTP session, BCrypt password hashing |
| Browser protection | HttpOnly SameSite=Lax cookie, session-backed CSRF token |
| Local deployment | Compose cho API + database; frontend chạy riêng bằng Vite |

## Quyết định đang áp dụng

### Phân tầng backend

Controller, service, repository, entity, DTO, config và exception handling được tách package. Đây là ứng dụng CRUD nhỏ; chưa chia module domain kiểu modular monolith hoặc Clean Architecture đầy đủ.

### REST/JSON

Frontend gọi `/api/auth`, `/api/products`, `/api/categories`. Request/response DTO tách khỏi entity. Catalog GET public; ghi catalog yêu cầu ADMIN.

### Session cookie thay JWT

Frontend và API được gọi qua cùng origin trong Vite proxy, phù hợp dùng session server-side. Backend lưu SecurityContext trong HTTP session; browser gửi cookie HttpOnly tự động. CSRF protection bật cho request thay đổi dữ liệu. Không lưu access token trong localStorage.

Session hiện dùng servlet container mặc định, thích hợp local/single-instance. Khi chạy nhiều backend instance cần session store dùng chung và sticky sessions hoặc Spring Session.

### Password và account provisioning

Password hash bằng BCrypt. Public registration chỉ cấp CUSTOMER. Admin ban đầu bootstrap từ `SHOPSPHERE_ADMIN_EMAIL`/`SHOPSPHERE_ADMIN_PASSWORD`; role không thể chọn trong request đăng ký. Seller/Shipper chưa có quy trình cấp.

### SQL Server + JPA

SQL Server dùng cho runtime và Compose. Hibernate `ddl-auto=update` phù hợp local nhưng chưa cung cấp migration có version, review, rollback.

### React + Docker Compose

Vite proxy `/api` tới host `localhost:8081`. Compose chạy API và SQL Server; `.env.docker` giữ thông tin local, không commit. Đây chưa phải cấu hình production.

## Giới hạn cần xử lý trước production

- Bật HTTPS và `SESSION_COOKIE_SECURE=true`; quản lý secrets ngoài repo.
- Cân nhắc rate limiting đăng nhập, email verification, khôi phục password và audit log.
- Thêm store ownership trước khi cấp quyền quản lý catalog cho Seller.
- Chuyển từ Hibernate schema auto-update sang migration có kiểm soát trước khi quản lý dữ liệu production.
- Bổ sung session store chia sẻ nếu scale nhiều instance.

## Chưa quyết định

PostgreSQL, Vue, Flyway, Redis, microservices và JWT không phải stack hiện tại. Chỉ xem xét lại theo yêu cầu sản phẩm/deployment cụ thể, không coi các đề xuất cũ là quyết định đã áp dụng.
