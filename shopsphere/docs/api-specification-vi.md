# ShopSphere -- Đặc tả API

**Tài liệu:** `docs/05-api/api-specification.md`\
**Trạng thái:** Bản thiết kế ban đầu\
**Dự án:** ShopSphere -- Nền tảng thương mại điện tử đa nhà bán hàng

## 1. Mục đích

Tài liệu này mô tả hợp đồng API giữa frontend và backend của ShopSphere.
Mục tiêu là giúp nhóm thống nhất endpoint, dữ liệu đầu vào/đầu ra, quyền
truy cập và quy tắc nghiệp vụ trước khi triển khai.

Đây là đặc tả ban đầu cho MVP. Các endpoint và trường dữ liệu có thể
được điều chỉnh khi hoàn thiện nghiệp vụ và thiết kế database.

## 2. Quy ước chung

-   Base path: `/api/v1`
-   Giao thức: HTTPS trong môi trường triển khai; HTTP có thể dùng cục
    bộ.
-   Định dạng dữ liệu: JSON, mã hóa UTF-8.
-   Thời gian: ISO 8601, lưu và trao đổi thống nhất theo UTC; frontend
    hiển thị theo múi giờ người dùng.
-   ID: UUID.
-   Tiền: dùng số nguyên theo đơn vị tiền nhỏ nhất hoặc kiểu decimal
    chính xác; không dùng số thực dấu phẩy động. API mẫu dưới đây biểu
    diễn tiền bằng decimal string.
-   Phân trang: `page` bắt đầu từ `0`, `size` mặc định `20`, giới hạn
    tối đa `100`.
-   Sắp xếp: `sort=createdAt,desc`.
-   Các endpoint cần đăng nhập nhận header
    `Authorization: Bearer <access_token>`.
-   Backend luôn kiểm tra quyền; ẩn nút trên frontend không được xem là
    cơ chế bảo mật.

## 3. Định dạng phản hồi

### 3.1 Phản hồi thành công

Các endpoint trả về tài nguyên sẽ trả trực tiếp đối tượng hoặc danh sách
có metadata phân trang.

Ví dụ:

``` json
{
  "id": "9f6a7c8e-1234-4abc-8def-0123456789ab",
  "name": "Áo thun basic",
  "price": "199000.00",
  "currency": "VND"
}
```

### 3.2 Phản hồi lỗi thống nhất

``` json
{
  "timestamp": "2026-10-01T10:15:30Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu gửi lên không hợp lệ.",
  "path": "/api/v1/products",
  "fieldErrors": [
    {
      "field": "price",
      "message": "Giá phải lớn hơn 0."
    }
  ],
  "traceId": "optional-request-trace-id"
}
```

Quy ước: - `code`: mã lỗi ổn định để frontend xử lý. - `message`: thông
báo dễ hiểu, không chứa stack trace hoặc thông tin nhạy cảm. -
`fieldErrors`: chỉ xuất hiện khi lỗi liên quan đến trường dữ liệu. -
`traceId`: hỗ trợ truy vết log nếu được bật.

### 3.3 Mã HTTP thường dùng

  -----------------------------------------------------------------------
  Mã                                  Ý nghĩa
  ----------------------------------- -----------------------------------
  `200 OK`                            Request thành công

  `201 Created`                       Tạo tài nguyên thành công

  `202 Accepted`                      Đã tiếp nhận xử lý bất đồng bộ

  `204 No Content`                    Thành công, không có nội dung trả
                                      về

  `400 Bad Request`                   Dữ liệu hoặc tham số không hợp lệ

  `401 Unauthorized`                  Chưa xác thực hoặc token không hợp
                                      lệ

  `403 Forbidden`                     Đã xác thực nhưng không có quyền

  `404 Not Found`                     Không tìm thấy tài nguyên

  `409 Conflict`                      Xung đột trạng thái hoặc dữ liệu

  `422 Unprocessable Entity`          Request hợp lệ về cú pháp nhưng
                                      không thỏa quy tắc nghiệp vụ

  `429 Too Many Requests`             Gửi request quá thường xuyên

  `500 Internal Server Error`         Lỗi máy chủ không dự kiến
  -----------------------------------------------------------------------

