# ShopSphere --- System Architecture

> **Tài liệu:** Kiến trúc hệ thống\
> **Phiên bản:** 0.1\
> **Trạng thái:** Bản nháp\
> **Ngày cập nhật:** 01/10/2026\
> **Cơ sở:** Requirements Analysis, Business Workflows, Database Design

## 1. Mục đích

Tài liệu mô tả kiến trúc tổng thể, ranh giới module, các tầng trong
backend, công nghệ và phương án triển khai ShopSphere. Mục tiêu là xây
dựng nền tảng dễ phát triển, kiểm thử và bảo trì trong phạm vi một dự án
portfolio.

## 2. Tổng quan hệ thống

ShopSphere là nền tảng thương mại điện tử multi-vendor. Một Seller sở
hữu tối đa một Store và Store có thể bán nhiều Product. Customer có thể
checkout sản phẩm từ nhiều Store trong một lần đặt hàng; hệ thống tạo
Parent Order và các Seller Orders tương ứng.

### 2.1. Sơ đồ ngữ cảnh

``` mermaid
flowchart LR
    C[Customer] --> FE[Vue Web App]
    S[Seller] --> FE
    A[Admin] --> FE
    D[Shipper] --> FE
    FE --> API[Spring Boot REST API]
    API --> DB[(PostgreSQL)]
    API --> PG[Payment Provider]
    API --> MSG[Email / Notification Provider]
    API --> OBJ[Object Storage]
```

## 3. Phong cách kiến trúc

### 3.1. Modular Monolith

ShopSphere bắt đầu bằng một backend deploy như một ứng dụng Spring Boot
duy nhất, nhưng được chia thành các module nghiệp vụ có ranh giới rõ
ràng.

**Lý do lựa chọn:** - Phù hợp quy mô dự án portfolio và nhóm phát triển
nhỏ. - Dễ chạy, debug, kiểm thử và triển khai hơn microservices. - Giao
dịch nghiệp vụ như tạo đơn và cập nhật tồn kho có thể được xử lý trong
cùng ứng dụng/database. - Ranh giới module giúp hạn chế phụ thuộc chéo
và có thể hỗ trợ tách dịch vụ sau này nếu có nhu cầu thực tế.

**Đánh đổi:** các module cùng chia sẻ tiến trình triển khai và có thể
dùng chung database; cần quy định dependency rõ ràng để tránh biến
monolith thành một khối code kết dính.

### 3.2. Clean Architecture

Trong từng module, tổ chức code theo hướng domain/application ở lõi,
infrastructure ở ngoài và web/API là điểm vào.

``` mermaid
flowchart TB
    WEB[Web / REST Controllers] --> APP[Application Use Cases]
    APP --> DOM[Domain Model and Business Rules]
    INF[Infrastructure: JPA, Providers, Messaging] -. implements ports .-> APP
    APP -. depends on abstractions .-> DOM
```

Quy tắc phụ thuộc: - Domain không phụ thuộc Spring MVC, JPA, database
hoặc nhà cung cấp bên ngoài. - Application điều phối use case và định
nghĩa các port cần thiết. - Infrastructure triển khai port như
repository, payment gateway, email sender. - Web adapter nhận HTTP
request, validate đầu vào cơ bản và gọi application use case. - Không để
module nghiệp vụ truy cập trực tiếp bảng của module khác.

## 4. Thiết kế module backend

  -----------------------------------------------------------------------
  Module                  Trách nhiệm             Dữ liệu sở hữu chính
  ----------------------- ----------------------- -----------------------
  `identity`              Đăng nhập, tài khoản,   User, Role, UserRole
                          vai trò, xác thực       

  `seller`                Hồ sơ Seller, đăng ký   Store, Seller profile
                          và trạng thái cửa hàng  

  `catalog`               Danh mục, sản phẩm, giá Category, Product
                          hiển thị                

  `cart`                  Giỏ hàng và dòng hàng   Cart, CartItem

  `order`                 Checkout, Parent Order, Order, SellerOrder,
                          Seller Order, Order     OrderItem, OrderHistory
                          Item, trạng thái đơn    

  `payment`               Khởi tạo và xác minh    Payment, Refund records
                          giao dịch, hoàn tiền    nếu cần

  `shipping`              Shipment, phân công     Shipment
                          Shipper, tiến trình     
                          giao                    

  `notification`          Gửi email/thông báo và  Notification/Outbox nếu
                          ghi nhận kết quả        triển khai
  -----------------------------------------------------------------------

