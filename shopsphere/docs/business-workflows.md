# ShopSphere — Quy trình nghiệp vụ hiện tại

> Phiên bản hiện tại tập trung vào quản lý danh mục, sản phẩm và tồn kho. Chưa có quy trình đăng nhập, đặt hàng hoặc thanh toán. Cập nhật: 2026-10-23.

## Đối tượng sử dụng

Giao diện hiện là dashboard quản lý catalog, không phân biệt vai trò người dùng. Backend chưa xác thực danh tính hoặc phân quyền; chỉ nên dùng trong môi trường phát triển đáng tin cậy.

## Khởi tạo dữ liệu

1. Nhân viên tạo một danh mục với tên bắt buộc và mô tả tùy chọn.
2. Backend lưu danh mục vào SQL Server và trả về ID.
3. Sản phẩm mới cần tham chiếu đến ID danh mục đã tồn tại.

## Quản lý sản phẩm

1. Giao diện tải danh sách sản phẩm và danh mục từ `GET /api/products` và `GET /api/categories`.
2. Người dùng có thể lọc danh sách theo danh mục hoặc tìm theo tên, tên danh mục và mô tả. Tìm kiếm/lọc diễn ra trong trình duyệt trên dữ liệu đã tải.
3. Khi tạo hoặc sửa, người dùng gửi tên, giá, mô tả, tồn kho và danh mục. Backend validate dữ liệu, xác nhận danh mục tồn tại và lưu sản phẩm.
4. Xóa sản phẩm gọi endpoint DELETE; giao diện yêu cầu xác nhận trước khi gửi.
5. Các thẻ tổng quan tính số sản phẩm, tổng tồn, tổng giá trị `price × stock` và số sản phẩm có tồn dưới 10 ở frontend.

## Quản lý danh mục

1. Người dùng tạo danh mục từ trang Categories.
2. Giao diện ngăn xóa danh mục nếu danh sách sản phẩm đã tải có sản phẩm thuộc danh mục đó.
3. Backend vẫn dựa trên khóa ngoại SQL Server khi xóa; lỗi vi phạm ràng buộc chưa có thông báo API thân thiện riêng.
4. Backend hỗ trợ PUT cập nhật danh mục, dù giao diện hiện chưa có thao tác sửa.

## Giới hạn và quy trình chưa có

- Chưa có đăng ký/đăng nhập, chủ cửa hàng hoặc nhiều tenant.
- Tồn kho chỉ là một giá trị số trên sản phẩm; chưa có lịch sử nhập/xuất hoặc giao dịch giữ hàng.
- Chưa có giỏ hàng, checkout, order, payment, shipping, cancel hay refund.
- Chưa có kiểm soát cạnh tranh tồn kho hoặc audit trail.

Không sử dụng tài liệu này như mô tả quy trình marketplace cho tới khi những luồng đó được xây dựng và kiểm thử.