## 4. Xác thực và tài khoản

### 4.1 Đăng ký

`POST /api/v1/auth/register`

Quyền: công khai.

Request:

``` json
{
  "fullName": "Nguyen Van An",
  "email": "an@example.com",
  "password": "StrongPassword123!"
}
```

Response: `201 Created`

``` json
{
  "id": "user-uuid",
  "fullName": "Nguyen Van An",
  "email": "an@example.com",
  "roles": ["CUSTOMER"]
}
```

Quy tắc: - Email phải hợp lệ và chưa được sử dụng. - Mật khẩu phải đáp
ứng chính sách bảo mật. - Tài khoản đăng ký công khai mặc định có vai
trò `CUSTOMER`. - Không cho phép người dùng tự đăng ký vai trò `ADMIN`.

### 4.2 Đăng nhập

`POST /api/v1/auth/login`

Quyền: công khai.

Request:

``` json
{
  "email": "an@example.com",
  "password": "StrongPassword123!"
}
```

Response: `200 OK`

``` json
{
  "accessToken": "jwt-access-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "user-uuid",
    "fullName": "Nguyen Van An",
    "email": "an@example.com",
    "roles": ["CUSTOMER"]
  }
}
```

### 4.3 Làm mới token

`POST /api/v1/auth/refresh`

Quyền: cần refresh token hợp lệ theo cơ chế lưu trữ được chọn.

Request/response chi tiết phụ thuộc quyết định triển khai refresh token.
Không đặt refresh token dài hạn trong local storage nếu chưa đánh giá
rủi ro XSS.

### 4.4 Đăng xuất

`POST /api/v1/auth/logout`

Quyền: người dùng đã đăng nhập.

Response: `204 No Content`.

Nếu triển khai refresh-token rotation, endpoint này cần thu hồi refresh
token tương ứng. JWT access token đã phát hành có thể còn hiệu lực đến
khi hết hạn, trừ khi có cơ chế thu hồi bổ sung.

### 4.5 Hồ sơ hiện tại

`GET /api/v1/users/me`

Quyền: người dùng đã đăng nhập.

`PATCH /api/v1/users/me`

Quyền: người dùng đã đăng nhập; chỉ cập nhật các trường được phép.

## 5. Danh mục và sản phẩm

### 5.1 Danh sách danh mục

`GET /api/v1/categories`

Quyền: công khai.

Query tùy chọn: `parentId`, `active`.

### 5.2 Danh sách sản phẩm

`GET /api/v1/products`

Quyền: công khai.

Query tùy chọn: - `keyword` - `categoryId` - `storeId` - `minPrice`,
`maxPrice` - `page`, `size`, `sort`

Chỉ trả về sản phẩm đang được phép hiển thị và còn hiệu lực theo trạng
thái sản phẩm.

### 5.3 Chi tiết sản phẩm

`GET /api/v1/products/{productId}`

Quyền: công khai đối với sản phẩm được hiển thị.

### 5.4 Sản phẩm của Seller

`GET /api/v1/seller/products`

Quyền: `SELLER`.

Trả về sản phẩm thuộc Store của Seller đang đăng nhập; không nhận
`sellerId` tùy ý để tránh truy cập dữ liệu Seller khác.

### 5.5 Tạo sản phẩm

`POST /api/v1/seller/products`

Quyền: `SELLER`, tài khoản phải có Store đang hoạt động.

Request:

``` json
{
  "name": "Áo thun basic",
  "description": "Áo thun cotton",
  "categoryId": "category-uuid",
  "price": "199000.00",
  "stockQuantity": 50,
  "imageUrls": [
    "https://example.com/product-image.jpg"
  ]
}
```

Response: `201 Created`.

Backend tự xác định Store từ Seller đang đăng nhập; không tin `storeId`
do client tự gửi.

### 5.6 Cập nhật sản phẩm

`PUT /api/v1/seller/products/{productId}`

Quyền: `SELLER` sở hữu sản phẩm.

### 5.7 Ẩn hoặc ngừng bán sản phẩm

