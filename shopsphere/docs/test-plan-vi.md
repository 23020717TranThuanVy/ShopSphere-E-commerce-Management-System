# ShopSphere -- Kế hoạch kiểm thử

**Tài liệu:** `docs/06-testing/test-plan.md`\
**Trạng thái:** Bản kế hoạch ban đầu\
**Dự án:** ShopSphere -- Nền tảng thương mại điện tử đa nhà bán hàng

## 1. Mục đích

Tài liệu này xác định chiến lược, phạm vi, môi trường và các kịch bản
kiểm thử cho ShopSphere. Mục tiêu là phát hiện lỗi sớm, bảo vệ các quy
tắc nghiệp vụ quan trọng và đảm bảo các mô-đun hoạt động đúng khi tích
hợp.

Đặc biệt, kiểm thử phải chứng minh được các quy tắc cốt lõi của
marketplace: một Seller sở hữu tối đa một Store, sản phẩm thuộc đúng
Store, một checkout có thể tạo nhiều Seller Order, và người dùng chỉ
được truy cập tài nguyên mà họ có quyền.

## 2. Mục tiêu kiểm thử

-   Xác minh chức năng theo yêu cầu và đặc tả API.
-   Kiểm tra tính toàn vẹn dữ liệu và các ràng buộc quan hệ.
-   Kiểm tra phân quyền theo vai trò và quyền sở hữu tài nguyên.
-   Kiểm tra các luồng nghiệp vụ từ đầu đến cuối.
-   Phát hiện lỗi về transaction, trạng thái đơn hàng, tồn kho và xử lý
    request lặp.
-   Đảm bảo API trả mã HTTP, dữ liệu và lỗi theo hợp đồng đã thống nhất.
-   Duy trì khả năng hồi quy khi thêm chức năng hoặc sửa lỗi.

## 3. Phạm vi

### 3.1 Trong phạm vi MVP

1.  Đăng ký, đăng nhập và quản lý hồ sơ.
2.  Phân quyền Customer, Seller, Admin và Shipper.
3.  Tạo và quản lý Store của Seller.
4.  Quản lý danh mục và sản phẩm.
5.  Tìm kiếm, lọc và xem sản phẩm.
6.  Giỏ hàng và địa chỉ giao hàng.
7.  Checkout đa nhà bán hàng.
8.  Parent Order và Seller Order.
9.  Thanh toán COD và luồng thanh toán trực tuyến ở mức tích hợp được
    chọn.
10. Quản lý shipment và phân công Shipper.
11. Hủy đơn, đổi trả/hoàn tiền theo chính sách MVP được chốt.
12. Nhật ký trạng thái và các thao tác quản trị cần thiết.

### 3.2 Ngoài phạm vi ban đầu

-   Kiểm thử tải quy mô lớn trên hạ tầng production.
-   Kiểm thử xâm nhập chuyên sâu bởi đơn vị độc lập.
-   Tích hợp mọi nhà cung cấp thanh toán và vận chuyển.
-   Các tính năng marketplace nâng cao chưa được xác nhận trong yêu cầu.

Các nội dung ngoài phạm vi có thể được bổ sung khi dự án mở rộng.

## 4. Chiến lược kiểm thử

  -----------------------------------------------------------------------
  Loại kiểm thử           Mục tiêu                Công cụ/Phương pháp dự
                                                  kiến
  ----------------------- ----------------------- -----------------------
  Unit test               Kiểm tra logic nhỏ, độc JUnit 5, Mockito
                          lập                     

  Integration test        Kiểm tra tương tác giữa Spring Boot Test,
                          service, repository và  Testcontainers
                          database                

  API test                Kiểm tra                MockMvc hoặc REST
                          request/response,       Assured
                          status code, validation 

  Security test           Kiểm tra xác thực, vai  Spring Security Test,
                          trò và quyền sở hữu     API integration tests

  End-to-end test         Kiểm tra luồng người    Công cụ E2E frontend
                          dùng xuyên suốt         được chọn sau

  Regression test         Đảm bảo chức năng cũ    Bộ test tự động chạy
                          không bị hỏng           trong CI

  Manual exploratory test Tìm lỗi luồng giao diện Checklist và test
                          và tình huống chưa dự   session
                          liệu                    

  Performance test        Đánh giá thời gian phản Công cụ được chọn khi
                          hồi và điểm nghẽn cơ    có mục tiêu tải
                          bản                     
  -----------------------------------------------------------------------

