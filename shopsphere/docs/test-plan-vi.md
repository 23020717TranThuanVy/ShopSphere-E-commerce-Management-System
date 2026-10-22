# ShopSphere — Kế hoạch kiểm thử phạm vi hiện tại

> Kế hoạch này giới hạn ở chức năng có trong repository. Không bao gồm kiểm thử checkout, bảo mật JWT hoặc giao vận vì các tính năng đó chưa tồn tại. Cập nhật: 2026-10-23.

## Công nghệ và test hiện có

- Backend dùng JUnit 5, Spring Boot Test/MockMvc và Mockito từ dependency test của Spring Boot.
- Frontend hiện chưa khai báo test runner hoặc script test trong `frontend/package.json`.
- Test context ứng dụng có thể cần SQL Server theo cấu hình runtime; các unit/controller tests mock service/repository.

## Phạm vi cần kiểm thử

### Category API/service

- Liệt kê, đọc theo ID, tạo, cập nhật, xóa danh mục.
- Không tìm thấy ID trả về 404.
- Tên rỗng trả validation 400.
- Xóa danh mục đang được sản phẩm tham chiếu: xác nhận hành vi trên SQL Server và response lỗi; hiện chưa có ánh xạ lỗi rõ ràng.

### Product API/service

- Liệt kê, đọc theo ID, tạo, cập nhật, xóa sản phẩm.
- Tạo/sửa yêu cầu category ID hợp lệ; category không tồn tại trả 404.
- Không tìm thấy product ID trả 404.
- Tên rỗng, giá thiếu/không dương, tồn kho âm/thiếu, categoryId thiếu/không dương trả 400.
- Response bao gồm dữ liệu sản phẩm cùng `categoryId` và `categoryName`.
- Xóa thành công trả 204.

### Frontend smoke test thủ công

1. Khởi động API/SQL Server và frontend.
2. Mở trang Products, xác nhận tải được danh mục và sản phẩm; lỗi API hiển thị thông báo.
3. Tạo danh mục, tạo sản phẩm thuộc danh mục đó, sửa sản phẩm, tìm kiếm/lọc và xóa sản phẩm.
4. Xác nhận giao diện cập nhật các summary sau thao tác và xóa danh mục bị chặn khi còn sản phẩm liên kết.
5. Chạy `npm run build` để kiểm tra TypeScript và tạo bản build Vite.

## Test tự động đã có trong repository

Các lớp test hiện tại:

- `ProductControllerTest`
- `CategoryControllerTest`
- `ProductServiceTest`
- `CategoryServiceTest`
- `GlobalExceptionHandlerTest`
- `ShopsphereApplicationTests`

Tên lớp không đảm bảo toàn bộ hành vi trong kế hoạch đã được kiểm thử. Hãy chạy `.\mvnw.cmd test` trong `shopsphere/` để xác nhận kết quả hiện tại; xem báo cáo trong `target/surefire-reports` nếu thất bại.

## Ngoài phạm vi

Chưa có test auth/role, store ownership, cart/checkout, transaction đơn hàng, payment/webhook, shipping, refund, hiệu năng hay concurrency tồn kho. Bổ sung các nhóm này cùng với việc triển khai chức năng tương ứng.

## Điều kiện hoàn thành thay đổi

- Bổ sung hoặc cập nhật test cho endpoint/service bị ảnh hưởng.
- Kiểm tra status code, response body, validation và trường hợp tài nguyên không tồn tại.
- Chạy bộ test backend liên quan; chạy `npm run build` khi thay đổi frontend.
- Không đưa file `.env*`, `node_modules`, `dist`, log hay dữ liệu database vào commit.