`PATCH /api/v1/seller/products/{productId}/status`

Quyền: Seller sở hữu sản phẩm.

Request:

``` json
{
  "status": "INACTIVE"
}
```

Các trạng thái hợp lệ cần được định nghĩa trong enum nghiệp vụ, ví dụ
`DRAFT`, `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`, `REJECTED`.

## 6. Cửa hàng

### 6.1 Xem cửa hàng công khai

`GET /api/v1/stores/{storeId}`

Quyền: công khai với Store đang hoạt động.

### 6.2 Xem cửa hàng của Seller hiện tại

`GET /api/v1/seller/store`

Quyền: `SELLER`.

### 6.3 Tạo cửa hàng

`POST /api/v1/seller/store`

Quyền: `SELLER`.

Request:

``` json
{
  "name": "An Store",
  "description": "Cửa hàng thời trang",
  "logoUrl": "https://example.com/logo.png"
}
```

Quy tắc: mỗi Seller chỉ được sở hữu tối đa một Store. Database cần có
unique constraint trên `stores.seller_id`; API phải trả `409 Conflict`
nếu Seller đã có Store.

### 6.4 Cập nhật cửa hàng

`PUT /api/v1/seller/store`

Quyền: Seller sở hữu Store.

## 7. Giỏ hàng

### 7.1 Xem giỏ hàng

`GET /api/v1/cart`

Quyền: `CUSTOMER`.

Giỏ hàng có thể chứa sản phẩm từ nhiều Store. Response nên nhóm dòng
hàng theo Store để frontend hiển thị rõ nhà bán.

### 7.2 Thêm sản phẩm

`POST /api/v1/cart/items`

Quyền: `CUSTOMER`.

Request:

``` json
{
  "productId": "product-uuid",
  "quantity": 2
}
```

Quy tắc: - Sản phẩm phải đang được bán. - Số lượng phải là số nguyên
dương và không vượt quá giới hạn nghiệp vụ. - Backend kiểm tra tồn kho
hiện tại. - Giá cuối cùng phải được xác nhận lại khi checkout; không tin
giá do frontend gửi.

### 7.3 Cập nhật số lượng

`PATCH /api/v1/cart/items/{cartItemId}`

Quyền: chủ sở hữu giỏ hàng.

Request:

``` json
{
  "quantity": 3
}
```

### 7.4 Xóa dòng hàng

`DELETE /api/v1/cart/items/{cartItemId}`

Quyền: chủ sở hữu giỏ hàng.

Response: `204 No Content`.

### 7.5 Xóa giỏ hàng

`DELETE /api/v1/cart/items`

Quyền: `CUSTOMER`.

Xóa các dòng hàng trong giỏ hiện tại; không xóa dữ liệu giỏ của người
dùng khác.

## 8. Địa chỉ giao hàng

### 8.1 Danh sách địa chỉ

`GET /api/v1/users/me/addresses`

Quyền: chủ tài khoản.

### 8.2 Tạo địa chỉ

`POST /api/v1/users/me/addresses`

Quyền: chủ tài khoản.

Request:

``` json
{
  "recipientName": "Nguyen Van An",
  "phone": "0900000000",
  "province": "Ho Chi Minh City",
  "district": "District 1",
  "ward": "Ben Nghe",
  "streetAddress": "123 Example Street",
  "defaultAddress": true
}
```

### 8.3 Cập nhật hoặc xóa địa chỉ

-   `PUT /api/v1/users/me/addresses/{addressId}`
-   `DELETE /api/v1/users/me/addresses/{addressId}`

Quyền: chủ tài khoản. Không cho phép thao tác với địa chỉ thuộc người
dùng khác.

## 9. Checkout và đơn hàng

### 9.1 Xem trước checkout

`POST /api/v1/checkout/preview`

Quyền: `CUSTOMER`.

Request:

``` json
{
  "addressId": "address-uuid",
  "items": [
    {
      "cartItemId": "cart-item-uuid",
      "quantity": 2
    }
  ],
  "voucherCodes": []
}
```

Response mẫu:

``` json
{
  "currency": "VND",
  "itemsSubtotal": "398000.00",
  "shippingFee": "30000.00",
  "discountTotal": "0.00",
  "grandTotal": "428000.00",
  "sellerGroups": [
    {
      "storeId": "store-uuid",
      "storeName": "An Store",
      "subtotal": "398000.00",
      "items": [
        {
          "productId": "product-uuid",
          "productName": "Áo thun basic",
          "quantity": 2,
          "unitPrice": "199000.00",
          "lineTotal": "398000.00"
        }
      ]
    }
  ],
  "warnings": []
}
```

Đây chỉ là bản xem trước. Backend phải tính lại giá, tồn kho, phí vận
chuyển và khuyến mãi khi tạo đơn.

### 9.2 Tạo đơn hàng

`POST /api/v1/checkout`

Quyền: `CUSTOMER`.

Request:

``` json
{
  "addressId": "address-uuid",
  "paymentMethod": "COD",
  "items": [
    {
      "productId": "product-uuid",
      "quantity": 2
    },
    {
      "productId": "another-product-uuid",
      "quantity": 1
    }
  ],
  "voucherCodes": [],
  "idempotencyKey": "unique-client-generated-key"
}
```

Response: `201 Created`.

``` json
{
  "orderId": "parent-order-uuid",
  "orderNumber": "SS-2026-000001",
  "status": "PENDING_PAYMENT",
  "paymentStatus": "UNPAID",
  "grandTotal": "528000.00",
  "currency": "VND",
  "sellerOrders": [
    {
      "sellerOrderId": "seller-order-uuid-1",
      "storeName": "An Store",
      "status": "NEW",
      "subtotal": "398000.00"
    },
    {
      "sellerOrderId": "seller-order-uuid-2",
      "storeName": "Binh Store",
      "status": "NEW",
      "subtotal": "100000.00"
    }
  ]
}
```

Quy tắc nghiệp vụ: - Một lần checkout tạo một Parent Order. - Backend
nhóm các mặt hàng theo Store và tạo một Seller Order cho mỗi Store có
hàng trong đơn. - Mỗi Seller Order chỉ chứa sản phẩm của một Store. -
Giá, giảm giá, phí vận chuyển và tổng tiền được tính ở backend. - Kiểm
tra lại trạng thái sản phẩm và tồn kho trước khi ghi nhận đơn. - Dùng
`idempotencyKey` để hạn chế tạo đơn trùng khi client retry. - Việc trừ
hoặc giữ tồn kho phải nằm trong transaction/chiến lược nhất quán đã
thiết kế. - Không tin `sellerId`, `storeId`, đơn giá hoặc tổng tiền do
client gửi.

### 9.3 Danh sách đơn hàng của Customer

`GET /api/v1/users/me/orders`

Quyền: `CUSTOMER`.

Query: `status`, `page`, `size`, `sort`.

### 9.4 Chi tiết Parent Order

`GET /api/v1/orders/{orderId}`

Quyền: chủ đơn hàng hoặc `ADMIN`.

Response cần bao gồm thông tin tổng đơn và các Seller Order con, nhưng
chỉ hiển thị dữ liệu phù hợp với quyền của người gọi.

### 9.5 Danh sách Seller Order của Seller

`GET /api/v1/seller/orders`

Quyền: `SELLER`.

Query: `status`, `page`, `size`, `sort`.

Chỉ trả về Seller Order thuộc Store của Seller hiện tại.

### 9.6 Chi tiết Seller Order

`GET /api/v1/seller/orders/{sellerOrderId}`

Quyền: Seller sở hữu Store tương ứng hoặc `ADMIN`.

### 9.7 Seller xác nhận hoặc cập nhật xử lý đơn

`PATCH /api/v1/seller/orders/{sellerOrderId}/status`

Quyền: Seller sở hữu Store.

Request:

``` json
{
  "status": "CONFIRMED"
}
```

Chỉ cho phép chuyển trạng thái hợp lệ theo state machine; không cho
client tùy ý đặt trạng thái bất kỳ.

### 9.8 Customer yêu cầu hủy đơn