## 5. Môi trường và dữ liệu kiểm thử

### 5.1 Môi trường

-   **Local:** backend, frontend và PostgreSQL chạy cục bộ; có thể dùng
    Docker Compose.
-   **Test/CI:** môi trường tự động, dữ liệu tách biệt, có thể tạo mới
    và làm sạch.
-   **Staging (nếu có):** gần với cấu hình triển khai thực tế, dùng tài
    khoản và dữ liệu giả lập.

Không dùng dữ liệu cá nhân hoặc thông tin thanh toán thật trong bộ kiểm
thử.

### 5.2 Bộ dữ liệu mẫu

  -----------------------------------------------------------------------
  Đối tượng                           Dữ liệu mẫu
  ----------------------------------- -----------------------------------
  Customer                            `customerA`, `customerB`

  Seller                              `sellerA`, `sellerB`

  Admin                               `adminA`

  Shipper                             `shipperA`, `shipperB`

  Store                               `storeA` thuộc `sellerA`; `storeB`
                                      thuộc `sellerB`

  Product                             `productA1`, `productA2` thuộc
                                      `storeA`; `productB1` thuộc
                                      `storeB`

  Inventory                           Sản phẩm còn hàng, sắp hết hàng và
                                      hết hàng

  Orders                              Đơn mới, đang xử lý, đã bàn giao,
                                      hoàn tất và đã hủy

  Shipments                           Chưa phân công, đã phân công, đang
                                      giao và giao thất bại
  -----------------------------------------------------------------------

Mật khẩu và token trong test phải là dữ liệu giả, không tái sử dụng
thông tin thật.

## 6. Quy ước mã test và mức độ ưu tiên

-   `AUTH`: xác thực và tài khoản.
-   `AUTHZ`: phân quyền.
-   `STORE`: cửa hàng.
-   `CAT`: danh mục và sản phẩm.
-   `CART`: giỏ hàng.
-   `CHK`: checkout.
-   `ORD`: đơn hàng.
-   `PAY`: thanh toán.
-   `SHP`: vận chuyển.
-   `ADM`: quản trị.
-   `SYS`: hệ thống và hợp đồng API.

Mức ưu tiên: - **P0 -- Critical:** lỗi làm sai dữ liệu tiền/đơn, lộ dữ
liệu hoặc phá vỡ quy tắc phân quyền; phải chặn phát hành. - **P1 --
High:** luồng chính không hoạt động hoặc nghiệp vụ cốt lõi sai. - **P2
-- Medium:** chức năng phụ hoặc tình huống ít phổ biến bị lỗi. - **P3 --
Low:** vấn đề nhỏ về trải nghiệm hoặc thông báo, không ảnh hưởng dữ
liệu/nghiệp vụ chính.

## 7. Danh sách kịch bản kiểm thử

