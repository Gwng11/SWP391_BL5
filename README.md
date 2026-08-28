# Hotel Management System (F01–F24)

Jakarta Servlet + JSP (Tomcat 10.1+), JDBC thuần với SQL Server — theo cấu trúc package:
`entity` · `controller` · `repository` · `service` · `dal` · `interfaces` · `ultis`

## 1. Cài đặt

### Bước 1 — Database
Chạy lần lượt trong SSMS (database `SingleHotelManagementDB` đã tạo sẵn từ script của bạn):

1. `sql/patch_01_required.sql` — **bắt buộc**: thêm bảng `user_tokens`, cột `users.email_verified_at`, cột `service_requests.scheduled_at`
2. `sql/patch_03_manager.sql` — **bắt buộc cho Manager**: profile nhân viên, trạng thái phòng, inspection/audit và index dashboard/report
3. `sql/patch_02_seed_data.sql` — dữ liệu mẫu + tài khoản test:
4. `sql/patch_04_manager_demo_data.sql` — **tùy chọn, nên chạy khi demo Manager**: room type/phòng/trạng thái, giá theo ngày, reservation/payment, housekeeping, maintenance và report data mang mã `MGR-DEMO`
5. `sql/patch_08_staff_work_queue_demo.sql` — **tùy chọn để test Staff**: thêm tối đa 4 housekeeping task `PENDING` và 4 maintenance issue `OPEN`, đều chưa phân công để nhiều Staff thử nhận việc

| Tài khoản | Mật khẩu | Vai trò |
|---|---|---|
| admin@hotel.vn | Admin@123 | ADMIN |
| receptionist@hotel.vn | Recep@123 | RECEPTIONIST |
| staff@hotel.vn | Staff@123 | SERVICE_STAFF |
| manager.demo@hotel.vn | Manager@123 | MANAGER |
| customer@test.vn | Customer@123 | CUSTOMER |

### Bước 2 — Cấu hình
- `dal/DBContext.java`: ưu tiên `HMS_DB_URL`, `HMS_DB_USERNAME`, `HMS_DB_PASSWORD`; hoặc dùng `HMS_DB_SERVER`, `HMS_DB_PORT`, `HMS_DB_NAME`, `HMS_DB_USER`.
  Nếu bạn đã có DBContext riêng thì giữ của bạn, miễn là có `public Connection getConnection()` trả về **connection mới mỗi lần gọi**.
- `ultis/EmailUtil.java`: điền SMTP_USER / SMTP_PASS (Gmail cần App Password). Chưa cấu hình cũng chạy được — email sẽ ghi log `FAILED` vào `email_logs`, nghiệp vụ chính không bị ảnh hưởng.

#### Cấu hình thanh toán online

Hệ thống mặc định **không ghi nhận giả một giao dịch online thành công**. Khi chưa cấu hình gateway,
nút thanh toán online bị vô hiệu hóa và backend cũng từ chối request thủ công.

Để dùng **MoMo Sandbox**, cấu hình ba credentials được MoMo cấp. Không đưa secret vào source code hoặc Git:

```text
HMS_PAYMENT_PROVIDER=MOMO
HMS_MOMO_PARTNER_CODE=<partnerCode>
HMS_MOMO_ACCESS_KEY=<accessKey>
HMS_MOMO_SECRET_KEY=<secretKey>
```

Các biến MoMo tùy chọn:

```text
HMS_MOMO_CREATE_URL=https://test-payment.momo.vn/v2/gateway/api/create
HMS_MOMO_REDIRECT_URL=https://<public-host>/HotelManagement/payment/momo-return
HMS_MOMO_IPN_URL=https://<public-host>/HotelManagement/payment/momo-ipn
HMS_MOMO_PARTNER_NAME=Hotel Management System
HMS_MOMO_STORE_ID=HMS
```

- `redirectUrl` đưa trình duyệt khách về hệ thống; `ipnUrl` là callback server-to-server xác nhận kết quả.
- Khi bỏ hai biến URL, hệ thống tự dựng URL HTTPS từ các header proxy của Cloudflare. Khi đặt URL tường minh,
  cả hai URL phải là địa chỉ public HTTPS mà MoMo truy cập được.
- Payment được lưu `PENDING` trước khi gọi MoMo; chỉ callback có HMAC-SHA256 hợp lệ, đúng `orderId`, đúng số tiền
  và `resultCode=0` mới chuyển sang `SUCCESS`.

Ví dụ PowerShell với Cloudflare Tunnel:

```powershell
$env:HMS_PAYMENT_PROVIDER='MOMO'
$env:HMS_MOMO_PARTNER_CODE='YOUR_PARTNER_CODE'
$env:HMS_MOMO_ACCESS_KEY='YOUR_ACCESS_KEY'
$env:HMS_MOMO_SECRET_KEY='YOUR_SECRET_KEY'
$env:HMS_MOMO_REDIRECT_URL='https://<tunnel-host>/HotelManagement/payment/momo-return'
$env:HMS_MOMO_IPN_URL='https://<tunnel-host>/HotelManagement/payment/momo-ipn'
```