`POST /api/v1/orders/{orderId}/cancellation-requests`

Quyền: chủ đơn hàng.

Request:

``` json
{
  "reason": "Customer requested cancellation"
}
```

Backend kiểm tra trạng thái từng Seller Order và chính sách hủy. Đơn đã
bàn giao vận chuyển có thể không được hủy theo luồng này.

## 10. Thanh toán

### 10.1 Khởi tạo thanh toán

`POST /api/v1/orders/{orderId}/payments`

Quyền: chủ đơn hàng.

Request:

``` json
{
  "paymentMethod": "ONLINE"
}
```

Response phụ thuộc nhà cung cấp thanh toán. Không lưu thông tin thẻ nhạy
cảm trong database ShopSphere.

### 10.2 Xem trạng thái thanh toán

`GET /api/v1/orders/{orderId}/payments`

Quyền: chủ đơn hàng hoặc `ADMIN`.

### 10.3 Webhook thanh toán

`POST /api/v1/payments/webhooks/{provider}`

Quyền: chỉ chấp nhận request được xác minh theo chữ ký/cơ chế bảo mật
của nhà cung cấp.

Webhook phải: - Xác minh chữ ký và tính hợp lệ của sự kiện. - Xử lý lặp
an toàn (idempotent). - Không tin trạng thái thanh toán do frontend
báo. - Ghi nhận mã giao dịch và kết quả xử lý để đối soát.

### 10.4 COD

Với `COD`, đơn được ghi nhận là chưa thu tiền cho đến khi có xác nhận
thu tiền theo quy trình vận hành. Không đánh dấu thanh toán thành công
ngay tại thời điểm tạo đơn.

## 11. Vận chuyển

### 11.1 Danh sách shipment của Customer

`GET /api/v1/users/me/shipments`

Quyền: chủ đơn hàng.

### 11.2 Chi tiết shipment

`GET /api/v1/shipments/{shipmentId}`

Quyền: Customer sở hữu đơn, Seller liên quan, Shipper được phân công
hoặc `ADMIN`.

### 11.3 Danh sách shipment dành cho Shipper

`GET /api/v1/shipper/shipments`

Quyền: `SHIPPER`.

Query: `status`, `page`, `size`.

Chỉ trả về shipment được phân công cho Shipper đang đăng nhập.

### 11.4 Cập nhật trạng thái giao hàng

`PATCH /api/v1/shipper/shipments/{shipmentId}/status`

Quyền: Shipper được phân công.

Request:

``` json
{
  "status": "PICKED_UP",
  "note": "Đã nhận hàng từ cửa hàng"
}
```

Backend kiểm tra người giao được phân công và trạng thái chuyển tiếp hợp
lệ. Các trạng thái ví dụ: `READY_FOR_PICKUP`, `PICKED_UP`, `IN_TRANSIT`,
`DELIVERED`, `FAILED`, `RETURNING`, `RETURNED`.

### 11.5 Admin phân công Shipper

`POST /api/v1/admin/shipments/{shipmentId}/assign`

Quyền: `ADMIN`.

Request:

``` json
{
  "shipperId": "shipper-user-uuid"
}
```

Backend xác minh tài khoản có vai trò Shipper và shipment đủ điều kiện
được phân công.

## 12. Quản trị

Các endpoint dưới đây yêu cầu vai trò `ADMIN`:

  -------------------------------------------------------------------------------------------------
  Method                  Endpoint                                          Mục đích
  ----------------------- ------------------------------------------------- -----------------------
  `GET`                   `/api/v1/admin/users`                             Tìm kiếm và phân trang
                                                                            người dùng

  `PATCH`                 `/api/v1/admin/users/{userId}/status`             Khóa/mở tài khoản theo
                                                                            chính sách

  `GET`                   `/api/v1/admin/stores`                            Quản lý, kiểm duyệt cửa
                                                                            hàng

  `PATCH`                 `/api/v1/admin/stores/{storeId}/status`           Duyệt, tạm ngưng hoặc
                                                                            kích hoạt Store

  `GET`                   `/api/v1/admin/products/pending`                  Xem sản phẩm chờ kiểm
                                                                            duyệt nếu có

  `PATCH`                 `/api/v1/admin/products/{productId}/moderation`   Duyệt hoặc từ chối sản
                                                                            phẩm

  `GET`                   `/api/v1/admin/orders`                            Tra cứu đơn hàng toàn
                                                                            hệ thống

  `GET`                   `/api/v1/admin/payments`                          Tra cứu giao dịch thanh
                                                                            toán

  `GET`                   `/api/v1/admin/shipments`                         Tra cứu vận đơn và phân
                                                                            công
  -------------------------------------------------------------------------------------------------