### 7.1 Xác thực và tài khoản

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  AUTH-01           Đăng ký với email Tạo tài khoản     P1
                    hợp lệ và mật     Customer thành    
                    khẩu đạt chính    công              
                    sách                                

  AUTH-02           Đăng ký bằng      Từ chối, trả lỗi  P1
                    email đã tồn tại  phù hợp; không    
                                      tạo bản ghi trùng 

  AUTH-03           Đăng ký với email Trả lỗi           P1
                    sai định dạng     validation        
                    hoặc mật khẩu yếu                   

  AUTH-04           Đăng nhập với     Trả access token  P1
                    thông tin đúng    và hồ sơ người    
                                      dùng              

  AUTH-05           Đăng nhập sai mật Từ chối; không    P1
                    khẩu              tiết lộ tài khoản 
                                      có tồn tại hay    
                                      không             

  AUTH-06           Gọi API cần đăng  Trả `401`         P1
                    nhập mà không có                    
                    token                               

  AUTH-07           Gọi API với token Trả `401`         P1
                    sai hoặc hết hạn                    

  AUTH-08           Đăng xuất/thu hồi Token refresh bị  P2
                    refresh token     vô hiệu hóa       
                    theo thiết kế                       

  AUTH-09           Người dùng cập    Chỉ các trường    P2
                    nhật hồ sơ của    cho phép được cập 
                    mình              nhật              

  AUTH-10           Người dùng cố tự  Bị từ chối hoặc   P0
                    gán vai trò Admin trường role bị bỏ 
                    khi đăng ký       qua; không cấp    
                                      quyền Admin       
  -----------------------------------------------------------------------

### 7.2 Phân quyền và quyền sở hữu

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  AUTHZ-01          Customer gọi API  Trả `403`         P0
                    quản trị                            

  AUTHZ-02          Seller A xem sản  Bị từ chối hoặc   P0
                    phẩm thuộc Store  không tiết lộ dữ  
                    của Seller B      liệu theo chính   
                                      sách              

  AUTHZ-03          Seller A cập nhật Trả `403` hoặc    P0
                    sản phẩm của      `404` theo chính  
                    Seller B          sách; dữ liệu     
                                      không đổi         

  AUTHZ-04          Customer A xem    Không truy cập    P0
                    đơn của Customer  được              
                    B                                   

  AUTHZ-05          Shipper A cập     Bị từ chối; trạng P0
                    nhật shipment     thái không đổi    
                    giao cho Shipper                    
                    B                                   

  AUTHZ-06          Shipper không     Không truy cập    P0
                    được phân công    được              
                    xem shipment                        
                    riêng tư                            

  AUTHZ-07          Người dùng có     Chỉ được thực     P1
                    nhiều vai trò     hiện thao tác phù 
                                      hợp với vai trò   
                                      và quyền sở hữu   
                                      hiện hành         

  AUTHZ-08          Frontend ẩn nút   Backend vẫn từ    P0
                    nhưng người dùng  chối nếu không đủ 
                    gọi trực tiếp API quyền             
  -----------------------------------------------------------------------

### 7.3 Cửa hàng và sản phẩm

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  STORE-01          Seller tạo Store  Tạo Store gắn với P1
                    lần đầu           Seller đang đăng  
                                      nhập              

  STORE-02          Seller đã có      Trả `409`; không  P0
                    Store tạo thêm    tạo Store thứ hai 
                    Store thứ hai                       

  STORE-03          Seller cập nhật   Cập nhật thành    P1
                    Store của mình    công các trường   
                                      hợp lệ            

  STORE-04          Seller sửa Store  Bị từ chối; dữ    P0
                    của Seller khác   liệu không đổi    

  CAT-01            Seller tạo sản    Sản phẩm được gắn P1
                    phẩm hợp lệ       vào Store của     
                                      Seller hiện tại   

  CAT-02            Seller gửi        Backend không     P0
                    `storeId` của     nhận quyền sở hữu 
                    người khác        từ dữ liệu        
                                      client; từ chối   
                                      hoặc bỏ qua       
                                      trường đó         

  CAT-03            Tạo sản phẩm với  Trả lỗi           P1
                    giá âm, tên trống validation        
                    hoặc số lượng âm                    

  CAT-04            Customer xem sản  Trả chi tiết sản  P1
                    phẩm đang hoạt    phẩm công khai    
                    động                                

  CAT-05            Customer tìm sản  Không hiển thị    P1
                    phẩm đã ẩn hoặc   trong danh sách   
                    bị từ chối        công khai         

  CAT-06            Lọc sản phẩm theo Kết quả đúng điều P2
                    từ khóa, danh     kiện lọc          
                    mục, Store và                       
                    khoảng giá                          

  CAT-07            Phân trang và sắp Kết quả có thứ tự P2
                    xếp danh sách sản và metadata đúng  
                    phẩm                                

  CAT-08            Seller cập nhật   Bị từ chối; dữ    P0
                    sản phẩm không    liệu không đổi    
                    thuộc Store của                     
                    mình                                
  -----------------------------------------------------------------------

