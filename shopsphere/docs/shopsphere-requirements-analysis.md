# ShopSphere — Phân tích phạm vi hiện tại

> Mô tả phạm vi repository hiện tại và một phần marketplace onboarding. Cập nhật: 2026-10-25.

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

- Đăng ký chung cho `CUSTOMER` và `SELLER`; cả hai chỉ nhập email/mật khẩu. Customer được kích hoạt ngay, Seller chờ Admin duyệt. Đăng nhập/đăng xuất bằng HTTP session phía server.
- Trình duyệt giữ session cookie HttpOnly, SameSite=Lax. Password hash bằng BCrypt.
- Request thay đổi dữ liệu cần CSRF token.
- Admin ban đầu được bootstrap bằng `SHOPSPHERE_ADMIN_EMAIL` và `SHOPSPHERE_ADMIN_PASSWORD`.
- GET danh mục public và sản phẩm ACTIVE public; Admin quản lý toàn bộ catalog, Seller đã duyệt chỉ được thao tác sản phẩm của mình.
- Admin có API và màn hình danh sách tài khoản, duyệt/từ chối hồ sơ Seller đang chờ.
- Role `SHIPPER` chưa có luồng cấp quyền hoặc chức năng; Seller ownership và moderation API đã có nhưng chưa có giao diện Seller/Admin tương ứng.

### Danh mục và sản phẩm

- Danh mục: xem danh sách/chi tiết, tạo, cập nhật, xóa.
- Sản phẩm: xem danh sách/chi tiết, tạo, cập nhật, xóa.
- Sản phẩm có tên, giá, mô tả, URL ảnh tùy chọn, số lượng tồn, một danh mục, Seller sở hữu và trạng thái kiểm duyệt.
- Tên không rỗng, giá lớn hơn 0, tồn kho không âm; danh mục phải tồn tại khi ghi sản phẩm.

### Giao diện

- Đăng nhập/đăng ký Customer, xem/tìm/lọc sản phẩm và xem summary tồn kho.
- Admin tạo/sửa/xóa sản phẩm, tạo/xóa danh mục.
- Backend hỗ trợ cập nhật danh mục nhưng giao diện hiện chưa có nút sửa danh mục.

## Chưa triển khai

Chưa có store profile, giao diện Seller CRUD/Admin duyệt sản phẩm, khóa/mở khóa tài khoản, quản lý Shipper, cart, checkout, order, payment, shipping hoặc khuyến mãi. Customer chỉ đọc được sản phẩm ACTIVE.

## Yêu cầu phát triển tiếp

- Khi thêm role, kiểm tra quyền tại backend; không dựa riêng vào việc ẩn nút trên UI.
- Nếu bổ sung hồ sơ cửa hàng, cần nối store profile với Seller; ownership hiện được kiểm tra trực tiếp theo account ở mọi thao tác sản phẩm.
- Không commit `.env`, `.env.docker`, `node_modules`, `dist`, log hoặc dữ liệu cục bộ.
