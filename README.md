# Hotel Management System (F01–F16)

Jakarta Servlet + JSP (Tomcat 10.1+), JDBC thuần với SQL Server — theo cấu trúc package:
`entity` · `controller` · `repository` · `service` · `dal` · `interfaces` · `ultis`

## 1. Cài đặt

### Bước 1 — Database
Chạy lần lượt trong SSMS (database `SingleHotelManagementDB` đã tạo sẵn từ script của bạn):

1. `sql/patch_01_required.sql` — **bắt buộc**: thêm bảng `user_tokens`, cột `users.email_verified_at`, cột `service_requests.scheduled_at`
2. `sql/patch_02_seed_data.sql` — dữ liệu mẫu + tài khoản test:

| Tài khoản | Mật khẩu | Vai trò |
|---|---|---|
| admin@hotel.vn | Admin@123 | ADMIN |
| receptionist@hotel.vn | Recep@123 | RECEPTIONIST |
| staff@hotel.vn | Staff@123 | SERVICE_STAFF |
| customer@test.vn | Customer@123 | CUSTOMER |

### Bước 2 — Cấu hình
- `dal/DBContext.java`: sửa `PASSWORD` (và SERVER nếu khác `localhost\SQLEXPRESS`).
  Nếu bạn đã có DBContext riêng thì giữ của bạn, miễn là có `public Connection getConnection()` trả về **connection mới mỗi lần gọi**.
- `ultis/EmailUtil.java`: điền SMTP_USER / SMTP_PASS (Gmail cần App Password). Chưa cấu hình cũng chạy được — email sẽ ghi log `FAILED` vào `email_logs`, nghiệp vụ chính không bị ảnh hưởng.

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
| F16 Xử lý yêu cầu DV | `/staff/service-requests` | ServiceTaskController | ServiceRequestService |

## 3. Luồng nghiệp vụ chính (thứ tự demo)

```
Khách đăng ký (F04) → xác thực email → tìm phòng (F02) → xem chi tiết (F03)
→ đặt phòng (F06, đơn PENDING) → đặt cọc 30% (F08, đơn → CONFIRMED)
→ Lễ tân check-in (F10) → gán phòng sạch (F11) → khách yêu cầu dịch vụ (F15)
→ nhân viên xử lý (F16, tiền cộng vào đơn) → phụ thu/gia hạn nếu cần (F12)
→ phát hành hóa đơn + thu tiền còn lại (F14) → check-out (F13, phòng → DIRTY)
```

## 4. Quy tắc nghiệp vụ đã cài đặt

- **Giá**: ưu tiên `room_rates` theo ngày, thiếu thì dùng `base_price`; thuế 10%, cọc 30% (đổi trong `ultis/Constants`).
- **Tồn phòng** = số phòng active của loại − SUM(quantity) các đơn PENDING/CONFIRMED/CHECKED_IN giao ngày; tôn trọng `stop_sell`.
- **Check-in** yêu cầu đơn CONFIRMED + đủ cọc. **Check-out** yêu cầu hóa đơn PAID.
- **Gán phòng**: chỉ phòng AVAILABLE + CLEAN/INSPECTED, đúng loại đã đặt; DB có unique index chặn 1 phòng bị gán 2 lần; đổi phòng giữ lịch sử.
- **Bảo mật**: mật khẩu PBKDF2-HMAC-SHA256; khóa 15 phút sau 5 lần đăng nhập sai; token verify/reset chỉ lưu SHA-256, có hạn dùng, dùng 1 lần.
- **Email**: template lấy từ `email_templates` (placeholder `{{key}}`), mọi lần gửi đều ghi `email_logs` (SENT/FAILED).
- **Transaction**: tạo đơn (reservation + rooms + guests), gán/đổi/trả phòng đều chạy trong 1 transaction JDBC.

### 4b. Luồng Walk-in tại quầy

Lễ tân mở `/reception/walkin`, tra khách theo CCCD/hộ chiếu, chọn một phòng vật lý
đang `AVAILABLE` và `CLEAN/INSPECTED`, nhập số đêm và thu tối thiểu 30% tiền cọc.
Một lần xác nhận sẽ tạo đơn `WALK_IN`, ghi payment, chuyển đơn sang `CONFIRMED`,
check-in và gán phòng đã chọn. Nếu check-in hoặc gán phòng gặp tranh chấp phút cuối,
đơn và payment vẫn được giữ để lễ tân xử lý tiếp tại màn Check-in/Gán phòng.

Việc tạo đơn kiểm tra lại tồn phòng trong cùng transaction bằng `UPDLOCK/HOLDLOCK`,
khóa theo thứ tự `room_type_id`, nên luồng online và walk-in không thể cùng bán phòng cuối.

## 5. Ghi chú

- Package tiện ích đặt tên `ultis` theo đúng cấu trúc bạn mô tả (nếu muốn đổi thành `utils`: đổi tên thư mục + sửa `package`/`import`).
- Cổng thanh toán ONLINE đang **giả lập** (SANDBOX_GATEWAY, luôn thành công) — chỗ tích hợp VNPay/MoMo thật đã đánh dấu `TODO` trong `PaymentService`.
- F17–F26 (housekeeping, maintenance, quản lý phòng/giá, dashboard, báo cáo, quản trị user, quản lý template email) chưa nằm trong phạm vi lần này — schema đã sẵn sàng, chỉ cần thêm repo/service/controller theo cùng pattern.