Các thao tác quản trị quan trọng cần ghi audit log, bao gồm người thực
hiện, thời điểm, đối tượng và kết quả.

## 13. Phân quyền tổng quát

  --------------------------------------------------------------------------------
  Nhóm API           Công khai     Customer       Seller      Shipper        Admin
  --------------- ------------ ------------ ------------ ------------ ------------
  Đăng ký/đăng              Có           Có           Có           Có           Có
  nhập                                                                

  Xem danh                  Có           Có           Có           Có           Có
  mục/sản phẩm                                                        
  công khai                                                           

  Quản lý giỏ và         Không           Có    Không mặc        Không   Theo quyền
  địa chỉ của                                       định                  quản trị
  mình                                                                

  Checkout và xem        Không           Có        Không        Không           Có
  đơn của mình                                                        

  Quản lý                Không        Không           Có        Không           Có
  Store/Product                                                       
  của mình                                                            

  Xử lý Seller           Không        Không           Có        Không           Có
  Order thuộc                                                         
  Store của mình                                                      

  Xem/cập nhật           Không Xem shipment         Theo           Có           Có
  shipment được                    của mình     shipment              
  phân công                                    liên quan              

  Quản trị toàn          Không        Không        Không        Không           Có
  hệ thống                                                            
  --------------------------------------------------------------------------------

**Lưu ý:** Bảng này là quyền ở mức chức năng. Mỗi endpoint vẫn phải kiểm
tra quyền sở hữu tài nguyên và trạng thái nghiệp vụ.

## 14. Trạng thái và chuyển trạng thái

Trạng thái dưới đây là đề xuất ban đầu; cần được hiện thực bằng enum và
state machine, không cho phép cập nhật tùy ý.

### 14.1 Parent Order

Ví dụ: `PENDING_PAYMENT`, `CONFIRMED`, `PROCESSING`,
`PARTIALLY_SHIPPED`, `SHIPPED`, `COMPLETED`, `CANCELLATION_REQUESTED`,
`CANCELLED`, `PARTIALLY_CANCELLED`.

Parent Order tổng hợp trạng thái từ các Seller Order, Payment và
Shipment. Không nên để một Seller tự cập nhật trạng thái Parent Order.

### 14.2 Seller Order

Ví dụ: `NEW`, `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`,
`HANDED_OVER`, `COMPLETED`, `CANCELLATION_REQUESTED`, `CANCELLED`,
`REJECTED`.

### 14.3 Payment

Ví dụ: `PENDING`, `UNPAID`, `PROCESSING`, `PAID`, `FAILED`, `CANCELLED`,
`REFUND_PENDING`, `PARTIALLY_REFUNDED`, `REFUNDED`.

### 14.4 Shipment

Ví dụ: `UNASSIGNED`, `READY_FOR_PICKUP`, `ASSIGNED`, `PICKED_UP`,
`IN_TRANSIT`, `DELIVERED`, `FAILED`, `RETURNING`, `RETURNED`.

Mọi chuyển trạng thái phải kiểm tra trạng thái hiện tại, vai trò người
thực hiện và điều kiện nghiệp vụ; các thay đổi quan trọng cần lưu lịch
sử.

## 15. Quy tắc validation và an toàn

-   Validate dữ liệu đầu vào ở backend bằng Bean Validation và kiểm tra
    nghiệp vụ trong application/domain layer.
-   Không tin giá, giảm giá, tổng tiền, seller/store ID hoặc trạng thái
    do client gửi.