Sau khi đặt biến, phải dừng và khởi động lại Tomcat/IDE. Các tài khoản demo công khai chỉ được dùng với
`test-payment.momo.vn`, tuyệt đối không dùng cho production.

Để dùng VNPay Sandbox, chỉ cần cấu hình hai biến môi trường trước khi khởi động Tomcat:

```text
HMS_VNPAY_TMN_CODE=<mã website sandbox do VNPay cấp>
HMS_VNPAY_HASH_SECRET=<chuỗi bí mật sandbox do VNPay cấp>
```

Khi thấy đủ hai biến trên, hệ thống tự chọn VNPay. Các biến tùy chọn:

```text
HMS_PAYMENT_PROVIDER=VNPAY
HMS_VNPAY_RETURN_URL=https://<public-host>/HotelManagement/payment/vnpay-return
HMS_VNPAY_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
```

- `HMS_PAYMENT_PROVIDER=VNPAY` chỉ cần khi muốn khai báo tường minh; bình thường có thể bỏ.
- `HMS_VNPAY_RETURN_URL` nên đặt khi có domain/tunnel cố định. Nếu bỏ, hệ thống tự dựng URL từ request
  và hỗ trợ `X-Forwarded-Proto/Host/Port` của Cloudflare Tunnel.
- `HMS_VNPAY_PAY_URL` có giá trị Sandbox như trên theo mặc định.
- Chỉ để demo không qua VNPay mới đặt `HMS_PAYMENT_PROVIDER=SANDBOX`; giao diện sẽ cảnh báo đây là mô phỏng.

Ví dụ cho phiên PowerShell hiện tại:

```powershell
$env:HMS_VNPAY_TMN_CODE='YOUR_TMN_CODE'
$env:HMS_VNPAY_HASH_SECRET='YOUR_HASH_SECRET'
$env:HMS_VNPAY_RETURN_URL='https://pay-test.example.com/HotelManagement/payment/vnpay-return'
mvn clean package
```

Tomcat/IDE phải được khởi động từ tiến trình đã nhận các biến này. Nếu thay biến khi Tomcat đang chạy,
hãy dừng và khởi động lại Tomcat.

Khai báo IPN URL trên VNPay Sandbox là:
`https://<public-host>/HotelManagement/payment/vnpay-ipn`.
VNPay phải truy cập được IPN từ Internet, vì vậy khi thử trên localhost cần một public HTTPS tunnel
hoặc máy chủ thử nghiệm. Không đưa `HMS_VNPAY_HASH_SECRET` vào source code hay commit lên Git.

### Bước 3 — Chạy
```bash
mvn clean package
# deploy target/HotelManagement.war lên Tomcat 10.1+ (Java 17)
```
NetBeans/IntelliJ: mở project Maven → Run trên Tomcat 10.1.

## 2. Mapping chức năng → URL → class

| F | URL | Controller | Service |
|---|---|---|---|
| F01 Xem thông tin KS | `/home` | HomeController | HotelInfoService |
| F02 Tìm phòng trống | `/rooms` | RoomController | RoomService |
| F03 Chi tiết phòng | `/rooms/detail?id=` | RoomController | RoomService |
| F04 Đăng ký/Đăng nhập/Khôi phục | `/login /register /verify /forgot-password /reset-password /logout` | AuthController | AuthService |
| F05 Hồ sơ cá nhân | `/profile` | ProfileController | UserService |
| F06 Đặt phòng | `/booking` | BookingController | ReservationService |
| F07 Quản lý đơn | `/my-reservations`, `/reservation?id=` | ReservationController | ReservationService |
| F08 Đặt cọc | `/deposit?reservationId=` | DepositController | PaymentService |
| F09 Quản lý khách hàng | `/reception/customers` | CustomerController | CustomerService |
| F10 Check-in | `/reception/checkin` | CheckInController | FrontDeskService |
| F11 Gán/đổi phòng | `/reception/assign?reservationId=` | AssignRoomController | FrontDeskService |
| F12 Quản lý kỳ ở | `/reception/stays` | StayController | FrontDeskService |
| F13 Check-out | `/reception/checkout` | CheckOutController | FrontDeskService |
| F14 Hóa đơn & TT cuối | `/reception/invoice?reservationId=` | InvoiceController | InvoiceService |
| F15 Yêu cầu dịch vụ | `/services` | ServiceController | ServiceRequestService |
| F16 Xử lý yêu cầu DV | `/reception/service-requests`, `/staff/service-requests` | ServiceTaskController | ServiceRequestService |
| F17 Housekeeping tasks | `/manager/housekeeping`, `/staff/housekeeping` | HousekeepingController | ManagerService |
| F18 Inspect rooms | action trong `/staff/housekeeping` | HousekeepingController | ManagerService |
| F19 Maintenance issues | `/manager/maintenance`, `/staff/maintenance` | MaintenanceController | ManagerService |
| F20 Quản lý phòng | `/manager/rooms` | ManagerRoomController | ManagerService |
| F21 Room types, amenities, images | `/manager/room-types` | RoomTypeManagementController, RoomImageController | ManagerService |
| F22 Room pricing | `/manager/pricing` | RoomPricingController | ManagerService |
| F23 Manager dashboard | `/manager/dashboard` | ManagerDashboardController | ManagerService |
| F24 Reports & statistics | `/manager/reports` | ManagerReportController | ManagerService |

