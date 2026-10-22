# ShopSphere --- Requirements Analysis

> **Tài liệu:** Phân tích yêu cầu\
> **Phiên bản:** 0.1\
> **Trạng thái:** Bản nháp\
> **Ngày cập nhật:** 01/10/2026

## 1. Tổng quan

### 1.1. Mục đích

ShopSphere là nền tảng thương mại điện tử đa nhà bán hàng
(multi-vendor). Khách hàng có thể mua sản phẩm từ nhiều cửa hàng trong
cùng một lần đặt hàng. Hệ thống quản lý cửa hàng, sản phẩm, đơn hàng,
thanh toán và giao hàng theo từng vai trò.

### 1.2. Mục tiêu

-   Cho phép khách hàng tìm kiếm, lựa chọn và đặt mua sản phẩm trực
    tuyến.
-   Cho phép nhiều Seller quản lý cửa hàng và xử lý đơn hàng của riêng
    họ.
-   Hỗ trợ một đơn hàng tổng chứa các đơn hàng con theo từng Seller.
-   Cho phép Admin quản lý và giám sát hoạt động nền tảng.
-   Hỗ trợ Shipper nhận phân công và cập nhật tiến trình giao hàng.
-   Xây dựng backend dễ kiểm thử, bảo trì và mở rộng.

### 1.3. Phạm vi MVP

MVP tập trung vào tài khoản, cửa hàng, sản phẩm, giỏ hàng, đặt hàng đa
Seller, thanh toán cơ bản hoặc mô phỏng, xử lý đơn hàng và cập nhật giao
hàng.

**Ngoài MVP:** ứng dụng di động riêng, gợi ý sản phẩm bằng AI,
livestream, chương trình khách hàng thân thiết nâng cao và kiến trúc
microservices.

## 2. Vai trò người dùng

  -----------------------------------------------------------------------
  Vai trò                 Mô tả                   Trách nhiệm chính
  ----------------------- ----------------------- -----------------------
  Customer                Người mua               Duyệt sản phẩm, quản lý
                                                  giỏ hàng, đặt hàng,
                                                  thanh toán, theo dõi
                                                  đơn

  Seller                  Nhà bán hàng            Quản lý cửa hàng, sản
                                                  phẩm, tồn kho và đơn
                                                  hàng của mình

  Admin                   Quản trị nền tảng       Quản lý người dùng,
                                                  Seller, danh mục và
                                                  giám sát hệ thống

  Shipper                 Nhân viên giao hàng     Xem đơn được phân công
                                                  và cập nhật trạng thái
                                                  giao hàng
  -----------------------------------------------------------------------

## 3. Yêu cầu chức năng

### 3.1. Customer

-   [ ] Đăng ký, đăng nhập, đăng xuất.
-   [ ] Quản lý hồ sơ và địa chỉ nhận hàng.
-   [ ] Duyệt, tìm kiếm, lọc và xem chi tiết sản phẩm.
-   [ ] Thêm, sửa số lượng, xóa sản phẩm trong giỏ hàng.
-   [ ] Đặt hàng với sản phẩm từ một hoặc nhiều cửa hàng.
-   [ ] Chọn địa chỉ và phương thức thanh toán được hỗ trợ.
-   [ ] Xem lịch sử, chi tiết và trạng thái đơn hàng.
-   [ ] Gửi yêu cầu hủy/hoàn trả theo chính sách.

### 3.2. Seller

-   [ ] Gửi yêu cầu mở cửa hàng và cập nhật thông tin cửa hàng.
-   [ ] Tạo, sửa, ẩn/hiện và quản lý sản phẩm.
-   [ ] Quản lý giá và tồn kho.
-   [ ] Xem và xử lý đơn hàng thuộc cửa hàng của mình.
-   [ ] Cập nhật trạng thái xử lý đơn trong phạm vi được phép.
-   [ ] Xem doanh thu cơ bản.

### 3.3. Admin

-   [ ] Đăng nhập khu vực quản trị.
-   [ ] Quản lý tài khoản người dùng.
-   [ ] Duyệt, kích hoạt hoặc tạm khóa Seller/cửa hàng.
-   [ ] Quản lý danh mục sản phẩm.
-   [ ] Giám sát đơn hàng, thanh toán và giao hàng.
-   [ ] Xử lý báo cáo/khiếu nại cơ bản.
-   [ ] Xem thống kê tổng quan.

### 3.4. Shipper

-   [ ] Đăng nhập và xem đơn được phân công.
-   [ ] Xem thông tin cần thiết để giao hàng.
-   [ ] Cập nhật trạng thái: đã nhận, đang giao, giao thành công hoặc
    thất bại.
-   [ ] Ghi chú lý do giao thất bại.

## 4. Quy tắc nghiệp vụ

1.  Mỗi sản phẩm thuộc về một cửa hàng cụ thể.
2.  Một giỏ hàng có thể chứa sản phẩm từ nhiều cửa hàng.
3.  Khi khách đặt hàng đa cửa hàng, hệ thống tạo một **Parent Order** và
    các **Seller Orders** tương ứng.
4.  Seller chỉ được xem và xử lý đơn hàng thuộc cửa hàng của mình.
5.  Tồn kho phải được kiểm tra khi checkout; cần tránh bán vượt số lượng
    khả dụng.
6.  Trạng thái đơn hàng, thanh toán và giao hàng được quản lý riêng
    biệt.
7.  Một Parent Order có thể có nhiều Shipment nếu hàng đến từ nhiều
    Seller.
