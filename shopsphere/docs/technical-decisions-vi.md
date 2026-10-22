# ShopSphere — Quyết định kỹ thuật đang áp dụng

> Tài liệu ghi nhận lựa chọn thể hiện trong mã nguồn hiện tại. Chỗ nào là giới hạn hoặc chưa quyết định đều được nêu rõ. Cập nhật: 2026-10-23.

## Tóm tắt stack

| Khu vực | Đang dùng |
| --- | --- |
| Ngôn ngữ backend | Java 17 |
| Backend | Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Jakarta Validation |
| API docs | springdoc OpenAPI UI 2.8.13 |
| Database | Microsoft SQL Server; Compose dùng SQL Server 2022 Express |
| Schema | Hibernate `ddl-auto=update`; chưa có migration tool |
| Frontend | React 19, TypeScript 6, Vite 8 |
| Local deployment | Docker Compose cho API + database; frontend chạy riêng bằng Vite |

## Quyết định có thể quan sát trong repository

### Backend phân tầng

Controllers, services, repositories, entities, DTOs và exception handling nằm trong package riêng. Đây là ứng dụng CRUD nhỏ; chưa có ranh giới module domain kiểu modular monolith hoặc Clean Architecture đầy đủ.

### REST/JSON

Frontend gọi REST API dưới `/api/products` và `/api/categories`. DTO tách request/response khỏi JPA entity. API chưa bật authentication/authorization.

### SQL Server + JPA

SQL Server được dùng trong cấu hình runtime và Compose. JPA/Hibernate ánh xạ `Product` và `Category`. `ddl-auto=update` thuận tiện cho môi trường hiện tại nhưng chưa thay thế được migration có version, review và rollback cho môi trường dùng dữ liệu thật.

### React + TypeScript

Vite phục vụ SPA trong development. Proxy `/api` mặc định tới `http://localhost:8081`, là host port của API Compose. Giao diện hiện tập trung vào CRUD catalog và tính summary ở client.

### Docker Compose

Compose khởi động SQL Server và backend. Mật khẩu được đọc từ `.env.docker`; file ví dụ là `.env.docker.example`. Không đưa credentials vào Git. Compose chỉ dành cho phát triển/local, chưa phải cấu hình triển khai production.

## Chưa được quyết định/triển khai

- Chưa có lựa chọn chính thức về kiến trúc marketplace, phân quyền, tenant hoặc auth/JWT.
- Chưa có quyết định dùng PostgreSQL, Vue, Flyway, Redis hay microservices; các nhắc tới này trong tài liệu cũ không còn là mô tả stack hiện tại.
- Chưa có pipeline production, frontend container, reverse proxy hoặc cấu hình secrets production.
- Chưa chốt chiến lược migration dữ liệu và chính sách backup ngoài Docker volume local.

## Nguyên tắc cập nhật tài liệu

Khi đổi công nghệ hoặc kiến trúc, cập nhật bảng stack cùng README, Docker config và code. Không ghi một đề xuất tương lai thành quyết định đã áp dụng. Khi một roadmap feature bắt đầu, bổ sung ADR và trạng thái triển khai riêng.