### 4.1. Ranh giới module

-   `identity` là nguồn dữ liệu chuẩn về tài khoản và vai trò.
-   `seller` sở hữu dữ liệu cửa hàng; `catalog` tham chiếu định danh
    Store qua ID, không sửa trực tiếp Store.
-   `catalog` sở hữu sản phẩm và danh mục.
-   `cart` lưu lựa chọn tạm thời; `order` xác minh lại giá và tồn kho
    khi checkout.
-   `order` sở hữu vòng đời đơn hàng và điều phối checkout.
-   `payment` sở hữu trạng thái giao dịch; `order` không tự giả lập kết
    quả thanh toán.
-   `shipping` sở hữu tiến trình giao hàng; `order` nhận các sự kiện/kết
    quả cần thiết để tổng hợp trạng thái đơn.
-   `notification` nhận yêu cầu gửi thông báo từ các module, không quyết
    định nghiệp vụ đặt hàng.

## 5. Quy tắc phụ thuộc giữa module

``` mermaid
flowchart TD
    ID[identity]
    SEL[seller]
    CAT[catalog]
    CART[cart]
    ORD[order]
    PAY[payment]
    SHIP[shipping]
    NOTI[notification]

    SEL --> ID
    CAT --> SEL
    CART --> CAT
    ORD --> ID
    ORD --> CAT
    ORD --> CART
    ORD --> PAY
    ORD --> SHIP
    ORD --> NOTI
    PAY --> NOTI
    SHIP --> NOTI
```

Sơ đồ thể hiện các quan hệ cần thiết ở mức khái niệm, không phải lời gọi
trực tiếp bắt buộc. Nên dùng application ports, sự kiện nội bộ hoặc
interface công khai để giảm coupling. Tránh dependency vòng.

## 6. Cấu trúc tầng trong một module

Ví dụ module `order`:

``` text
order/
├── domain/
│   ├── model/
│   ├── valueobject/
│   ├── event/
│   ├── repository/
│   └── service/
├── application/
│   ├── command/
│   ├── query/
│   ├── dto/
│   ├── port/
│   └── usecase/
├── infrastructure/
│   ├── persistence/
│   ├── repository/
│   └── adapter/
└── web/
    ├── controller/
    ├── request/
    └── response/
```

Ý nghĩa: - **Domain:** quy tắc nghiệp vụ cốt lõi, entity và value
object. - **Application:** use case, điều phối giao dịch, kiểm tra luồng
nghiệp vụ và gọi port. - **Infrastructure:** Spring Data JPA, triển khai
repository, tích hợp dịch vụ ngoài. - **Web:** REST controller,
request/response DTO và ánh xạ lỗi HTTP.

Đây là cấu trúc tham khảo; chỉ tạo package khi có chức năng thực sự,
tránh tạo quá nhiều lớp rỗng.

## 7. Luồng xử lý checkout

``` mermaid
sequenceDiagram
    actor Customer
    participant Web as OrderController
    participant App as CheckoutUseCase
    participant Catalog as Catalog Port
    participant Cart as Cart Port
    participant DB as PostgreSQL
    participant Payment as Payment Port

    Customer->>Web: POST /orders
    Web->>App: Checkout command
    App->>Cart: Load cart
    Cart-->>App: Cart items
    App->>Catalog: Validate products, prices, stock
    Catalog-->>App: Validated item data
    App->>DB: Begin transaction
    App->>DB: Create Parent Order + Seller Orders + Items
    App->>DB: Reserve/decrement stock per policy
    App->>DB: Commit transaction
    App->>Payment: Initiate payment (if online)
    Payment-->>App: Payment initiation result
    App-->>Web: Order result
    Web-->>Customer: Order number and status
```