### 7.4 Giỏ hàng và địa chỉ

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  CART-01           Customer thêm sản Dòng hàng được    P1
                    phẩm đang bán vào tạo hoặc số lượng 
                    giỏ               được cộng theo    
                                      quy tắc           

  CART-02           Thêm sản phẩm     Bị từ chối với    P1
                    không tồn tại     lỗi phù hợp       
                    hoặc đã ngừng bán                   

  CART-03           Thêm số lượng     Trả lỗi           P1
                    bằng 0 hoặc số âm validation        

  CART-04           Thêm số lượng     Bị từ chối hoặc   P1
                    vượt tồn kho      cảnh báo theo     
                                      chính sách tồn    
                                      kho               

  CART-05           Cập nhật và xóa   Chỉ giỏ của       P1
                    dòng hàng trong   Customer hiện tại 
                    giỏ               bị thay đổi       

  CART-06           Customer cố sửa   Bị từ chối        P0
                    dòng hàng trong                     
                    giỏ người khác                      

  CART-07           Giỏ chứa sản phẩm Response nhóm     P1
                    từ nhiều Store    đúng mặt hàng     
                                      theo Store        

  CART-08           Tạo, sửa, xóa địa Thao tác thành    P2
                    chỉ của mình      công và dữ liệu   
                                      hợp lệ            

  CART-09           Truy cập địa chỉ  Bị từ chối        P0
                    của tài khoản                       
                    khác                                

  CART-10           Đặt địa chỉ mặc   Chỉ một địa chỉ   P2
                    định mới          mặc định theo     
                                      chính sách được   
                                      chọn              
  -----------------------------------------------------------------------

### 7.5 Checkout đa nhà bán hàng

  --------------------------------------------------------------------------
  ID                Kịch bản           Kết quả mong đợi    Ưu tiên
  ----------------- ------------------ ------------------- -----------------
  CHK-01            Checkout với sản   Tạo một Parent      P0
                    phẩm từ một Store  Order và một Seller 
                                       Order               

  CHK-02            Checkout với sản   Tạo một Parent      P0
                    phẩm từ hai Store  Order và hai Seller 
                                       Order, mỗi đơn con  
                                       thuộc đúng Store    

  CHK-03            Checkout với nhiều Các sản phẩm được   P0
                    sản phẩm cùng      gom vào cùng Seller 
                    Store              Order               

  CHK-04            Client gửi giá     Backend tự tính     P0
                    hoặc tổng tiền giả lại; không dùng số  
                                       tiền client gửi     

  CHK-05            Giá sản phẩm thay  Checkout dùng giá   P1
                    đổi sau khi thêm   hiện hành hoặc yêu  
                    vào giỏ            cầu khách xác nhận  
                                       lại theo chính sách 

  CHK-06            Tồn kho không đủ   Không tạo đơn không P0
                    tại thời điểm      hợp lệ; trả lỗi     
                    checkout           nghiệp vụ rõ ràng   

  CHK-07            Hai request        Không bán vượt tồn  P0
                    checkout đồng thời kho                 
                    cho cùng lượng tồn                     
                    kho                                    

  CHK-08            Client retry cùng  Không tạo đơn       P0
                    `idempotencyKey`   trùng; trả kết quả  
                                       nhất quán           

  CHK-09            Một phần thao tác  Transaction/chiến   P0
                    tạo đơn thất bại   lược nhất quán ngăn 
                                       dữ liệu đơn dở dang 

  CHK-10            Địa chỉ giao hàng  Từ chối checkout    P0
                    không thuộc                            
                    Customer                               

  CHK-11            Checkout giỏ trống Từ chối với lỗi     P1
                                       nghiệp vụ           

  CHK-12            Checkout với       Đơn được tạo, thanh P1
                    phương thức COD    toán chưa được đánh 
                                       dấu là đã thu       

  CHK-13            Checkout có        Từ chối voucher     P1
                    voucher không hợp  hoặc trả cảnh báo   
                    lệ/hết hạn         theo hợp đồng; tổng 
                                       tiền không bị tính  
                                       sai                 

  CHK-14            Phí vận chuyển     Tổng phí và phân bổ P1
                    được tính cho      phí đúng quy tắc đã 
                    nhiều Seller Order chốt                
  --------------------------------------------------------------------------

