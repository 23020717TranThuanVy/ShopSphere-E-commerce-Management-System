# ShopSphere --- Business Workflows

> **Tài liệu:** Quy trình nghiệp vụ\
> **Phiên bản:** 0.1\
> **Trạng thái:** Bản nháp\
> **Ngày cập nhật:** 01/10/2026\
> **Liên quan:** `../01-requirements/requirements-analysis.md`

## 1. Mục đích

Tài liệu mô tả cách các vai trò tương tác với ShopSphere và cách hệ
thống xử lý các quy trình nghiệp vụ chính. Đây là cơ sở để thiết kế
database, API, phân quyền và kiểm thử.

## 2. Vai trò tham gia

  -----------------------------------------------------------------------
  Vai trò                             Trách nhiệm trong quy trình
  ----------------------------------- -----------------------------------
  Customer                            Tìm sản phẩm, quản lý giỏ hàng, đặt
                                      hàng, thanh toán và theo dõi

  Seller                              Quản lý cửa hàng/sản phẩm, xác nhận
                                      và chuẩn bị đơn của cửa hàng

  Admin                               Duyệt Seller, quản lý danh mục và
                                      hỗ trợ xử lý sự cố

  Shipper                             Nhận phân công và cập nhật kết quả
                                      giao hàng

  System                              Kiểm tra dữ liệu, quản lý tồn kho,
                                      tạo đơn, ghi nhận trạng thái và gửi
                                      thông báo
  -----------------------------------------------------------------------

## 3. Quy trình đăng ký và xác thực

### 3.1. Customer đăng ký

1.  Customer nhập thông tin đăng ký.
2.  System kiểm tra dữ liệu bắt buộc và tính duy nhất của thông tin định
    danh (ví dụ email).
3.  System tạo tài khoản với vai trò Customer.
4.  System xác nhận đăng ký theo cơ chế được cấu hình.
5.  Customer đăng nhập và truy cập các chức năng được cấp quyền.

**Ngoại lệ:** dữ liệu không hợp lệ, email đã tồn tại hoặc xác minh thất
bại. Hệ thống trả lỗi phù hợp và không tạo tài khoản trùng.

### 3.2. Đăng nhập

1.  Người dùng gửi thông tin đăng nhập.
2.  System xác thực thông tin.
3.  System kiểm tra tài khoản có hoạt động hay bị khóa.
4.  Nếu hợp lệ, System cấp thông tin xác thực phiên (ví dụ access
    token).
5.  Các API được bảo vệ kiểm tra token, vai trò và quyền sở hữu tài
    nguyên.

**Quy tắc:** không dựa riêng vào giao diện để phân quyền; backend phải
kiểm tra quyền ở mọi thao tác nhạy cảm.

## 4. Quy trình Seller đăng ký và quản lý cửa hàng

### 4.1. Đăng ký Seller/cửa hàng

1.  Người dùng gửi yêu cầu trở thành Seller cùng thông tin cửa hàng.
2.  System kiểm tra dữ liệu và lưu yêu cầu ở trạng thái chờ duyệt.
3.  Admin xem thông tin và quyết định duyệt hoặc từ chối.
4.  Nếu được duyệt, cửa hàng được kích hoạt và Seller có thể quản lý cửa
    hàng.
5.  Nếu bị từ chối, System lưu lý do và thông báo cho người gửi.

**Trạng thái gợi ý:** `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`.

### 4.2. Seller đăng sản phẩm

1.  Seller mở trang quản lý sản phẩm.
2.  Seller nhập tên, mô tả, giá, danh mục, hình ảnh và số lượng tồn kho.
3.  System xác thực dữ liệu, kiểm tra quyền sở hữu cửa hàng và tính hợp
    lệ của danh mục.
4.  System lưu sản phẩm gắn với cửa hàng của Seller.
5.  Sản phẩm được hiển thị theo trạng thái duyệt/hoạt động của nền tảng.

**Ngoại lệ:** cửa hàng chưa được duyệt hoặc bị khóa; giá/số lượng không
hợp lệ; danh mục không tồn tại.

## 5. Quy trình duyệt và tìm kiếm sản phẩm

1.  Customer truy cập danh mục hoặc trang tìm kiếm.
2.  System chỉ trả về sản phẩm đang được phép hiển thị và thuộc cửa hàng
    đủ điều kiện hoạt động.
3.  Customer tìm kiếm hoặc lọc theo từ khóa, danh mục, khoảng giá và các
    tiêu chí MVP.
4.  Customer mở trang chi tiết sản phẩm.
5.  System trả thông tin sản phẩm, cửa hàng, giá và tình trạng tồn kho
    hiện tại.