**Lưu ý thiết kế:** tích hợp thanh toán bên ngoài thường không nên nằm
trong transaction database kéo dài. Cần thiết kế trạng thái trung gian,
idempotency và cơ chế xử lý khi tạo đơn thành công nhưng khởi tạo thanh
toán thất bại. Luồng thực tế có thể dùng outbox hoặc retry có kiểm soát.

## 8. Công nghệ đề xuất

  -----------------------------------------------------------------------
  Thành phần              Công nghệ               Vai trò
  ----------------------- ----------------------- -----------------------
  Backend                 Java 21, Spring Boot    REST API và nghiệp vụ
                          3.x                     

  Build                   Maven                   Quản lý dependency,
                                                  build

  Persistence             Spring Data JPA,        ORM và truy cập dữ liệu
                          Hibernate               

  Database                PostgreSQL              Lưu dữ liệu quan hệ

  Migration               Flyway                  Quản lý phiên bản
                                                  schema

  Security                Spring Security, JWT    Xác thực và phân quyền

  Validation              Jakarta Bean Validation Kiểm tra dữ liệu đầu
                                                  vào

  API Docs                OpenAPI / Swagger       Tài liệu và thử API

  Testing                 JUnit 5, Mockito,       Unit và integration
                          Testcontainers          test

  Frontend                Vue 3, TypeScript, Vite Giao diện web

  Frontend state/routing  Pinia, Vue Router       Trạng thái và điều
                                                  hướng

  Styling                 Tailwind CSS            Giao diện

  HTTP client             Axios                   Gọi REST API

  Deployment              Docker Compose, Nginx   Môi trường phát
                                                  triển/triển khai ban
                                                  đầu
  -----------------------------------------------------------------------

Redis, RabbitMQ và hệ thống tìm kiếm chuyên dụng là lựa chọn mở rộng,
không bắt buộc trong MVP.

## 9. Thiết kế bảo mật

-   Mật khẩu lưu dưới dạng hash bằng cơ chế phù hợp, không lưu
    plaintext.
-   Dùng Spring Security để xác thực và bảo vệ endpoint.
-   Dùng role-based access control cho các nhóm quyền Customer, Seller,
    Admin, Shipper.
-   Kiểm tra quyền sở hữu tài nguyên ở backend: Seller chỉ thao tác
    Store/Product/Seller Order của mình; Customer chỉ xem đơn của mình;
    Shipper chỉ cập nhật Shipment được phân công.
-   Không tin role, giá, tổng tiền, user ID hoặc trạng thái nhạy cảm do
    frontend tự gửi.
-   Xác minh chữ ký và dữ liệu callback từ nhà cung cấp thanh toán.
-   Không ghi token, mật khẩu, thông tin thanh toán nhạy cảm vào log.
-   Dùng HTTPS ở môi trường triển khai và quản lý secrets qua biến môi
    trường/secret store.

## 10. Xử lý lỗi và tính nhất quán

-   Dùng một định dạng lỗi API thống nhất, có mã lỗi nghiệp vụ và thông
    điệp an toàn.
-   Dùng transaction cho các thay đổi dữ liệu cần nguyên tử, đặc biệt
    tạo đơn và cập nhật tồn kho.
-   Chống đặt vượt tồn kho bằng cập nhật có điều kiện, khóa phù hợp hoặc
    optimistic locking.
-   Dùng idempotency cho thao tác tạo đơn/thanh toán có khả năng gửi
    lại.
-   Tách trạng thái Order, Payment và Shipment.
-   Ghi lịch sử thay đổi trạng thái quan trọng.
-   Với tác vụ gửi email hoặc xử lý bất đồng bộ, cân nhắc transactional
    outbox để tránh mất sự kiện sau khi commit.

