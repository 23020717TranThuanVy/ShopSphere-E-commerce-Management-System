# ShopSphere --- Database Design

> **Tài liệu:** Thiết kế cơ sở dữ liệu\
> **Phiên bản:** 0.1\
> **Trạng thái:** Bản nháp thiết kế\
> **Ngày cập nhật:** 01/10/2026\
> **Cơ sở:** Requirements Analysis và Business Workflows

## 1. Mục tiêu

Thiết kế cơ sở dữ liệu quan hệ cho ShopSphere, nền tảng thương mại điện
tử đa nhà bán hàng. Thiết kế hỗ trợ một lần checkout có thể chứa sản
phẩm từ nhiều cửa hàng, đồng thời mỗi Seller chỉ sở hữu tối đa một cửa
hàng.

## 2. Quyết định nghiệp vụ đã xác nhận

-   Một Seller có tối đa một Store.
-   Một Store có thể bán nhiều Product.
-   Mỗi Product thuộc về đúng một Store.
-   Mỗi Product thuộc một Category trong phiên bản đầu.
-   Một Parent Order có thể chứa nhiều Seller Orders.
-   Mỗi Seller Order đại diện cho phần hàng của một Store trong đơn
    tổng.
-   Payment và Shipment được mô hình hóa riêng với Order.

## 3. ERD tổng quan

``` mermaid
erDiagram
    USERS ||--o| STORES : owns
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    CATEGORIES ||--o{ PRODUCTS : classifies
    STORES ||--o{ PRODUCTS : lists
    USERS ||--o{ ADDRESSES : has
    USERS ||--o| CARTS : owns
    CARTS ||--o{ CART_ITEMS : contains
    PRODUCTS ||--o{ CART_ITEMS : selected
    USERS ||--o{ ORDERS : places
    ADDRESSES ||--o{ ORDERS : used_for
    ORDERS ||--|{ SELLER_ORDERS : contains
    STORES ||--o{ SELLER_ORDERS : fulfills
    SELLER_ORDERS ||--|{ ORDER_ITEMS : contains
    PRODUCTS ||--o{ ORDER_ITEMS : referenced
    ORDERS ||--o{ PAYMENTS : paid_by
    SELLER_ORDERS ||--o{ SHIPMENTS : shipped_as
    USERS ||--o{ SHIPMENTS : assigned_to
```

> ERD khái quát. Ràng buộc, cardinality tùy chọn và trạng thái chi tiết
> được đặc tả ở các phần sau.

## 4. Danh sách thực thể

  Bảng                     Mục đích
  ------------------------ --------------------------------------------
  `users`                  Tài khoản Customer, Seller, Admin, Shipper
  `roles`                  Danh mục vai trò
  `user_roles`             Gán vai trò cho tài khoản
  `stores`                 Thông tin cửa hàng của Seller
  `categories`             Danh mục sản phẩm
  `products`               Sản phẩm được bán tại cửa hàng
  `addresses`              Địa chỉ giao hàng của người dùng
  `carts`                  Giỏ hàng của Customer
  `cart_items`             Các sản phẩm trong giỏ
  `orders`                 Đơn hàng tổng (Parent Order)
  `seller_orders`          Đơn hàng con theo cửa hàng
  `order_items`            Dòng sản phẩm thuộc Seller Order
  `payments`               Giao dịch thanh toán/hoàn tiền
  `shipments`              Các kiện/lượt giao hàng
  `order_status_history`   Lịch sử thay đổi trạng thái đơn

## 5. Đặc tả bảng chính

### 5.1. `users`

  Cột               Kiểu PostgreSQL   Ràng buộc
  ----------------- ----------------- ------------------
  `id`              `UUID`            PK
  `full_name`       `VARCHAR(150)`    NOT NULL
  `email`           `VARCHAR(255)`    NOT NULL, UNIQUE
  `password_hash`   `VARCHAR(255)`    NOT NULL
  `phone`           `VARCHAR(30)`     NULL
  `status`          `VARCHAR(30)`     NOT NULL
  `created_at`      `TIMESTAMPTZ`     NOT NULL
  `updated_at`      `TIMESTAMPTZ`     NOT NULL

Không lưu mật khẩu dạng rõ. Chỉ lưu password hash từ thuật toán băm mật
khẩu phù hợp.

### 5.2. `roles` và `user_roles`

`roles` gồm `id UUID PK`, `code VARCHAR(50) UNIQUE NOT NULL`,
`name VARCHAR(100) NOT NULL`.

`user_roles` gồm `user_id UUID FK`, `role_id UUID FK`,
`assigned_at TIMESTAMPTZ`. Khóa chính ghép (`user_id`, `role_id`). Có
thể một tài khoản có nhiều vai trò nếu chính sách sản phẩm cho phép.

