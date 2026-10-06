# ShopSphere -- Các quyết định kỹ thuật

**Tài liệu:** `docs/04-architecture/technical-decisions.md`\
**Trạng thái:** Đề xuất\
**Dự án:** ShopSphere -- Nền tảng thương mại điện tử đa nhà bán hàng

## 1. Mục đích

Tài liệu này ghi lại các quyết định kỹ thuật chính của ShopSphere, bao
gồm phương án được chọn, lý do lựa chọn, ưu điểm, hạn chế và điều kiện
cần xem xét lại quyết định. Tài liệu bổ sung cho tài liệu kiến trúc hệ
thống, giúp nhóm hiểu và giải thích được *vì sao* hệ thống được thiết kế
theo cách hiện tại.

## 2. Tóm tắt các quyết định

  -----------------------------------------------------------------------
  Hạng mục                Quyết định              Lý do
  ----------------------- ----------------------- -----------------------
  Kiến trúc ứng dụng      Modular Monolith        Dễ phát triển và triển
                          (nguyên khối phân       khai ở giai đoạn đầu,
                          mô-đun)                 đồng thời vẫn duy trì
                                                  ranh giới rõ ràng giữa
                                                  các mô-đun.

  Tổ chức mã nguồn        Áp dụng nguyên tắc      Tách biệt nghiệp vụ
                          Clean Architecture      khỏi framework và chi
                          trong từng mô-đun       tiết hạ tầng.

  Ngôn ngữ backend        Java 21                 Phiên bản Java LTS hiện
                                                  đại, có hệ sinh thái
                                                  rộng và nhiều tính năng
                                                  ngôn ngữ hữu ích.

  Framework backend       Spring Boot 3.x         Hệ sinh thái trưởng
                                                  thành cho REST API,
                                                  dependency injection,
                                                  bảo mật và truy cập dữ
                                                  liệu.

  Công cụ build           Maven                   Quản lý thư viện và quy
                                                  trình build theo cách
                                                  phổ biến, được nhiều
                                                  IDE hỗ trợ.

  Truy cập dữ liệu        Spring Data JPA /       Giảm mã xử lý lưu trữ
                          Hibernate               lặp lại và hỗ trợ mô
                                                  hình dữ liệu quan hệ.

  Cơ sở dữ liệu           PostgreSQL              Hỗ trợ giao dịch, ràng
                                                  buộc dữ liệu, chỉ mục
                                                  và kiểu dữ liệu JSON.

  Quản lý thay đổi schema Flyway                  Theo dõi và áp dụng các
                                                  phiên bản thay đổi cơ
                                                  sở dữ liệu nhất quán
                                                  giữa các môi trường.

  Xác thực                Spring Security + JWT   Phù hợp với REST API có
                                                  frontend tách biệt và
                                                  không cần lưu session
                                                  trên server.

  Frontend                Vue 3 + TypeScript      Xây dựng giao diện theo
                                                  component, có kiểm tra
                                                  kiểu dữ liệu và hệ sinh
                                                  thái phát triển tốt.

  Kiểu API                REST qua HTTP, dữ liệu  Dễ hiểu, dễ kiểm thử và
                          JSON                    phù hợp với trình duyệt
                                                  cũng như các ứng dụng
                                                  khách khác.

  Môi trường phát triển   Docker Compose          Giúp nhóm khởi chạy các
                                                  dịch vụ phụ trợ trong
                                                  môi trường phát triển
                                                  một cách nhất quán.

  Triển khai ban đầu      Một ứng dụng backend    Tránh độ phức tạp không
                          duy nhất                cần thiết của hệ thống
                                                  phân tán khi dự án còn
                                                  ở giai đoạn MVP.
  -----------------------------------------------------------------------

## 3. Các quyết định về kiến trúc

### ADR-001: Sử dụng Modular Monolith

**Trạng thái:** Đã chấp nhận cho MVP

**Bối cảnh**\
ShopSphere có nhiều mảng nghiệp vụ như tài khoản, danh mục sản phẩm, giỏ
hàng, checkout, đơn hàng, thanh toán và vận chuyển. Các mảng này cần
được phân chia rõ ràng, nhưng quy mô ban đầu chưa đủ lớn để cần triển
khai từng dịch vụ độc lập.

**Quyết định**\
Xây dựng một ứng dụng backend có thể triển khai độc lập, bên trong được
chia thành các mô-đun nghiệp vụ. Mỗi mô-đun quản lý logic nghiệp vụ của
mình và cung cấp giao diện rõ ràng để mô-đun khác sử dụng.