### 7.6 Đơn hàng và vòng đời xử lý

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  ORD-01            Customer xem danh Chỉ trả các đơn   P1
                    sách đơn của mình thuộc Customer    
                                      hiện tại          

  ORD-02            Customer xem chi  Hiển thị Parent   P1
                    tiết đơn của mình Order và các      
                                      Seller Order liên 
                                      quan              

  ORD-03            Customer xem đơn  Bị từ chối        P0
                    của người khác                      

  ORD-04            Seller xem danh   Chỉ thấy đơn      P0
                    sách Seller Order thuộc Store của   
                                      mình              

  ORD-05            Seller xác nhận   Chuyển trạng thái P1
                    đơn mới hợp lệ    theo state        
                                      machine và lưu    
                                      lịch sử           

  ORD-06            Seller chuyển đơn Bị từ chối; trạng P0
                    từ trạng thái     thái không đổi    
                    không hợp lệ sang                   
                    trạng thái tùy ý                    

  ORD-07            Seller cập nhật   Bị từ chối;       P0
                    Parent Order trực Parent Order do   
                    tiếp              hệ thống tổng hợp 

  ORD-08            Customer yêu cầu  Tạo yêu cầu hủy   P1
                    hủy đơn còn đủ    hoặc hủy theo     
                    điều kiện         chính sách; lưu   
                                      lịch sử           

  ORD-09            Customer yêu cầu  Xử lý theo chính  P1
                    hủy đơn đã bàn    sách; không tự    
                    giao vận chuyển   động hủy trái quy 
                                      tắc               

  ORD-10            Một Seller Order  Trạng thái Parent P0
                    bị hủy, các đơn   Order được tổng   
                    con khác tiếp tục hợp chính xác     

  ORD-11            Đơn hoàn tất      Chỉ hoàn tất khi  P1
                                      thỏa điều kiện    
                                      nghiệp vụ         

  ORD-12            Trạng thái đơn    Ghi nhận người    P1
                    thay đổi          thực hiện, thời   
                                      gian và trạng     
                                      thái trước/sau    
  -----------------------------------------------------------------------

### 7.7 Thanh toán

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  PAY-01            Khởi tạo thanh    Tạo yêu cầu thanh P1
                    toán trực tuyến   toán theo nhà     
                    cho đơn hợp lệ    cung cấp đã cấu   
                                      hình              

  PAY-02            Khách gửi thông   Backend không     P0
                    báo thanh toán    đánh dấu đã thanh 
                    thành công giả từ toán nếu chưa xác 
                    frontend          minh              

  PAY-03            Webhook có chữ ký Ghi nhận sự kiện  P0
                    hợp lệ            và cập nhật trạng 
                                      thái phù hợp      

  PAY-04            Webhook sai chữ   Từ chối; không    P0
                    ký                thay đổi trạng    
                                      thái thanh toán   

  PAY-05            Webhook được gửi  Xử lý idempotent; P0
                    lặp               không ghi nhận    
                                      thanh toán hai    
                                      lần               

  PAY-06            Thanh toán thất   Ghi nhận trạng    P1
                    bại               thái thất bại và  
                                      cho phép xử lý    
                                      tiếp theo theo    
                                      chính sách        

  PAY-07            COD khi tạo đơn   Trạng thái chưa   P1
                                      thu tiền          

  PAY-08            Xác nhận thu COD  Cập nhật trạng    P1
                                      thái thanh toán   
                                      đúng vai trò và   
                                      quy trình         

  PAY-09            Yêu cầu hoàn tiền Kiểm tra điều     P1
                                      kiện và ghi nhận  
                                      trạng thái hoàn   
                                      tiền              

  PAY-10            Người không có    Bị từ chối        P0
                    quyền xem giao                      
                    dịch                                
  -----------------------------------------------------------------------