-   Không trả password hash, token bí mật, dữ liệu thanh toán nhạy cảm
    hoặc thông tin nội bộ.
-   Kiểm tra quyền sở hữu ở mọi API đọc/ghi tài nguyên riêng tư.
-   Giới hạn kích thước request, phân trang và tần suất gọi các endpoint
    nhạy cảm.
-   Dùng HTTPS trong môi trường triển khai.
-   Ghi log có chọn lọc; không ghi mật khẩu, access token hoặc dữ liệu
    thanh toán nhạy cảm.
-   Dùng idempotency cho các thao tác có thể bị gửi lại, đặc biệt là
    checkout và xử lý webhook.
-   Chuẩn hóa lỗi để frontend có thể hiển thị thông báo phù hợp mà không
    làm lộ chi tiết hệ thống.

## 16. Kiểm thử hợp đồng API

Các nhóm kiểm thử tối thiểu:

1.  **Authentication:** đăng ký, đăng nhập, token hết hạn, token không
    hợp lệ.
2.  **Authorization:** Customer không truy cập đơn người khác; Seller
    không truy cập Store/Order của Seller khác; Shipper chỉ thao tác
    shipment được phân công.
3.  **Catalog:** lọc, phân trang, trạng thái hiển thị và quyền sở hữu
    sản phẩm.
4.  **Cart:** thêm/cập nhật/xóa mặt hàng, kiểm tra tồn kho và giá.
5.  **Checkout:** giỏ có sản phẩm từ nhiều Store tạo một Parent Order và
    nhiều Seller Order; kiểm tra tổng tiền và tính lặp an toàn.
6.  **Order lifecycle:** xác nhận, chuẩn bị, bàn giao, hoàn tất, hủy và
    các chuyển trạng thái không hợp lệ.
7.  **Payment:** webhook sai chữ ký, webhook lặp, thanh toán thất bại và
    COD.
8.  **Shipment:** phân công, cập nhật trạng thái, giao thất bại và kiểm
    tra quyền Shipper.
9.  **Error contract:** mã HTTP, mã lỗi, validation field errors và
    không lộ thông tin nhạy cảm.

## 17. Các quyết định API còn mở

Cần chốt trước hoặc trong quá trình triển khai:

-   Cấu trúc địa chỉ theo dữ liệu hành chính Việt Nam và có hỗ trợ địa
    chỉ quốc tế hay không.
-   Chính sách voucher: áp dụng cho toàn đơn, từng Store hay từng sản
    phẩm.
-   Cách tính phí vận chuyển khi một Parent Order có nhiều Seller Order.
-   Tồn kho được giữ tại checkout hay chỉ trừ khi xác nhận đơn.
-   Chính sách hủy một Seller Order trong khi các Seller Order khác vẫn
    tiếp tục.
-   Nhà cung cấp thanh toán, giao vận và định dạng webhook cụ thể.
-   Refresh token, thu hồi token và quản lý phiên đăng nhập.
-   Quy ước API versioning, sorting và filter nâng cao.
-   Chính sách retry, timeout và xử lý sự cố tích hợp ngoài.

## 18. Tiêu chí hoàn thành tài liệu API

-   Mỗi endpoint có method, path, quyền truy cập, request và response
    mẫu.
-   Các endpoint quan trọng có quy tắc nghiệp vụ và lỗi dự kiến.
-   Phân quyền bao gồm kiểm tra role lẫn quyền sở hữu.
-   Checkout đa nhà bán hàng được mô tả rõ ở cấp Parent Order và Seller
    Order.
-   Trạng thái có quy tắc chuyển tiếp, không chỉ là danh sách enum.
-   Frontend và backend thống nhất hợp đồng trước khi tích hợp.
-   Các câu hỏi còn mở được ghi nhận thay vì tự giả định.

## 19. Bước tiếp theo

Sau khi rà soát API Specification, bước tiếp theo là lập kế hoạch kiểm
thử tại:

`docs/06-testing/test-plan.md`

Kế hoạch cần bao gồm unit test, integration test, API/security test và
các kịch bản nghiệp vụ trọng yếu của marketplace.
