# ShopSphere — Phân tích phạm vi hiện tại

> Tài liệu này mô tả phiên bản đang có trong repository, không phải đặc tả marketplace tương lai. Cập nhật lần cuối: 2026-10-23.

## Mục tiêu

ShopSphere hiện là ứng dụng quản lý danh mục sản phẩm và tồn kho cơ bản. Nhân viên có thể tạo và tổ chức danh mục, quản lý sản phẩm, giá và số lượng tồn qua giao diện web hoặc REST API.

## Công nghệ đang dùng

- Backend: Java 17, Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Jakarta Validation.
- Database: Microsoft SQL Server; Hibernate `ddl-auto=update` cập nhật schema khi ứng dụng khởi động.
- Frontend: React 19, TypeScript 6, Vite 8.
- Local services: Docker Compose chạy API và SQL Server Express. Host API port là `8081`; chạy Maven trực tiếp mặc định dùng `8080`.
- API docs: springdoc OpenAPI / Swagger UI.

## Chức năng đã triển khai

### Danh mục

- Liệt kê, xem theo ID, tạo, cập nhật và xóa danh mục.
- Tên danh mục bắt buộc không rỗng; mô tả là tùy chọn.

### Sản phẩm

- Liệt kê, xem theo ID, tạo, cập nhật và xóa sản phẩm.
- Mỗi sản phẩm có tên, giá, mô tả, số lượng tồn và tham chiếu đến một danh mục.
- Khi tạo hoặc cập nhật, danh mục phải tồn tại. Tên không rỗng, giá lớn hơn 0, tồn kho không âm.

### Giao diện web

- Xem, tìm kiếm và lọc sản phẩm theo danh mục.
- Xem tổng số sản phẩm, số lượng tồn, giá trị tồn kho và số sản phẩm có tồn kho dưới 10.
- Tạo, sửa, xóa sản phẩm; tạo và xóa danh mục.
- Giao diện không có đăng nhập; API hiện cũng chưa cấu hình xác thực/phân quyền.

## Ngoài phạm vi phiên bản hiện tại

Repository chưa triển khai tài khoản/role, seller/store, giỏ hàng, checkout, đơn hàng, thanh toán, giao vận, khuyến mãi hay quản trị marketplace. Các luồng này không nên được hiểu là tính năng dùng được chỉ vì từng xuất hiện trong bản tài liệu cũ.

## Yêu cầu khi phát triển tiếp

- Giữ API và giao diện đồng bộ khi thay đổi DTO hoặc endpoint.
- Không commit `.env`, `.env.docker`, `node_modules`, `dist` hoặc dữ liệu cục bộ.
- Nếu bổ sung xác thực hoặc nghiệp vụ marketplace, cần cập nhật tài liệu thiết kế, schema, API và test cùng thay đổi mã nguồn.