### 7.8 Vận chuyển và Shipper

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  SHP-01            Tạo shipment cho  Shipment liên kết P1
                    Seller Order đủ   đúng đơn và thông 
                    điều kiện         tin giao hàng     

  SHP-02            Admin phân công   Shipment được gán P1
                    Shipper hợp lệ    đúng Shipper      

  SHP-03            Admin phân công   Bị từ chối        P1
                    tài khoản không                     
                    phải Shipper                        

  SHP-04            Shipper xem danh  Chỉ thấy shipment P0
                    sách shipment     được phân công    
                                      cho mình          

  SHP-05            Shipper cập nhật  Trạng thái được   P1
                    trạng thái theo   cập nhật và lưu   
                    trình tự hợp lệ   lịch sử           

  SHP-06            Shipper cập nhật  Bị từ chối; dữ    P0
                    shipment không    liệu không đổi    
                    được phân công                      

  SHP-07            Cập nhật trạng    Bị từ chối theo   P1
                    thái giao hàng    state machine     
                    không hợp lệ                        

  SHP-08            Giao hàng thất    Ghi nhận lý do và P1
                    bại               trạng thái; kích  
                                      hoạt bước xử lý   
                                      tiếp theo theo    
                                      chính sách        

  SHP-09            Giao hàng thành   Shipment chuyển   P0
                    công              `DELIVERED`; đơn  
                                      được tổng hợp     
                                      trạng thái phù    
                                      hợp               

  SHP-10            Nhiều Seller      Theo dõi và tổng  P0
                    Order có nhiều    hợp trạng thái    
                    shipment          giao hàng chính   
                                      xác               
  -----------------------------------------------------------------------

### 7.9 Quản trị và hệ thống

  -----------------------------------------------------------------------
  ID                Kịch bản          Kết quả mong đợi  Ưu tiên
  ----------------- ----------------- ----------------- -----------------
  ADM-01            Admin tìm kiếm    Kết quả đúng, có  P2
                    người dùng và cửa phân trang        
                    hàng                                

  ADM-02            Admin duyệt/tạm   Trạng thái thay   P1
                    ngưng Store       đổi theo quy tắc  
                                      và có audit log   

  ADM-03            Admin kiểm duyệt  Kết quả duyệt/từ  P1
                    sản phẩm          chối được lưu và  
                                      phản ánh khi hiển 
                                      thị               

  ADM-04            Admin thực hiện   Ghi audit log đầy P0
                    thao tác quản trị đủ                
                    nhạy cảm                            

  SYS-01            Request thiếu     Trả lỗi           P1
                    trường bắt buộc   validation theo   
                                      định dạng thống   
                                      nhất              

  SYS-02            Request chứa dữ   Trả lỗi phù hợp,  P1
                    liệu sai          không gây lỗi 500 
                    kiểu/ngoài giới   không cần thiết   
                    hạn                                 

  SYS-03            Endpoint danh     Trả dữ liệu và    P2
                    sách nhận `page`, metadata phân     
                    `size`, `sort`    trang chính xác   
                    hợp lệ                              

  SYS-04            `size` vượt giới  Từ chối hoặc giới P2
                    hạn tối đa        hạn theo quy ước  
                                      API               

  SYS-05            Lỗi không dự kiến Trả lỗi tổng      P0
                                      quát, không lộ    
                                      stack trace hoặc  
                                      secret            

  SYS-06            Kiểm tra log      Không ghi         P0
                                      password, token   
                                      hoặc dữ liệu      
                                      thanh toán nhạy   
                                      cảm               

  SYS-07            Migration         Tạo schema đúng   P1
                    database chạy     và ứng dụng khởi  
                    trên database     động thành công   
                    trống                               

  SYS-08            Migration phiên   Thay đổi schema   P1
                    bản mới trên dữ   không làm mất dữ  
                    liệu mẫu          liệu ngoài dự     
                                      kiến              
  -----------------------------------------------------------------------