**Quy tắc:** thông tin tồn kho hiển thị chỉ mang tính tham khảo; tồn kho
phải được kiểm tra lại khi checkout.

## 6. Quy trình giỏ hàng

1.  Customer chọn sản phẩm và số lượng.
2.  System xác thực sản phẩm còn hoạt động, số lượng hợp lệ và cửa hàng
    có thể bán.
3.  System thêm hoặc cập nhật dòng hàng trong giỏ.
4.  Customer thay đổi số lượng hoặc xóa sản phẩm khi cần.
5.  System tính lại tạm tính giỏ hàng.

**Quy tắc:** giá và tồn kho trong giỏ không phải cam kết cuối cùng. Giá,
khả năng bán và tồn kho cần được xác nhận lại tại thời điểm đặt hàng.

## 7. Quy trình checkout và tạo đơn đa Seller

### 7.1. Luồng chính

1.  Customer bắt đầu checkout từ giỏ hàng.
2.  System tải lại thông tin sản phẩm, giá, trạng thái bán và tồn kho.
3.  System kiểm tra địa chỉ nhận hàng và phương thức thanh toán.
4.  System nhóm các dòng hàng theo cửa hàng/Seller.
5.  System tính tạm tính từng nhóm, phí vận chuyển và tổng tiền theo
    chính sách đã cấu hình.
6.  Customer xác nhận đặt hàng.
7.  System thực hiện thao tác tạo đơn trong transaction phù hợp:
    -   Tạo một **Parent Order** đại diện cho lần checkout.
    -   Tạo một **Seller Order** cho mỗi cửa hàng có hàng trong giỏ.
    -   Tạo các **Order Items** thuộc Seller Order tương ứng.
    -   Ghi nhận giá, số lượng, địa chỉ và các thông tin cần thiết tại
        thời điểm đặt hàng.
    -   Giữ hoặc trừ tồn kho theo chiến lược tồn kho đã chọn.
8.  System khởi tạo thanh toán hoặc ghi nhận lựa chọn COD nếu được hỗ
    trợ.
9.  System trả mã đơn và trạng thái ban đầu cho Customer.

### 7.2. Minh họa

Customer có giỏ hàng gồm: - 2 sản phẩm từ Store A. - 1 sản phẩm từ Store
B. - 3 sản phẩm từ Store C.

Khi xác nhận, hệ thống tạo một Parent Order và ba Seller Orders. Mỗi
Seller chỉ nhìn thấy Seller Order của cửa hàng mình; Customer có thể
theo dõi toàn bộ đơn tổng.

### 7.3. Nguyên tắc dữ liệu và giao dịch

-   Không tạo đơn một phần nếu bước tạo đơn thất bại giữa chừng.
-   Không tin giá hoặc tổng tiền do frontend gửi lên; backend tự tính từ
    dữ liệu hợp lệ.
-   Tránh trừ tồn kho hai lần khi người dùng gửi lại yêu cầu.
-   Cân nhắc idempotency key cho thao tác tạo đơn.
-   Lưu snapshot giá và thông tin hàng hóa cần thiết để lịch sử đơn
    không thay đổi khi Seller sửa sản phẩm.

## 8. Quy trình thanh toán

### 8.1. Thanh toán trực tuyến

1.  System tạo giao dịch thanh toán gắn với đơn hàng.
2.  System chuyển Customer đến cổng thanh toán hoặc khởi tạo phương thức
    thanh toán tích hợp.
3.  Customer thực hiện thanh toán tại nhà cung cấp.
4.  Nhà cung cấp gửi kết quả qua callback/webhook hoặc Customer quay lại
    trang kết quả.
5.  Backend xác minh kết quả theo cơ chế bảo mật của nhà cung cấp.
6.  System cập nhật giao dịch và trạng thái thanh toán một cách
    idempotent.
7.  Nếu thanh toán thành công, đơn được chuyển sang bước xử lý tiếp
    theo.
8.  Nếu thất bại hoặc hết hạn, đơn được giữ, hủy hoặc giải phóng tồn kho
    theo chính sách đã chốt.

### 8.2. Thanh toán khi nhận hàng (COD)

1.  Customer chọn COD nếu phương thức này được bật.
2.  System ghi nhận phương thức thanh toán và trạng thái chưa thu tiền.
3.  Đơn được xử lý và giao hàng.
4.  Khi giao hàng thành công, hệ thống ghi nhận kết quả thu tiền theo
    quy trình vận hành.