### 5.3. `stores`

  Cột             Kiểu PostgreSQL   Ràng buộc
  --------------- ----------------- -----------------------------------
  `id`            `UUID`            PK
  `seller_id`     `UUID`            FK → `users.id`, UNIQUE, NOT NULL
  `name`          `VARCHAR(160)`    NOT NULL
  `description`   `TEXT`            NULL
  `status`        `VARCHAR(30)`     NOT NULL
  `created_at`    `TIMESTAMPTZ`     NOT NULL
  `updated_at`    `TIMESTAMPTZ`     NOT NULL

**Ràng buộc quan trọng:** `UNIQUE(seller_id)` bảo đảm mỗi Seller có tối
đa một cửa hàng. Quy trình duyệt Seller/cửa hàng được thể hiện bằng
`status`.

### 5.4. `categories`

  Cột           Kiểu PostgreSQL   Ràng buộc
  ------------- ----------------- ----------------------------
  `id`          `UUID`            PK
  `parent_id`   `UUID`            FK → `categories.id`, NULL
  `name`        `VARCHAR(120)`    NOT NULL
  `slug`        `VARCHAR(150)`    NOT NULL, UNIQUE
  `status`      `VARCHAR(30)`     NOT NULL

`parent_id` cho phép xây dựng danh mục cha--con.

### 5.5. `products`

  Cột                Kiểu PostgreSQL   Ràng buộc
  ------------------ ----------------- --------------------------------
  `id`               `UUID`            PK
  `store_id`         `UUID`            FK → `stores.id`, NOT NULL
  `category_id`      `UUID`            FK → `categories.id`, NOT NULL
  `name`             `VARCHAR(200)`    NOT NULL
  `description`      `TEXT`            NULL
  `sku`              `VARCHAR(100)`    NOT NULL
  `price`            `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `stock_quantity`   `INTEGER`         NOT NULL, CHECK \>= 0
  `status`           `VARCHAR(30)`     NOT NULL
  `created_at`       `TIMESTAMPTZ`     NOT NULL
  `updated_at`       `TIMESTAMPTZ`     NOT NULL

Đặt `UNIQUE(store_id, sku)` để SKU không bị trùng trong cùng cửa hàng.
Giá và tồn kho phải được kiểm tra ở backend; cập nhật tồn kho cần xử lý
đồng thời an toàn.

### 5.6. `addresses`

Các cột đề xuất: `id UUID PK`, `user_id UUID FK NOT NULL`,
`recipient_name VARCHAR(150) NOT NULL`, `phone VARCHAR(30) NOT NULL`,
`line1 VARCHAR(255) NOT NULL`, `ward VARCHAR(120)`,
`district VARCHAR(120)`, `city VARCHAR(120) NOT NULL`,
`country_code CHAR(2) NOT NULL`, `is_default BOOLEAN NOT NULL`,
`created_at TIMESTAMPTZ NOT NULL`.

Khi tạo đơn, nên lưu snapshot địa chỉ giao hàng trên Order để thay đổi
địa chỉ trong sổ địa chỉ không làm thay đổi đơn đã đặt.

### 5.7. `carts` và `cart_items`

`carts`: `id UUID PK`, `customer_id UUID FK UNIQUE NOT NULL`,
`created_at`, `updated_at`.

`cart_items`: `id UUID PK`, `cart_id UUID FK NOT NULL`,
`product_id UUID FK NOT NULL`,
`quantity INTEGER NOT NULL CHECK (quantity > 0)`, `created_at`,
`updated_at`.

Đặt `UNIQUE(cart_id, product_id)` để một sản phẩm chỉ có một dòng trong
cùng giỏ. Giá được xác nhận lại khi checkout, không xem giá trong giỏ là
giá chốt.

### 5.8. `orders` --- Parent Order

  Cột                           Kiểu PostgreSQL   Ràng buộc
  ----------------------------- ----------------- ---------------------------
  `id`                          `UUID`            PK
  `order_number`                `VARCHAR(40)`     NOT NULL, UNIQUE
  `customer_id`                 `UUID`            FK → `users.id`, NOT NULL
  `status`                      `VARCHAR(40)`     NOT NULL
  `currency`                    `CHAR(3)`         NOT NULL
  `subtotal`                    `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `shipping_total`              `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `discount_total`              `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `grand_total`                 `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `shipping_address_snapshot`   `JSONB`           NOT NULL
  `created_at`                  `TIMESTAMPTZ`     NOT NULL
  `updated_at`                  `TIMESTAMPTZ`     NOT NULL

Parent Order là lần checkout của Customer. Snapshot địa chỉ giữ thông
tin giao hàng tại thời điểm đặt.

### 5.9. `seller_orders`

  Cột              Kiểu PostgreSQL   Ràng buộc
  ---------------- ----------------- ----------------------------
  `id`             `UUID`            PK
  `order_id`       `UUID`            FK → `orders.id`, NOT NULL
  `store_id`       `UUID`            FK → `stores.id`, NOT NULL
  `status`         `VARCHAR(40)`     NOT NULL
  `subtotal`       `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `shipping_fee`   `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `created_at`     `TIMESTAMPTZ`     NOT NULL
  `updated_at`     `TIMESTAMPTZ`     NOT NULL

Đặt `UNIQUE(order_id, store_id)` để một Parent Order chỉ có tối đa một
Seller Order cho mỗi Store.

### 5.10. `order_items`

  Cột                       Kiểu PostgreSQL   Ràng buộc
  ------------------------- ----------------- -----------------------------------
  `id`                      `UUID`            PK
  `seller_order_id`         `UUID`            FK → `seller_orders.id`, NOT NULL
  `product_id`              `UUID`            FK → `products.id`, NOT NULL
  `product_name_snapshot`   `VARCHAR(200)`    NOT NULL
  `sku_snapshot`            `VARCHAR(100)`    NOT NULL
  `unit_price`              `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0
  `quantity`                `INTEGER`         NOT NULL, CHECK \> 0
  `line_total`              `NUMERIC(12,2)`   NOT NULL, CHECK \>= 0