## 3. Luồng nghiệp vụ chính (thứ tự demo)

```
Khách đăng ký (F04) → xác thực email → tìm phòng (F02) → xem chi tiết (F03)
→ đặt phòng (F06, đơn PENDING) → đặt cọc 20% (F08, đơn → CONFIRMED)
→ Lễ tân check-in (F10) → gán phòng sạch (F11) → khách yêu cầu dịch vụ (F15)
→ nhân viên xử lý (F16, tiền cộng vào đơn) → phụ thu/gia hạn nếu cần (F12)
→ phát hành hóa đơn + thu tiền còn lại (F14) → check-out (F13, phòng → DIRTY)
```

## 4. Quy tắc nghiệp vụ đã cài đặt

- **Giá**: ưu tiên `room_rates` theo ngày, thiếu thì dùng `base_price`; SRS hiện không quy định thuế và yêu cầu cọc 20% (cấu hình trong `ultis/Constants`).
- **Tồn phòng** = số phòng active của loại − SUM(quantity) các đơn PENDING/CONFIRMED/CHECKED_IN giao ngày; tôn trọng `stop_sell`.
- **Check-in** yêu cầu đơn CONFIRMED + đủ cọc. **Check-out** yêu cầu hóa đơn PAID.
- **Gán phòng**: chỉ phòng AVAILABLE + CLEAN/INSPECTED, đúng loại đã đặt; DB có unique index chặn 1 phòng bị gán 2 lần; đổi phòng giữ lịch sử.
- **Bảo mật**: mật khẩu PBKDF2-HMAC-SHA256; khóa 15 phút sau 5 lần đăng nhập sai; token verify/reset chỉ lưu SHA-256, có hạn dùng, dùng 1 lần.
- **Email**: template lấy từ `email_templates` (placeholder `{{key}}`), mọi lần gửi đều ghi `email_logs` (SENT/FAILED).
- **Transaction**: tạo đơn (reservation + rooms + guests), gán/đổi/trả phòng đều chạy trong 1 transaction JDBC.

### 4b. Luồng Walk-in tại quầy

Lễ tân mở `/reception/walkin`, tra khách theo CCCD/hộ chiếu, chọn một phòng vật lý
đang `AVAILABLE` và `CLEAN/INSPECTED`, nhập số đêm và thu tối thiểu 20% tiền cọc.
Một lần xác nhận sẽ tạo đơn `WALK_IN`, ghi payment, chuyển đơn sang `CONFIRMED`,
check-in và gán phòng đã chọn. Nếu check-in hoặc gán phòng gặp tranh chấp phút cuối,
đơn và payment vẫn được giữ để lễ tân xử lý tiếp tại màn Check-in/Gán phòng.

Việc tạo đơn kiểm tra lại tồn phòng trong cùng transaction bằng `UPDLOCK/HOLDLOCK`,
khóa theo thứ tự `room_type_id`, nên luồng online và walk-in không thể cùng bán phòng cuối.

## 5. Ghi chú

- Package tiện ích đặt tên `ultis` theo đúng cấu trúc bạn mô tả (nếu muốn đổi thành `utils`: đổi tên thư mục + sửa `package`/`import`).
- Thanh toán ONLINE bị khóa an toàn khi chưa cấu hình. Hệ thống chọn `MomoPaymentGateway` hoặc `VnPayPaymentGateway`
  theo credentials/provider; `SandboxPaymentGateway` chỉ hoạt động khi được bật tường minh. Giao dịch gateway đi qua vòng đời `PENDING` → redirect → Return/IPN xác minh
  chữ ký và số tiền → `SUCCESS/FAILED`; callback lặp lại được xử lý idempotent để không ghi nhận tiền hai lần.
- F25–F26 (quản trị user và quản lý template email) chưa nằm trong phạm vi Manager và không được cấp cho role `MANAGER`.

## 6. Kiểm thử

```powershell
# Unit tests
mvn test

# Unit + SQL Server integration + embedded Tomcat/JSP smoke test
$env:HMS_IT='1'
mvn test

# Clean WAR build
mvn clean package
```

Integration tests dùng dữ liệu có mã riêng, tự dọn sau mỗi test và yêu cầu đã chạy `patch_03_manager.sql` cùng tài khoản seed `manager.demo@hotel.vn`.