**Hệ quả** - Một ứng dụng duy nhất giúp chạy, gỡ lỗi, kiểm thử và triển
khai đơn giản hơn. - Có thể phát triển và tìm hiểu từng mô-đun riêng nếu
tuân thủ ranh giới đã thiết kế. - Một mô-đun không được truy cập trực
tiếp repository hoặc entity nội bộ của mô-đun khác. - Có thể xem xét
tách mô-đun thành dịch vụ riêng trong tương lai, nhưng việc tách không
tự động hay hoàn toàn dễ dàng.

**Khi nào cần xem xét lại** - Một mô-đun có nhu cầu mở rộng quy mô hoặc
tính sẵn sàng khác biệt đáng kể. - Việc triển khai độc lập trở thành yêu
cầu thực tế. - Quy mô nhóm và năng lực vận hành đủ để quản lý hệ thống
phân tán.

### ADR-002: Áp dụng nguyên tắc Clean Architecture trong từng mô-đun

**Trạng thái:** Đã chấp nhận

**Bối cảnh**\
Các quy tắc nghiệp vụ như checkout nhiều nhà bán hàng, chia đơn, kiểm
tra quyền sở hữu của Seller và phân công vận chuyển không nên phụ thuộc
trực tiếp vào framework web hoặc công nghệ cơ sở dữ liệu.

**Quyết định**\
Tổ chức từng mô-đun theo hướng nghiệp vụ và tầng ứng dụng nằm bên trong;
tầng hạ tầng và API phụ thuộc vào các tầng bên trong. Tuân thủ quy tắc
phụ thuộc: **tầng bên trong không được phụ thuộc vào tầng bên ngoài**.

**Hệ quả** - Có thể kiểm thử quy tắc nghiệp vụ mà không cần khởi chạy
toàn bộ ứng dụng. - Chi tiết framework và lưu trữ dữ liệu được giữ ở các
ranh giới. - Cần thêm một số interface và mã chuyển đổi dữ liệu, làm
tăng công sức ban đầu. - Không tạo abstraction hoặc interface nếu chúng
không giải quyết vấn đề thực tế.

**Khi nào cần xem xét lại**\
Nếu một tầng hoặc abstraction làm tăng độ phức tạp nhiều hơn lợi ích
mang lại, hãy đơn giản hóa nhưng vẫn giữ hướng phụ thuộc và khả năng
kiểm thử.

### ADR-003: Chưa sử dụng Microservices trong MVP

**Trạng thái:** Đã chấp nhận

**Bối cảnh**\
Microservices kéo theo nhiều vấn đề vận hành như giao tiếp qua mạng,
khám phá dịch vụ, theo dõi phân tán, phối hợp triển khai và đảm bảo nhất
quán dữ liệu giữa các dịch vụ.

**Quyết định**\
Giữ backend trong một ứng dụng có thể triển khai duy nhất, đồng thời
thiết kế ranh giới mô-đun rõ ràng.

**Hệ quả** - Phát triển cục bộ và tích hợp liên tục đơn giản hơn. - Có
thể sử dụng giao dịch cơ sở dữ liệu trong phạm vi ứng dụng khi phù
hợp. - Cần kiểm soát ranh giới mô-đun thông qua review mã nguồn và kiểm
thử. - Ban đầu mở rộng quy mô ở cấp ứng dụng, chưa mở rộng riêng từng
mô-đun.

**Khi nào cần xem xét lại**\
Khi tải thực tế, cách tổ chức nhóm hoặc yêu cầu phát hành độc lập chứng
minh rằng việc tách một mô-đun là cần thiết.

### ADR-004: Sử dụng Java 21 và Spring Boot 3.x

**Trạng thái:** Đã chấp nhận

**Bối cảnh**\
Dự án hướng đến việc thể hiện năng lực phát triển backend Java và xây
dựng REST API an toàn, dễ bảo trì.

**Quyết định**\
Sử dụng Java 21 kết hợp Spring Boot 3.x.

**Hệ quả** - Có thể sử dụng các tính năng hiện đại của Java và môi
trường chạy mới. - Hệ sinh thái hỗ trợ tốt cho dependency injection,
validation, security, testing và data access. - Cần chọn phiên bản thư
viện tương thích và sử dụng JDK được hỗ trợ. - Người phát triển cần hiểu
cấu hình Spring và dependency injection, không chỉ dựa vào mã được sinh
tự động.

### ADR-005: Sử dụng PostgreSQL, Spring Data JPA và Flyway

**Trạng thái:** Đã chấp nhận

**Bối cảnh**\
ShopSphere có dữ liệu quan hệ và các yêu cầu toàn vẹn như mỗi Seller tối
đa một Store, Product thuộc Store, Order Item thuộc Seller Order, và
Payment/Shipment liên kết với đơn hàng.

