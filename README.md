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

## 5. Ghi chú

- Package tiện ích đặt tên `ultis` theo đúng cấu trúc bạn mô tả (nếu muốn đổi thành `utils`: đổi tên thư mục + sửa `package`/`import`).
- Cổng thanh toán ONLINE đang **giả lập** (SANDBOX_GATEWAY, luôn thành công) — chỗ tích hợp VNPay/MoMo thật đã đánh dấu `TODO` trong `PaymentService`.
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
