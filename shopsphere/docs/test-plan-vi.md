# ShopSphere — Kế hoạch kiểm thử hiện tại

> Phạm vi: auth session, quyền truy cập và CRUD catalog đã có trong repository. Cập nhật: 2026-10-23.

## Công nghệ kiểm thử

- Backend: JUnit 5, Spring Boot Test/MockMvc, Mockito.
- Frontend: chưa khai báo test runner; `npm run build` kiểm tra TypeScript và build Vite.
- `ShopsphereApplicationTests` khởi động Spring context và cần SQL Server theo cấu hình runtime.

## Authentication/session — kiểm tra cần có

- CSRF endpoint tạo token; POST không có token bị từ chối.
- Đăng ký hợp lệ tạo CUSTOMER, hash password, cấp session cookie; request không thể chọn role khác.
- Email/password không hợp lệ bị validate; email trùng trả 409.
- Đăng nhập đúng trả tài khoản/session; sai credentials trả 401.
- `/api/auth/me` trả 401 khi anonymous và tài khoản khi đã đăng nhập.
- Logout yêu cầu CSRF, hủy session; session cũ không còn truy cập nội dung cần đăng nhập.
- Password lưu trong DB là BCrypt hash, không phải giá trị thô.

## Authorization/catalog — kiểm tra cần có

- GET products/categories hoạt động khi chưa đăng nhập.
- Customer đăng nhập vẫn chỉ đọc catalog; POST/PUT/DELETE catalog trả 403.
- Admin được phép tạo/sửa/xóa catalog.
- Admin bootstrap tạo tài khoản khi chưa có; restart không tạo bản trùng và không ghi password vào log.
- Validation product/category và 404 cho ID không tồn tại.
- Tạo/sửa product cần category ID hợp lệ; response có categoryId/categoryName.
- Xóa product thành công trả 204; xóa category còn product được kiểm tra hành vi khóa ngoại.

## Frontend smoke test thủ công

1. Khởi động API/SQL Server và Vite; trang đăng nhập hiển thị.
2. Đăng ký Customer, tải dashboard và kiểm tra chế độ chỉ đọc.
3. Đăng xuất/đăng nhập lại; kiểm tra session còn hiệu lực khi refresh trang.
4. Đăng nhập bằng Admin bootstrap; tạo danh mục/sản phẩm, sửa sản phẩm, tìm/lọc, xóa sản phẩm.
5. Kiểm tra CSRF header được gửi cho thao tác ghi, lỗi API hiển thị rõ và dashboard cập nhật summary.
6. Chạy `npm run build` trong `shopsphere/frontend/`.

## Automated tests hiện có

- `ProductControllerTest`
- `CategoryControllerTest`
- `ProductServiceTest`
- `CategoryServiceTest`
- `GlobalExceptionHandlerTest`
- `ShopsphereApplicationTests`

Các lớp auth/security test chưa được bổ sung. Hãy chạy `.\mvnw.cmd test` trong `shopsphere/` để kiểm tra bộ hiện có; xem `target/surefire-reports` khi thất bại.

## Ngoài phạm vi

Chưa kiểm thử Seller/store ownership, Shipper, cart/checkout/order, payment/webhook, shipping/refund, hiệu năng hay concurrency tồn kho vì các tính năng đó chưa tồn tại.