**Quyết định**\
Sử dụng PostgreSQL làm cơ sở dữ liệu quan hệ, Spring Data JPA/Hibernate
để ánh xạ đối tượng với dữ liệu và Flyway để quản lý phiên bản schema.

**Hệ quả** - Foreign key, unique constraint, transaction và index giúp
bảo vệ tính toàn vẹn dữ liệu. - JPA giảm mã lặp nhưng cần chú ý lazy
loading, hiệu năng truy vấn và ranh giới transaction. - Thay đổi
database được ghi thành migration file; không chỉnh sửa thủ công ở môi
trường dùng chung. - Nên bảo vệ các quy tắc nghiệp vụ quan trọng bằng cả
logic ứng dụng và ràng buộc database khi khả thi.

**Khi nào cần xem xét lại**\
Chỉ đổi công nghệ lưu trữ khi có yêu cầu hoặc tải thực tế chứng minh sự
cần thiết; không thêm cơ sở dữ liệu thứ hai nếu chưa có lý do rõ ràng.

### ADR-006: Sử dụng REST API và JSON

**Trạng thái:** Đã chấp nhận

**Bối cảnh**\
Backend phục vụ frontend Vue riêng biệt và có thể được sử dụng bởi các
client khác trong tương lai.

**Quyết định**\
Cung cấp REST endpoint có phiên bản, sử dụng JSON, kèm cơ chế validation
và định dạng lỗi nhất quán.

**Hệ quả** - API dễ quan sát và kiểm thử bằng Swagger/OpenAPI. - DTO
giúp tách hợp đồng API công khai khỏi entity nội bộ. - Cần cân nhắc tính
tương thích khi thay đổi hành vi endpoint hoặc cấu trúc response. -
Không trả trực tiếp JPA entity ra API.

### ADR-007: Sử dụng Spring Security và JWT

**Trạng thái:** Đã chấp nhận cho API ban đầu

**Bối cảnh**\
ShopSphere có quyền hạn khác nhau cho Customer, Seller, Admin và
Shipper.

**Quyết định**\
Dùng Spring Security để xác thực và phân quyền, kết hợp JWT access token
cho các request API. Backend phải kiểm tra quyền truy cập, bao gồm cả
quyền sở hữu tài nguyên.

**Hệ quả** - API có thể xác thực request mà không cần lưu session phía
server. - Chỉ kiểm tra role là chưa đủ: cần xác minh Seller có thực sự
sở hữu Store, Product hoặc Order đang truy cập hay không. - Trước khi
dùng thực tế cần thiết kế thời hạn token, quản lý signing key, refresh
token và lưu trữ an toàn. - Các thao tác nhạy cảm phải có kiểm thử phân
quyền cụ thể.

**Khi nào cần xem xét lại**\
Khi hệ thống cần định danh tập trung, single sign-on hoặc vòng đời
session/token phức tạp hơn.

### ADR-008: Sử dụng Vue 3 và TypeScript cho frontend

**Trạng thái:** Đã chấp nhận

**Bối cảnh**\
Ứng dụng cần các luồng mua sắm cho khách hàng và giao diện vận hành
riêng theo vai trò.

**Quyết định**\
Dùng Vue 3 với TypeScript, triển khai frontend riêng và giao tiếp với
backend qua REST API.

**Hệ quả** - Có thể tổ chức component và màn hình theo vai trò. -
TypeScript phát hiện nhiều lỗi kiểu dữ liệu và hợp đồng API ngay trong
quá trình phát triển. - Route guard giúp điều hướng giao diện nhưng
không thay thế cơ chế phân quyền backend. - Hợp đồng API cần được ghi
tài liệu và đồng bộ với backend.

### ADR-009: Sử dụng Docker Compose cho môi trường phát triển

**Trạng thái:** Đã chấp nhận cho phát triển

**Bối cảnh**\
Nhóm cần cách chạy backend và các dịch vụ phụ trợ, đặc biệt là
PostgreSQL, một cách nhất quán.

**Quyết định**\
Cung cấp cấu hình Docker Compose cho các dịch vụ phát triển cục bộ và
tài liệu hóa các biến môi trường.

**Hệ quả** - Thành viên nhóm có thể khởi chạy môi trường phát triển
tương tự nhau. - Không commit mật khẩu, secret hoặc signing key vào
repository. - Môi trường production có thể dùng nền tảng khác và cần tài
liệu triển khai riêng.

## 4. Quy tắc kỹ thuật xuyên suốt

1.  Đặt logic nghiệp vụ trong domain/application, không đặt trong
    controller hoặc entity lưu trữ.