Lưu snapshot tên, SKU và đơn giá để lịch sử đơn hàng không bị thay đổi
khi Seller chỉnh sửa sản phẩm. `line_total` nên được tính và kiểm tra ở
backend.

### 5.11. `payments`

Các cột đề xuất: `id UUID PK`, `order_id UUID FK NOT NULL`,
`provider VARCHAR(80) NOT NULL`,
`provider_transaction_id VARCHAR(160) NULL`,
`method VARCHAR(40) NOT NULL`, `status VARCHAR(40) NOT NULL`,
`amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0)`,
`currency CHAR(3) NOT NULL`, `idempotency_key VARCHAR(120) NULL`,
`created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ NOT NULL`.

Có thể có nhiều bản ghi Payment cho một Order khi có thử thanh toán lại
hoặc hoàn tiền, tùy mô hình giao dịch được chọn. Cần ràng buộc duy nhất
phù hợp cho mã giao dịch từ nhà cung cấp.

### 5.12. `shipments`

Các cột đề xuất: `id UUID PK`, `seller_order_id UUID FK NOT NULL`,
`shipper_id UUID FK → users.id NULL`,
`tracking_number VARCHAR(120) NULL`, `status VARCHAR(40) NOT NULL`,
`assigned_at TIMESTAMPTZ NULL`, `shipped_at TIMESTAMPTZ NULL`,
`delivered_at TIMESTAMPTZ NULL`, `failure_reason TEXT NULL`,
`created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ NOT NULL`.

Một Seller Order có thể có nhiều Shipment nếu nghiệp vụ hỗ trợ giao tách
kiện hoặc giao lại. Nếu MVP chỉ cho phép một Shipment trên mỗi Seller
Order, có thể thêm `UNIQUE(seller_order_id)`.

### 5.13. `order_status_history`

Các cột đề xuất: `id UUID PK`, `order_id UUID FK NULL`,
`seller_order_id UUID FK NULL`, `old_status VARCHAR(40) NULL`,
`new_status VARCHAR(40) NOT NULL`, `changed_by UUID FK → users.id NULL`,
`note TEXT NULL`, `created_at TIMESTAMPTZ NOT NULL`.

Dùng để lưu vết thay đổi trạng thái. Cần quy định chỉ một trong
`order_id` hoặc `seller_order_id` được điền trên mỗi bản ghi, hoặc thiết
kế riêng hai bảng lịch sử nếu muốn ràng buộc chặt hơn.

## 6. Quan hệ và lực lượng

  -----------------------------------------------------------------------
  Quan hệ                 Cardinality             Diễn giải
  ----------------------- ----------------------- -----------------------
  User--Store             1 : 0..1                Một Seller có tối đa
                                                  một Store

  Store--Product          1 : N                   Một cửa hàng có nhiều
                                                  sản phẩm

  Category--Product       1 : N                   Một danh mục có nhiều
                                                  sản phẩm

  User--Address           1 : N                   Một người dùng có nhiều
                                                  địa chỉ

  User--Cart              1 : 0..1                Một Customer có tối đa
                                                  một giỏ đang hoạt động

  Cart--Cart Item         1 : N                   Giỏ chứa nhiều dòng
                                                  hàng

  User--Order             1 : N                   Customer có nhiều
                                                  Parent Order

  Order--Seller Order     1 : N                   Đơn tổng tách theo cửa
                                                  hàng

  Seller Order--Order     1 : N                   Đơn con chứa nhiều dòng
  Item                                            sản phẩm

  Order--Payment          1 : N                   Đơn có thể có nhiều
                                                  giao dịch liên quan

  Seller Order--Shipment  1 : N                   Đơn con có thể được
                                                  giao thành nhiều
                                                  Shipment
  -----------------------------------------------------------------------