## 8. Kiểm thử phi chức năng

### 8.1 Hiệu năng cơ bản

Mục tiêu cụ thể cần được xác định sau khi có môi trường chạy ổn định. Ở
giai đoạn MVP, cần đo: - Thời gian phản hồi các endpoint danh sách và
chi tiết. - Thời gian checkout trong điều kiện dữ liệu mẫu. - Truy vấn
chậm và dấu hiệu N+1. - Hành vi khi nhiều request cập nhật tồn kho cùng
lúc.

Không công bố một ngưỡng hiệu năng như cam kết nếu chưa đo trên môi
trường đại diện.

### 8.2 Bảo mật

-   Xác minh authentication và authorization cho từng nhóm endpoint.
-   Kiểm tra truy cập ngang quyền (IDOR), đặc biệt với Order, Store,
    Product và Shipment.
-   Kiểm tra validation, giới hạn request và xử lý dữ liệu không tin
    cậy.
-   Kiểm tra bảo vệ secret, token, mật khẩu và thông tin thanh toán.
-   Kiểm tra webhook và khả năng chống xử lý sự kiện lặp.

### 8.3 Độ tin cậy và toàn vẹn dữ liệu

-   Kiểm tra transaction khi checkout tạo Parent Order, Seller Orders,
    Order Items và cập nhật tồn kho.
-   Kiểm tra constraint một Seller tối đa một Store.
-   Kiểm tra liên kết giữa Store, Product, Seller Order, Payment và
    Shipment.
-   Kiểm tra các thao tác retry và xử lý lỗi giữa chừng.
-   Kiểm tra lịch sử trạng thái và audit log.

### 8.4 Khả năng bảo trì

-   Unit test tập trung vào quy tắc nghiệp vụ.
-   Module không truy cập trực tiếp repository/entity nội bộ của module
    khác.
-   Tự động chạy test trong CI.
-   Có hướng dẫn chạy test và khởi tạo dữ liệu mẫu.

## 9. Tiêu chí vào và ra

### 9.1 Điều kiện bắt đầu kiểm thử

-   Yêu cầu và API contract của chức năng đã được rà soát.
-   Môi trường test và database sẵn sàng.
-   Migration đã chạy thành công.
-   Dữ liệu kiểm thử có thể tái tạo.
-   Các dependency ngoài có mock hoặc sandbox phù hợp.

### 9.2 Điều kiện hoàn thành một đợt kiểm thử

-   Tất cả test P0 và P1 trong phạm vi đợt chạy đã được thực hiện.
-   Không còn lỗi P0 chưa xử lý.
-   Lỗi P1 còn lại được ghi nhận, có người phụ trách và quyết định rõ
    ràng.
-   Regression test trọng yếu chạy thành công.
-   Kết quả, lỗi và giới hạn kiểm thử được ghi lại.

## 10. Quy trình xử lý lỗi

Mỗi lỗi cần ghi: - Mã lỗi và tiêu đề ngắn. - Môi trường, phiên bản và
commit liên quan. - Các bước tái hiện. - Kết quả thực tế và kết quả mong
đợi. - Mức độ ưu tiên và ảnh hưởng nghiệp vụ. - Log/trace đã loại bỏ
thông tin nhạy cảm. - Người phụ trách, trạng thái xử lý và kết quả kiểm
thử lại.