5.  Đơn và giao dịch được cập nhật theo quy tắc đối soát.

### 8.3. Kiểm soát quan trọng

-   Chỉ backend được quyền xác nhận kết quả thanh toán.
-   Không coi trang redirect của trình duyệt là bằng chứng thanh toán
    duy nhất.
-   Xác minh chữ ký, mã giao dịch, số tiền và đơn hàng từ thông báo của
    nhà cung cấp.
-   Callback lặp phải trả kết quả an toàn, không tạo giao dịch hoặc ghi
    nhận thanh toán trùng.
-   Quy tắc phân bổ tiền giữa nền tảng và Seller cần được xác định riêng
    nếu có.

## 9. Quy trình Seller xử lý đơn

1.  Seller nhận thông báo có Seller Order mới.
2.  Seller xem các mặt hàng, số lượng và thông tin cần thiết để chuẩn
    bị.
3.  Seller xác nhận nhận xử lý hoặc từ chối theo chính sách.
4.  Nếu xác nhận, Seller chuẩn bị hàng và cập nhật trạng thái.
5.  Khi sẵn sàng, Seller bàn giao hàng theo quy trình giao nhận.
6.  System ghi nhận thời điểm và trạng thái xử lý.

**Quy tắc:** Seller không được sửa đơn hàng đã xác nhận theo cách làm
sai lệch số tiền hoặc lịch sử. Thay đổi cần được xử lý qua luồng điều
chỉnh/hủy có kiểm soát.

## 10. Quy trình phân công và giao hàng

1.  Khi Seller Order sẵn sàng giao, System tạo Shipment hoặc yêu cầu tạo
    Shipment.
2.  Người/tiến trình có thẩm quyền phân công Shipper.
3.  Shipper xem đơn được phân công và thông tin giao hàng cần thiết.
4.  Shipper nhận hàng, cập nhật trạng thái đang giao.
5.  Shipper cập nhật giao thành công hoặc giao thất bại, kèm ghi chú khi
    cần.
6.  System cập nhật Shipment và đồng bộ trạng thái liên quan của Seller
    Order.
7.  System tính trạng thái tổng của Parent Order từ các Seller Orders.

**Lưu ý:** một Parent Order có thể có nhiều Shipment. Trạng thái đơn
tổng phải phản ánh trạng thái của các đơn con, không được giả định mọi
kiện hàng hoàn tất cùng lúc.

## 11. Quy trình hủy đơn

1.  Customer hoặc Seller gửi yêu cầu hủy trong phạm vi được phép.
2.  System kiểm tra người yêu cầu, quyền sở hữu và trạng thái hiện tại.
3.  Nếu đủ điều kiện, System ghi nhận yêu cầu và chuyển trạng thái theo
    chính sách.
4.  System giải phóng tồn kho nếu hàng chưa được xuất kho và chính sách
    cho phép.
5.  Nếu đã thanh toán, System tạo yêu cầu hoàn tiền hoặc đưa vào quy
    trình xử lý hoàn tiền.
6.  System thông báo kết quả cho các bên liên quan.

**Cần chốt:** mốc thời gian được phép hủy, quyền hủy của từng vai trò,
cách xử lý phí vận chuyển và điều kiện hoàn tiền.

## 12. Quy trình giao hàng thất bại và hoàn trả

### 12.1. Giao hàng thất bại

1.  Shipper chọn kết quả giao thất bại và nhập lý do.
2.  System lưu lịch sử lần giao.
3.  Đơn chuyển sang trạng thái chờ xử lý tiếp theo theo chính sách.
4.  Admin hoặc bộ phận vận hành quyết định giao lại, trả hàng hoặc hủy.
5.  System cập nhật Shipment, Seller Order, Parent Order và thông báo
    cho Customer/Seller.

### 12.2. Yêu cầu hoàn trả/hoàn tiền

1.  Customer gửi yêu cầu kèm lý do và thông tin liên quan.
2.  System kiểm tra thời hạn, trạng thái đơn và chính sách áp dụng.
3.  Seller hoặc Admin xem xét theo quyền được cấp.
4.  Nếu chấp thuận, System ghi nhận quy trình nhận lại hàng (nếu cần) và
    yêu cầu hoàn tiền.
5.  Khi có kết quả từ nhà cung cấp thanh toán, System cập nhật giao dịch
    và trạng thái yêu cầu.
6.  System lưu dấu vết xử lý và thông báo kết quả.

## 13. Quy tắc trạng thái đề xuất

Các trạng thái dưới đây là gợi ý ban đầu, cần được xác nhận khi thiết kế
domain và API.

