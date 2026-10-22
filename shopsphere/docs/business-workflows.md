# ShopSphere — Quy trình nghiệp vụ hiện tại

> Phiên bản hiện tại có đăng nhập và quản lý catalog. Chưa có luồng marketplace. Cập nhật: 2026-10-23.

## Đăng ký và đăng nhập

1. Frontend lấy CSRF token từ `GET /api/auth/csrf`.
2. Người dùng gửi email, tên hiển thị, mật khẩu tới `POST /api/auth/register`; backend validate, hash password bằng BCrypt và tạo role `CUSTOMER`.
3. Đăng nhập gửi email/mật khẩu tới `POST /api/auth/login`. Backend kiểm tra credentials, đổi session ID và lưu thông tin xác thực vào HTTP session.
4. Browser giữ cookie HttpOnly và tự gửi cookie qua Vite proxy khi gọi API.
5. `/api/auth/me` trả tài khoản và role hiện tại; đăng xuất qua POST logout, gửi CSRF header, rồi backend hủy session.

## Quyền truy cập

- Danh sách/chi tiết sản phẩm và danh mục có thể đọc công khai.
- Chỉ `ADMIN` được tạo, sửa, xóa sản phẩm/danh mục.
- Đăng ký công khai không nhận role từ client, luôn tạo Customer.
- Admin khởi tạo bằng environment. Seller/Shipper chưa được cấp qua ứng dụng.
- CSRF token bắt buộc cho POST/PUT/DELETE; frontend tự lấy token và gửi bằng header do API trả về.

## Quản lý danh mục và sản phẩm

1. Admin tạo danh mục với tên bắt buộc và mô tả tùy chọn.
2. Admin tạo/sửa sản phẩm với tên, giá, mô tả, tồn kho và ID danh mục tồn tại.
3. Backend validate, lưu bằng JPA và trả DTO JSON.
4. Frontend tải catalog, tìm kiếm theo tên/mô tả/danh mục và lọc danh mục trong trình duyệt.
5. Dashboard tính tổng sản phẩm, tổng stock, giá trị inventory `price × stock` và số sản phẩm dưới 10 đơn vị.
6. UI chặn xóa danh mục nếu catalog đã tải còn sản phẩm liên kết; khóa ngoại database vẫn là lớp ràng buộc cuối.

## Chưa có

- Không có seller onboarding/approval hoặc gắn product với store/owner.
- Không có cart, checkout, order, payment, shipment, cancel/refund.
- Không có quên mật khẩu, email verification, MFA, quản lý tài khoản/role UI hoặc giới hạn đăng nhập sai.
- Session hiện lưu trong servlet container; chưa cấu hình kho session dùng chung nhiều instance.