Trạng thái đề xuất: `OPEN`, `IN_PROGRESS`, `FIXED`, `RETEST`, `CLOSED`,
`REOPENED`, `WONT_FIX`.

## 11. Tổ chức kiểm thử trong dự án

  -----------------------------------------------------------------------
  Giai đoạn                           Hoạt động kiểm thử
  ----------------------------------- -----------------------------------
  Khi phát triển từng chức năng       Viết unit test cho logic và
                                      validation

  Khi hoàn thành một module           Integration test repository,
                                      service và database

  Khi hoàn thành API                  API test, validation và security
                                      test

  Khi tích hợp                        Integration và end-to-end test các
  checkout/order/payment/shipping     luồng chính

  Trước mỗi lần merge                 Chạy formatter/linter (nếu có),
                                      unit test và build

  Trước bản demo/release              Chạy regression checklist và ghi
                                      nhận lỗi còn tồn tại
  -----------------------------------------------------------------------

## 12. Danh sách kiểm thử hồi quy tối thiểu trước demo

-   Customer đăng ký và đăng nhập.
-   Seller tạo Store và đăng sản phẩm.
-   Customer tìm sản phẩm từ ít nhất hai Store.
-   Customer thêm sản phẩm của nhiều Store vào giỏ.
-   Checkout tạo một Parent Order và nhiều Seller Order.
-   Backend tính lại tổng tiền và kiểm tra tồn kho.
-   Seller chỉ xem và xử lý đơn thuộc Store của mình.
-   Admin phân công Shipper.
-   Shipper chỉ cập nhật shipment được phân công.
-   Trạng thái đơn và shipment được tổng hợp chính xác.
-   Các request không có quyền bị từ chối ở backend.
-   Không tạo đơn trùng khi retry checkout.
-   API lỗi trả response thống nhất và không lộ dữ liệu nhạy cảm.

## 13. Rủi ro kiểm thử cần theo dõi

-   Quy tắc tính phí vận chuyển và phân bổ phí giữa nhiều Seller chưa
    chốt.
-   Chính sách giữ/trừ tồn kho tại checkout chưa chốt.
-   Luồng hoàn tiền và hủy một Seller Order trong Parent Order chưa hoàn
    thiện.
-   Tích hợp payment/shipping phụ thuộc nhà cung cấp được chọn.
-   Dữ liệu test có thể không phản ánh đầy đủ tải và hành vi production.
-   Nếu ranh giới mô-đun không được giữ, kiểm thử tích hợp có thể trở
    nên khó cô lập.

## 14. Tiêu chí hoàn thành tài liệu

-   Các luồng nghiệp vụ quan trọng có kịch bản kiểm thử.
-   Mỗi kịch bản có mã, bước hoặc điều kiện, kết quả mong đợi và mức ưu
    tiên.
-   Có kiểm thử quyền theo vai trò và quyền sở hữu tài nguyên.
-   Có kịch bản cho checkout đa nhà bán hàng, tồn kho, payment và
    shipment.
-   Có chiến lược unit, integration, API, security và regression test.
-   Các giả định và nội dung chưa chốt được ghi rõ.

## 15. Bước tiếp theo

Sau khi hoàn thiện Test Plan, có thể bắt đầu khởi tạo backend:

1.  Tạo project Spring Boot với Java 21 và Maven.
2.  Thêm các dependency cần thiết: Spring Web, Validation, Spring Data
    JPA, PostgreSQL Driver, Flyway, Spring Security và test.
3.  Tạo cấu trúc package theo module và quy tắc kiến trúc.
4.  Cấu hình PostgreSQL qua Docker Compose.
5.  Tạo migration đầu tiên cho schema.
6.  Viết một vertical slice nhỏ: đăng ký/đăng nhập hoặc quản lý danh
    mục, kèm test.

Nên triển khai từng luồng hoàn chỉnh từ API đến database và test, thay
vì tạo thật nhiều class rỗng cùng lúc.