### 13.1. Parent Order

`PENDING_PAYMENT` → `PAID` → `PROCESSING` → `PARTIALLY_FULFILLED` →
`COMPLETED`

Các trạng thái kết thúc/ngoại lệ có thể gồm: `CANCELLED`,
`PAYMENT_FAILED`, `REFUND_PENDING`, `PARTIALLY_REFUNDED`, `REFUNDED`.

### 13.2. Seller Order

`PENDING` → `CONFIRMED` → `PREPARING` → `READY_TO_SHIP` → `SHIPPED` →
`DELIVERED`

Ngoại lệ có thể gồm: `REJECTED`, `CANCELLED`, `DELIVERY_FAILED`,
`RETURN_REQUESTED`, `RETURNED`.

### 13.3. Payment

`INITIATED` → `PENDING` → `SUCCEEDED`

Ngoại lệ: `FAILED`, `CANCELLED`, `EXPIRED`, `REFUND_PENDING`,
`PARTIALLY_REFUNDED`, `REFUNDED`.

### 13.4. Shipment

`PENDING_ASSIGNMENT` → `ASSIGNED` → `PICKED_UP` → `IN_TRANSIT` →
`DELIVERED`

Ngoại lệ: `DELIVERY_FAILED`, `RETURNING`, `RETURNED`, `CANCELLED`.

**Nguyên tắc:** không cho phép chuyển trạng thái tùy ý. Mỗi chuyển đổi
cần có điều kiện, vai trò được phép thực hiện và tác động dữ liệu rõ
ràng.

## 14. Phân quyền theo quy trình

  ------------------------------------------------------------------------
  Hành động          Customer         Seller          Admin        Shipper
  ------------ -------------- -------------- -------------- --------------
  Xem danh                 Có             Có             Có             Có
  mục/sản phẩm                                              
  công khai                                                 

  Quản lý hồ               Có             Có             Có             Có
  sơ cá nhân                                                

  Quản lý sản           Không   Cửa hàng của             Có          Không
  phẩm cửa                              mình                
  hàng                                                      

  Xem đơn hàng   Đơn của mình    Đơn của cửa             Có  Đơn được giao
                                   hàng mình                

  Xác                   Không    Đơn của cửa     Theo quyền          Không
  nhận/chuẩn                       hàng mình       quản trị 
  bị Seller                                                 
  Order                                                     

  Phân công             Không     Theo chính             Có          Không
  Shipper                               sách                

  Cập nhật              Không          Không     Có thể can  Đơn được giao
  tiến trình                                          thiệp 
  giao                                                      

  Duyệt                 Không          Không             Có          Không
  Seller/cửa                                                
  hàng                                                      
  ------------------------------------------------------------------------

## 15. Các quyết định nghiệp vụ còn mở

-   [ ] Một Seller có thể sở hữu nhiều cửa hàng không?
-   [ ] Khách được checkout khi chưa đăng nhập không?
-   [ ] MVP dùng COD, thanh toán trực tuyến hay cả hai?
-   [ ] Phí vận chuyển tính theo Seller Order hay Parent Order?
-   [ ] Ai phân công Shipper và có hỗ trợ tự động không?
-   [ ] Khi một Seller từ chối đơn, các Seller Order còn lại tiếp tục
    hay toàn bộ Parent Order bị hủy?
-   [ ] Tồn kho được giữ tại thời điểm checkout hay chỉ trừ khi thanh
    toán thành công?
-   [ ] Có thu hoa hồng nền tảng hoặc chia tiền tự động không?
-   [ ] Chính sách hủy, giao lại, đổi trả và hoàn tiền cụ thể là gì?

## 16. Tiêu chí hoàn thành tài liệu

-   [ ] Mỗi quy trình có tác nhân, điều kiện bắt đầu, luồng chính và
    ngoại lệ.
-   [ ] Quy trình đặt hàng đa Seller được mô tả rõ từ checkout đến giao
    hàng.
-   [ ] Trạng thái Parent Order, Seller Order, Payment và Shipment được
    tách biệt.
-   [ ] Quyền của từng vai trò được xác định.
-   [ ] Các câu hỏi nghiệp vụ còn mở được ghi lại và có người/nhóm chịu
    trách nhiệm xác nhận.
-   [ ] Quy trình đủ rõ để chuyển thành ERD, API và test cases.

------------------------------------------------------------------------

**Trạng thái:** Bản nháp nghiệp vụ. Không xem các trạng thái, chính sách
thanh toán, phân công giao hàng và hoàn tiền là quyết định cuối cùng cho
đến khi được xác nhận.
