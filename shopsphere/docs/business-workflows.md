# ShopSphere — Quy trình nghiệp vụ hiện tại

> Phạm vi hiện tại gồm đăng nhập, catalog, onboarding Seller và API ownership/moderation sản phẩm. Chưa có cửa hàng hoặc đặt hàng. Cập nhật: 2026-10-25.

## Đăng ký và đăng nhập

1. Frontend lấy CSRF token từ `GET /api/auth/csrf`.
2. Người đăng ký chọn Customer hoặc Seller trên cùng form. Customer gửi thông tin cơ bản; backend hash password bằng BCrypt, tạo tài khoản `ACTIVE` và đăng nhập ngay.
3. Seller chỉ gửi email và mật khẩu. Backend chỉ chấp nhận role `SELLER` từ allowlist Customer/Seller, hash password, lưu hồ sơ `PENDING` và không tạo session. Tên hiển thị tạm lấy từ phần email trước `@`; Seller phải được Admin duyệt mới đăng nhập được. Thông tin cửa hàng bổ sung ở bước onboarding sau.
4. Đăng nhập gửi email/mật khẩu tới `POST /api/auth/login`. Backend kiểm tra credentials, đổi session ID và lưu thông tin xác thực vào HTTP session. Tài khoản Seller chờ duyệt/từ chối không thể đăng nhập.
5. Browser giữ cookie HttpOnly và tự gửi cookie qua Vite proxy khi gọi API.
6. `/api/auth/me` trả tài khoản, role và trạng thái hiện tại; đăng xuất qua POST logout, gửi CSRF header, rồi backend hủy session.

## Quyền truy cập

- Danh mục có thể đọc công khai; public/Customer chỉ đọc sản phẩm `ACTIVE`.
- `ADMIN` quản lý mọi sản phẩm/danh mục; Seller đã duyệt chỉ quản lý sản phẩm thuộc mình. Seller không được ghi danh mục.
- Đăng ký công khai cho phép chọn Customer hoặc Seller; Admin/shipper không thể tự cấp role.
- Admin khởi tạo bằng environment. Admin xem danh sách tài khoản và duyệt/từ chối hồ sơ Seller đang `PENDING`.
- CSRF token bắt buộc cho POST/PUT/DELETE; frontend tự lấy token và gửi bằng header do API trả về.

## Quản lý danh mục và sản phẩm

1. Admin tạo danh mục với tên bắt buộc và mô tả tùy chọn.
2. Admin tạo sản phẩm ACTIVE hoặc Seller đã được duyệt tạo sản phẩm PENDING_REVIEW với tên, giá, mô tả, ảnh tùy chọn, tồn kho và danh mục tồn tại.
3. Backend validate, lưu bằng JPA cùng chủ sở hữu Seller (nếu có), trạng thái kiểm duyệt và trả DTO JSON.
4. Frontend tải catalog, tìm kiếm theo tên/mô tả/danh mục và lọc danh mục trong trình duyệt.
5. Dashboard tính tổng sản phẩm, tổng stock, giá trị inventory `price × stock` và số sản phẩm dưới 10 đơn vị.
6. UI chặn xóa danh mục nếu catalog đã tải còn sản phẩm liên kết; khóa ngoại database vẫn là lớp ràng buộc cuối.
7. Seller chỉ sửa/xóa sản phẩm của mình; mỗi lần sửa đưa sản phẩm về PENDING_REVIEW. Admin duyệt thành ACTIVE, từ chối thành REJECTED hoặc ẩn bằng SUSPENDED. Catalog public chỉ hiển thị ACTIVE.

## Chưa có

- Chưa có store profile riêng, giao diện Admin duyệt sản phẩm, cart/checkout/order/payment/shipment hoặc khóa/mở khóa tài khoản.
- Không có cart, checkout, order, payment, shipment, cancel/refund.
- Không có quên mật khẩu, email verification, MFA, quản lý tài khoản/role UI hoặc giới hạn đăng nhập sai.
- Session hiện lưu trong servlet container; chưa cấu hình kho session dùng chung nhiều instance.