2.  Controller xử lý các mối quan tâm HTTP như nhận request, validation,
    status code và chuyển đổi response.
3.  Dùng DTO ở ranh giới API; không trả trực tiếp entity lưu trữ.
4.  Phân quyền ở backend, bao gồm kiểm tra role và quyền sở hữu tài
    nguyên.
5.  Dùng ràng buộc database cho các quy tắc toàn vẹn lâu dài, ví dụ mỗi
    Seller chỉ có tối đa một Store.
6.  Xác định rõ transaction cho các nghiệp vụ quan trọng như checkout và
    tạo đơn.
7.  Tránh distributed transaction trong MVP; dùng transaction cục bộ rõ
    ràng và quản lý trạng thái thanh toán/vận chuyển minh bạch.
8.  Lưu lịch sử các thay đổi trạng thái đơn hàng trong bảng
    audit/history.
9.  Viết test cho quy tắc nghiệp vụ, đặc biệt là checkout nhiều Seller,
    cô lập dữ liệu Seller, hủy đơn và phân công vận chuyển.
10. Tách cấu hình theo môi trường và không commit thông tin xác thực.

## 5. Đánh đổi và rủi ro đã biết

  -----------------------------------------------------------------------
  Quyết định        Lợi ích           Đánh đổi / Rủi ro Cách giảm thiểu
  ----------------- ----------------- ----------------- -----------------
  Modular Monolith  Triển khai đơn    Các mô-đun có thể Quy định API
                    giản, hỗ trợ      phụ thuộc chặt    mô-đun và kiểm
                    transaction nội   vào nhau theo     soát hướng phụ
                    bộ                thời gian         thuộc

  Clean             Logic nghiệp vụ   Tăng cấu trúc và  Chỉ tạo
  Architecture      dễ kiểm thử       mã mapping        abstraction khi
                                                        có mục đích rõ
                                                        ràng

  JPA/Hibernate     Tăng tốc phát     N+1 query và truy Theo dõi SQL,
                    triển với dữ liệu vấn khó nhận biết chọn chiến lược
                    quan hệ                             fetch phù hợp,
                                                        kiểm thử truy vấn

  JWT               Xác thực API      Thu hồi token và  Access token ngắn
                    không cần session refresh cần thiết hạn và quy trình
                    phía server       kế cẩn thận       refresh được tài
                                                        liệu hóa

  Một backend triển Giảm chi phí vận  Không thể mở rộng Đo điểm nghẽn
  khai              hành              riêng từng mô-đun trước khi tách
                                                        dịch vụ

  Phạm vi MVP       Tập trung hoàn    Một số trường hợp Ghi rõ phần chưa
                    thành chức năng   marketplace nâng  làm và câu hỏi
                    cốt lõi           cao bị hoãn       còn mở
  -----------------------------------------------------------------------

## 6. Các quyết định chưa chốt

Những nội dung sau sẽ được quyết định khi yêu cầu và chi tiết triển khai
rõ hơn:

-   Nhà cung cấp thanh toán và cách xác minh webhook.
-   Checkout có giữ tồn kho tạm thời hay không và thời hạn giữ hàng.
-   Tích hợp đơn vị vận chuyển và nguồn dữ liệu tracking.
-   Chính sách đổi trả và hoàn tiền.
-   Cách lưu trữ, làm mới và thu hồi JWT refresh token.
-   Nền tảng production, CI/CD, giám sát hệ thống và sao lưu.
-   Thông báo được xử lý đồng bộ hay qua job/outbox bất đồng bộ.
-   Quy ước versioning API và phân trang.

## 7. Danh sách kiểm tra khi xem xét thay đổi quyết định

Trước khi thay đổi một quyết định, cần trả lời:

-   Yêu cầu cụ thể hoặc vấn đề đã đo lường nào khiến cần thay đổi?
-   Những mô-đun và luồng nghiệp vụ nào bị ảnh hưởng?
-   Thay đổi có làm yếu đi tính toàn vẹn dữ liệu, phân quyền hoặc khả
    năng kiểm thử không?
-   Cần migration hoặc xử lý tương thích nào?
-   Có thể triển khai thay đổi theo từng bước không?
-   Bằng chứng nào cho thấy phương án mới phù hợp hơn với dự án?

## 8. Bước tài liệu tiếp theo

Sau khi rà soát các quyết định trên, bước tiếp theo là xác định hợp đồng
API công khai tại:

`docs/05-api/api-specification.md`

Tài liệu API nên bao gồm xác thực, danh mục sản phẩm, giỏ hàng,
checkout, Parent Order, Seller Order, thanh toán, vận chuyển,
validation, định dạng lỗi, phân trang và quy tắc phân quyền.