## 7. Quy tắc toàn vẹn dữ liệu

-   Dùng UUID cho khóa chính để thuận tiện tạo định danh ở các tầng ứng
    dụng.
-   Dùng `NUMERIC(12,2)` cho tiền; không dùng kiểu số thực dấu phẩy
    động.
-   Dùng `TIMESTAMPTZ` cho thời điểm tạo/cập nhật.
-   Dùng `CHECK` cho giá, số lượng và các giá trị không âm.
-   Dùng khóa ngoại để bảo vệ tính toàn vẹn quan hệ.
-   Dùng `UNIQUE(seller_id)` trên `stores` để giới hạn một cửa hàng mỗi
    Seller.
-   Dùng `UNIQUE(order_id, store_id)` trên `seller_orders`.
-   Dùng transaction cho thao tác tạo đơn và cập nhật tồn kho.
-   Cân nhắc optimistic locking (`version`) hoặc câu lệnh cập nhật có
    điều kiện để chống tranh chấp tồn kho.
-   Không xóa cứng dữ liệu đơn hàng và thanh toán đã phát sinh; ưu tiên
    trạng thái, lưu vết và chính sách lưu trữ.
-   Chỉ backend tính tổng tiền cuối cùng; không tin giá trị tổng do
    client gửi lên.

## 8. Chỉ mục đề xuất ban đầu

-   `users(email)` --- unique index.
-   `stores(seller_id)` --- unique index.
-   `products(store_id, status)` --- lọc sản phẩm theo cửa hàng/trạng
    thái.
-   `products(category_id, status)` --- lọc theo danh mục.
-   `orders(customer_id, created_at DESC)` --- lịch sử đơn của khách.
-   `seller_orders(store_id, status, created_at DESC)` --- danh sách đơn
    cho Seller.
-   `seller_orders(order_id)` --- truy vấn đơn con của Parent Order.
-   `shipments(shipper_id, status)` --- danh sách giao hàng của Shipper.

Cần xác nhận bằng `EXPLAIN ANALYZE` và dữ liệu thực tế trước khi tối ưu
sâu.

## 9. Lưu ý triển khai

-   Tạo schema bằng Flyway migration, không chỉnh sửa schema thủ công
    trên môi trường dùng chung.
-   Ánh xạ Entity JPA theo từng module nghiệp vụ; tránh một Entity lớn
    chứa mọi quan hệ.
-   Không dùng cascade delete tùy tiện trên Order, Payment hoặc
    Shipment.
-   Xác định enum trạng thái ở domain/backend và lưu bằng chuỗi ổn định
    trong database.
-   Các bảng/số liệu liên quan đến tiền cần xác định quy tắc làm tròn và
    đơn vị tiền tệ.
-   Với `shipping_address_snapshot`, quy định rõ cấu trúc JSON và
    validate ở backend; nếu cần truy vấn địa chỉ sâu, cân nhắc các cột
    snapshot riêng.

## 10. Câu hỏi cần chốt trước khi tạo migration

-   [ ] Seller được kích hoạt tài khoản và cửa hàng theo cùng một quy
    trình hay hai bước?
-   [ ] Một sản phẩm chỉ thuộc một danh mục hay có nhiều danh mục?
-   [ ] Có biến thể sản phẩm (size, màu sắc) trong MVP không?
-   [ ] Tồn kho được giữ khi checkout hay trừ khi thanh toán thành công?
-   [ ] Một Seller Order có thể giao thành nhiều Shipment trong MVP
    không?
-   [ ] Thanh toán gắn với Parent Order hay từng Seller Order?
-   [ ] COD và hoàn tiền có nằm trong MVP không?
-   [ ] Có cần lưu lịch sử giá sản phẩm không?

## 11. Tiêu chí hoàn thành

-   [ ] ERD phản ánh đúng các quy trình nghiệp vụ.
-   [ ] Mọi bảng có mục đích và khóa chính rõ ràng.
-   [ ] Quan hệ, khóa ngoại, unique và check constraint được xác định.
-   [ ] Mô hình Parent Order/Seller Order đáp ứng checkout đa Seller.
-   [ ] Các quy tắc tồn kho, thanh toán và giao hàng được xác nhận.
-   [ ] Có thể chuyển thiết kế thành migration PostgreSQL và Entity JPA.

------------------------------------------------------------------------

**Trạng thái:** Bản nháp thiết kế. Đây là mô hình khởi đầu cho MVP; cần
chốt các câu hỏi ở mục 10 trước khi viết migration chính thức.
