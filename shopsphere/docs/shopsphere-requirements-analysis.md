# ShopSphere — Phân tích phạm vi hiện tại

> Mô tả phiên bản đang có trong repository, không phải đặc tả marketplace tương lai. Cập nhật: 2026-10-23.

## Mục tiêu

ShopSphere là ứng dụng web quản lý danh mục sản phẩm và tồn kho cơ bản. Người dùng xem catalog; Admin quản lý sản phẩm/danh mục.

## Công nghệ

- Backend: Java 17, Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Spring Security, Jakarta Validation.
- Database: Microsoft SQL Server; Hibernate `ddl-auto=update` cập nhật schema khi ứng dụng khởi động.
- Frontend: React 19, TypeScript 6, Vite 8.
- Local deployment: Docker Compose chạy API và SQL Server Express. Host API port `8081`; chạy Maven trực tiếp mặc định dùng `8080`.
- API docs: springdoc OpenAPI / Swagger UI.

## Chức năng đã triển khai

### Tài khoản và phân quyền

- Đăng ký công khai tạo tài khoản `CUSTOMER`; đăng nhập và đăng xuất bằng HTTP session phía server.
- Trình duyệt giữ session cookie HttpOnly, SameSite=Lax. Password hash bằng BCrypt.
- Request thay đổi dữ liệu cần CSRF token.
- Admin ban đầu được bootstrap bằng `SHOPSPHERE_ADMIN_EMAIL` và `SHOPSPHERE_ADMIN_PASSWORD`.
- GET danh mục/sản phẩm public; thao tác ghi yêu cầu role `ADMIN`.
- Các role `SELLER` và `SHIPPER` đã có trong enum nhưng chưa có luồng tạo/cấp quyền.

### Danh mục và sản phẩm

- Danh mục: xem danh sách/chi tiết, tạo, cập nhật, xóa.
- Sản phẩm: xem danh sách/chi tiết, tạo, cập nhật, xóa.
- Sản phẩm có tên, giá, mô tả, số lượng tồn và một danh mục.
- Tên không rỗng, giá lớn hơn 0, tồn kho không âm; danh mục phải tồn tại khi ghi sản phẩm.

### Giao diện

- Đăng nhập/đăng ký Customer, xem/tìm/lọc sản phẩm và xem summary tồn kho.
- Admin tạo/sửa/xóa sản phẩm, tạo/xóa danh mục.
- Backend hỗ trợ cập nhật danh mục nhưng giao diện hiện chưa có nút sửa danh mục.

## Chưa triển khai

Chưa có store/chủ sở hữu sản phẩm, duyệt Seller, quản lý Shipper, cart, checkout, order, payment, shipping, khuyến mãi hoặc quản trị marketplace. Customer có thể đọc catalog nhưng không được thay đổi dữ liệu.

## Yêu cầu phát triển tiếp

- Khi thêm role, kiểm tra quyền tại backend; không dựa riêng vào việc ẩn nút trên UI.
- Trước khi cho Seller quản lý sản phẩm, cần bổ sung store và kiểm tra ownership ở mọi thao tác.
- Không commit `.env`, `.env.docker`, `node_modules`, `dist`, log hoặc dữ liệu cục bộ.