8.  Callback thanh toán lặp không được ghi nhận giao dịch hai lần.
9.  Các thao tác quan trọng phải được xác thực và kiểm tra quyền truy
    cập.

Các quy tắc về phí nền tảng, chia tiền, thời hạn xác nhận, hủy đơn và
hoàn tiền cần được chốt trước khi triển khai.

## 5. Quy trình đặt hàng đa Seller

1.  Customer tìm kiếm sản phẩm và thêm vào giỏ hàng.
2.  Hệ thống kiểm tra sản phẩm còn bán và đủ tồn kho.
3.  Customer chọn địa chỉ nhận hàng và phương thức thanh toán.
4.  Hệ thống tạo Parent Order, sau đó tách thành Seller Orders theo cửa
    hàng.
5.  Hệ thống khởi tạo thanh toán hoặc ghi nhận phương thức COD nếu được
    hỗ trợ.
6.  Sau khi thanh toán hợp lệ (nếu trả trước), Seller xử lý đơn hàng.
7.  Hệ thống tạo hoặc ghi nhận Shipment tương ứng.
8.  Shipper được phân công và cập nhật tiến trình giao hàng.
9.  Hệ thống tổng hợp trạng thái các Seller Orders thành trạng thái
    Parent Order.
10. Customer theo dõi kết quả và lịch sử đơn hàng.

### Tình huống ngoại lệ cần xử lý

-   Sản phẩm hết hàng hoặc tồn kho thay đổi trong lúc checkout.
-   Thanh toán thất bại, bị hủy hoặc callback gửi lặp.
-   Seller từ chối hoặc không xử lý được đơn.
-   Khách hủy đơn ở các giai đoạn khác nhau.
-   Một phần đơn giao thành công, phần còn lại thất bại.
-   Shipper không liên hệ được người nhận.
-   Hoàn tiền một phần hoặc toàn bộ.

## 6. Yêu cầu phi chức năng

  -----------------------------------------------------------------------
  Nhóm                                Yêu cầu ban đầu
  ----------------------------------- -----------------------------------
  Bảo mật                             Xác thực, phân quyền theo vai trò
                                      và quyền sở hữu dữ liệu

  Toàn vẹn dữ liệu                    Transaction và ràng buộc phù hợp
                                      cho đặt hàng, tồn kho, thanh toán

  Hiệu năng                           Phân trang, tối ưu truy vấn; xác
                                      định mục tiêu đo lường sau

  Bảo trì                             Module theo nghiệp vụ, quy ước code
                                      và xử lý lỗi thống nhất

  Kiểm thử                            Unit test cho logic nghiệp vụ,
                                      integration test cho API quan trọng

  Quan sát                            Logging có cấu trúc, tránh ghi dữ
                                      liệu bí mật

  Triển khai                          Cấu hình theo môi trường, Docker
                                      Compose cho phát triển
  -----------------------------------------------------------------------

## 7. Phân chia tính năng

  -----------------------------------------------------------------------
  Giai đoạn                           Tính năng
  ----------------------------------- -----------------------------------
  MVP                                 Tài khoản, phân quyền, cửa hàng,
                                      danh mục, sản phẩm, giỏ hàng, đặt
                                      hàng đa Seller, quản lý đơn, giao
                                      hàng cơ bản

  Sau MVP                             Cổng thanh toán thực, hoàn tiền
                                      nâng cao, mã giảm giá, đánh giá,
                                      thông báo thời gian thực

  Mở rộng                             Gợi ý sản phẩm, phân tích dữ liệu,
                                      loyalty, tối ưu vận chuyển
  -----------------------------------------------------------------------

## 8. Câu hỏi nghiệp vụ cần xác nhận

-   [ ] Một Seller có thể sở hữu nhiều cửa hàng không?
-   [ ] Khách có thể đặt hàng khi chưa đăng nhập không?
-   [ ] MVP hỗ trợ COD, thanh toán trực tuyến hay cả hai?
-   [ ] Phí vận chuyển tính theo từng Seller Order hay Parent Order?
-   [ ] Ai phân công Shipper: Admin, hệ thống hay Seller?
-   [ ] Seller có được tự hủy đơn sau khi xác nhận không?
-   [ ] Hủy/đổi trả/hoàn tiền áp dụng theo đơn tổng hay từng đơn con?
-   [ ] Nền tảng có thu hoa hồng từ Seller trong MVP không?

## 9. Tiêu chí nghiệm thu MVP

-   Customer đăng ký, đăng nhập và duyệt sản phẩm được.
-   Customer đặt được giỏ hàng có sản phẩm từ ít nhất hai Seller.
-   Hệ thống tạo đúng Parent Order và Seller Orders.
-   Seller chỉ truy cập và cập nhật đơn thuộc cửa hàng của mình.
-   Hệ thống kiểm tra tồn kho và ngăn đặt vượt số lượng khả dụng.
-   Admin quản lý được tài khoản, Seller và danh mục.
-   Shipper chỉ xem đơn được phân công và cập nhật trạng thái hợp lệ.
-   API quan trọng có kiểm thử và tài liệu request/response.
-   Các lỗi chính được xử lý với thông báo nhất quán.

## 10. Tài liệu liên quan

-   `../02-business/business-workflows.md`
-   `../03-database/database-design.md`
-   `../04-architecture/system-architecture.md`
-   `../04-architecture/technical-decisions.md`
-   `../05-api/api-specification.md`
-   `../06-testing/test-plan.md`

------------------------------------------------------------------------

**Trạng thái:** Bản nháp để thảo luận. Hãy xác nhận các câu hỏi ở mục 8
trước khi chốt thiết kế database và API.