## 11. Triển khai mức cao

``` mermaid
flowchart TB
    B[Browser] --> N[Nginx / Reverse Proxy]
    N --> FE[Vue Static Web App]
    N --> API[Spring Boot Application]
    API --> DB[(PostgreSQL)]
    API --> EXT[External Payment / Email Services]
    API --> STORE[Object Storage]
```

Môi trường local có thể chạy bằng Docker Compose: - `frontend` -
`backend` - `postgres` - tùy chọn `redis` khi có use case rõ ràng

Cấu hình môi trường tách biệt; không commit secrets hoặc dữ liệu thật
vào repository.

## 12. Yêu cầu vận hành và chất lượng

-   Có health endpoint và log có cấu trúc.
-   Có migration database được version control.
-   Có unit test cho domain/application và integration test cho
    repository/API quan trọng.
-   Có phân trang cho danh sách lớn.
-   Có giới hạn kích thước upload và xác thực loại file nếu hỗ trợ ảnh
    sản phẩm.
-   Có backup database theo môi trường triển khai.
-   Theo dõi lỗi thanh toán, lỗi giao hàng và các thao tác quản trị quan
    trọng.

## 13. Các quyết định kiến trúc cần ghi nhận

  -----------------------------------------------------------------------
  ID                Quyết định        Lý do             Đánh đổi
  ----------------- ----------------- ----------------- -----------------
  ADR-001           Modular Monolith  Đơn giản để phát  Cần kỷ luật
                                      triển và triển    dependency
                                      khai ban đầu, vẫn 
                                      có ranh giới      
                                      module            

  ADR-002           Clean             Tách nghiệp vụ    Có thêm
                    Architecture      khỏi framework và abstraction và
                    trong module      hạ tầng           cấu trúc

  ADR-003           PostgreSQL + JPA  Dữ liệu quan hệ,  Cần chú ý truy
                                      transaction và hệ vấn và mapping
                                      sinh thái Java    
                                      phù hợp           

  ADR-004           Parent Order +    Hỗ trợ checkout   Trạng thái/tổng
                    Seller Orders     nhiều cửa hàng và tiền phức tạp hơn
                                      xử lý độc lập     
                                      theo Seller       

  ADR-005           Một Store tối đa  Phù hợp quy tắc   Nếu thay đổi sau
                    cho mỗi Seller    nghiệp vụ đã xác  này cần migration
                                      nhận              
  -----------------------------------------------------------------------

## 14. Các điểm cần xác nhận

-   [ ] Chốt cách giữ/trừ tồn kho khi checkout.
-   [ ] Chốt thanh toán gắn với Parent Order hay Seller Order.
-   [ ] Chốt chính sách một hay nhiều Shipment cho mỗi Seller Order.
-   [ ] Chốt cơ chế giao tiếp module: gọi application port đồng bộ,
    domain event nội bộ hay kết hợp.
-   [ ] Chốt có triển khai Redis/outbox trong MVP hay để giai đoạn sau.
-   [ ] Chốt môi trường triển khai đích và yêu cầu backup/monitoring.

## 15. Tiêu chí hoàn thành

-   [ ] Sơ đồ kiến trúc tổng thể và deployment rõ ràng.
-   [ ] Ranh giới và trách nhiệm module được mô tả.
-   [ ] Quy tắc phụ thuộc không tạo vòng lặp.
-   [ ] Cấu trúc tầng backend và luồng checkout được giải thích.
-   [ ] Công nghệ được liệt kê kèm vai trò.
-   [ ] Các yêu cầu bảo mật, nhất quán và vận hành được đề cập.
-   [ ] Các quyết định và câu hỏi còn mở được ghi lại.

------------------------------------------------------------------------

**Trạng thái:** Bản nháp kiến trúc cho MVP. Cần chốt các quyết định còn
mở trước khi triển khai chi tiết.
