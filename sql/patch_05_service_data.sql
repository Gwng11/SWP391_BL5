USE [SingleHotelManagementDB];
GO

SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

-- ==========================================
-- 0. AN TOÀN BỔ SUNG CỘT BỊ THIẾU
-- ==========================================
IF COL_LENGTH('dbo.hotel_services', 'image_url') IS NULL
BEGIN
    ALTER TABLE dbo.hotel_services ADD image_url VARCHAR(255) NULL;
END
GO

-- ==========================================
-- 1. HỒ SƠ KHÁCH SẠN (HOTEL_PROFILE)
-- ==========================================
-- Cập nhật dữ liệu cho dòng profile_id = 1 đã được tạo ở script gốc
IF EXISTS (SELECT 1 FROM dbo.hotel_profile WHERE profile_id = 1)
BEGIN
    UPDATE dbo.hotel_profile
    SET hotel_name = N'Sunrise Hanoi Hotel',
        [description] = N'Khách sạn 4 sao ngay trung tâm phố cổ Hà Nội.',
        [address] = N'12 Hàng Bài, Hoàn Kiếm, Hà Nội',
        phone = '+842438252525',
        email = 'info@sunrisehotel.vn',
        check_in_time = '14:00:00',
        check_out_time = '12:00:00',
        currency_code = 'VND',
        updated_at = SYSUTCDATETIME()
    WHERE profile_id = 1;
END
ELSE
BEGIN
    INSERT INTO dbo.hotel_profile (profile_id, hotel_name, [description], [address], phone, email, check_in_time, check_out_time, currency_code, updated_at)
    VALUES (1, N'Sunrise Hanoi Hotel', N'Khách sạn 4 sao ngay trung tâm phố cổ Hà Nội.',
            N'12 Hàng Bài, Hoàn Kiếm, Hà Nội', '+842438252525', 'info@sunrisehotel.vn', '14:00:00', '12:00:00', 'VND', SYSUTCDATETIME());
END
GO

-- ==========================================
-- 2. NGƯỜI DÙNG (USERS)
-- ==========================================
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'admin@hotel.vn')
    INSERT INTO dbo.users (email, password_hash, full_name, phone, role_code, department_code, status_code, email_verified_at)
    VALUES ('admin@hotel.vn', '65536:42o/ESjTo/rXp8yura47jQ==:+uf3pLB8rvMLXty8VDvS1NUafQeZybhoj4e54MhAt6U=', N'Quản trị hệ thống', '0900000001', 'ADMIN', NULL, 'ACTIVE', SYSUTCDATETIME());
ELSE
    UPDATE dbo.users SET full_name = N'Quản trị hệ thống' WHERE email = 'admin@hotel.vn';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'manager@hotel.vn')
    INSERT INTO dbo.users (email, password_hash, full_name, phone, role_code, department_code, status_code, email_verified_at)
    VALUES ('manager@hotel.vn', '65536:bMM2lgiA60SoQsyOLgoHGA==:ei9PfrptPXMvh+C2zYsRMLyMP/8AGtOm9+a+hoJJomM=', N'Hotel Manager', '0900000005', 'MANAGER', NULL, 'ACTIVE', SYSUTCDATETIME());
ELSE
    UPDATE dbo.users SET full_name = N'Hotel Manager' WHERE email = 'manager@hotel.vn';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'manager.demo@hotel.vn')
    INSERT INTO dbo.users (email, password_hash, full_name, phone, role_code, department_code, status_code, email_verified_at)
    VALUES ('manager.demo@hotel.vn', '65536:QR/p4a/n/2EORqdpGsUmVA==:SyBaOtLf651q/DgvEASfbsoFAjj3FmhbMG7wXYFPfx4=', N'Quản lý khách sạn Demo', '0900000004', 'MANAGER', NULL, 'ACTIVE', SYSUTCDATETIME());
ELSE
    UPDATE dbo.users SET full_name = N'Quản lý khách sạn Demo' WHERE email = 'manager.demo@hotel.vn';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'receptionist@hotel.vn')
    INSERT INTO dbo.users (email, password_hash, full_name, phone, role_code, department_code, status_code, email_verified_at)
    VALUES ('receptionist@hotel.vn', '65536:/ipmwffCw0JymYHFhHBieg==:Ou4q8bNOb+QujFDCS67EFwgQ3r/MolkA8RnL8I+vnxU=', N'Lễ tân Mai Anh', '0900000002', 'RECEPTIONIST', NULL, 'ACTIVE', SYSUTCDATETIME());
ELSE
    UPDATE dbo.users SET full_name = N'Lễ tân Mai Anh' WHERE email = 'receptionist@hotel.vn';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'staff@hotel.vn')
    INSERT INTO dbo.users (email, password_hash, full_name, phone, role_code, department_code, status_code, email_verified_at)
    VALUES ('staff@hotel.vn', '65536:4A7+JwhhVQrGkzAr3bd1aA==:+2sNrGOO485fdEui7NxKEVCd55sy0aHe9JUag2VmIcg=', N'Nhân viên Văn Bình', '0900000003', 'SERVICE_STAFF', 'GENERAL_SERVICE', 'ACTIVE', SYSUTCDATETIME());
ELSE
    UPDATE dbo.users SET full_name = N'Nhân viên Văn Bình' WHERE email = 'staff@hotel.vn';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'customer@test.vn')
    INSERT INTO dbo.users (email, password_hash, full_name, phone, role_code, department_code, status_code, email_verified_at)
    VALUES ('customer@test.vn', '65536:PUc8F/jYWpQ79CzcTU96gw==:vOv0AAX35PUtuXIzg1aq7iYtEUihQgxvbd37yR7ro5c=', N'Nguyễn Văn Khách', '0912345678', 'CUSTOMER', NULL, 'ACTIVE', SYSUTCDATETIME());
ELSE
    UPDATE dbo.users SET full_name = N'Nguyễn Văn Khách' WHERE email = 'customer@test.vn';
GO

-- ==========================================
-- 3. HỒ SƠ KHÁCH HÀNG (CUSTOMERS)
-- ==========================================
IF NOT EXISTS (SELECT 1 FROM dbo.customers WHERE email = 'customer@test.vn')
BEGIN
    INSERT INTO dbo.customers (user_id, customer_code, full_name, email, phone, nationality)
    SELECT user_id, 'CUS000001', N'Nguyễn Văn Khách', 'customer@test.vn', '0912345678', N'Việt Nam'
    FROM dbo.users WHERE email = 'customer@test.vn';
END
ELSE
BEGIN
    UPDATE dbo.customers 
    SET full_name = N'Nguyễn Văn Khách', 
        nationality = N'Việt Nam'
    WHERE email = 'customer@test.vn';
END
GO

-- ==========================================
-- 4. LOẠI PHÒNG (ROOM_TYPES)
-- ==========================================
IF NOT EXISTS (SELECT 1 FROM dbo.room_types)
BEGIN
    INSERT INTO dbo.room_types (type_code, type_name, [description], max_adults, max_children, bed_type, room_size_m2, base_price, amenities_json) VALUES
    ('STD', N'Standard',  N'Phòng tiêu chuẩn, cửa sổ hướng phố.', 2, 1, N'1 giường đôi', 22, 800000,  N'["WiFi","TV","Điều hòa"]'),
    ('DLX', N'Deluxe',    N'Phòng cao cấp, ban công.',            2, 2, N'1 giường king', 30, 1200000, N'["WiFi","TV","Điều hòa","Minibar","Ban công"]'),
    ('FAM', N'Family',    N'Phòng gia đình rộng rãi.',            4, 2, N'2 giường đôi',  40, 1800000, N'["WiFi","TV","Điều hòa","Minibar","Bồn tắm"]'),
    ('SUT', N'Suite',     N'Suite hạng sang, view hồ.',           2, 2, N'1 giường king', 55, 3000000, N'["WiFi","TV","Điều hòa","Minibar","Bồn tắm","Phòng khách"]');
END
GO

-- ==========================================
-- 5. PHÒNG VẬT LÝ (ROOMS)
-- ==========================================
IF NOT EXISTS (SELECT 1 FROM dbo.rooms)
BEGIN
    INSERT INTO dbo.rooms (room_type_id, room_number, floor_number)
    SELECT rt.room_type_id, v.room_number, v.floor_number
    FROM (VALUES
     ('STD','101',1),('STD','102',1),('STD','103',1),('STD','201',2),('STD','202',2),
     ('DLX','203',2),('DLX','301',3),('DLX','302',3),
     ('FAM','303',3),('FAM','401',4),
     ('SUT','402',4),('SUT','501',5)
    ) v(type_code, room_number, floor_number)
    JOIN dbo.room_types rt ON rt.type_code = v.type_code;
END
GO

-- ==========================================
-- 6. DỊCH VỤ KHÁCH SẠN (HOTEL_SERVICES)
-- ==========================================
IF NOT EXISTS (SELECT 1 FROM dbo.hotel_services)
BEGIN
    INSERT INTO dbo.hotel_services (service_code, service_name, [description], unit_name, unit_price) VALUES
    ('LAUNDRY',   N'Giặt ủi',         N'Giặt ủi trong ngày',           N'kg',     50000),
    ('BREAKFAST', N'Bữa sáng',      N'Buffet sáng tại nhà hàng',     N'suất',   150000),
    ('SPA',       N'Spa & Massage',   N'Gói massage thư giãn 60 phút', N'lượt',   500000),
    ('AIRPORT',   N'Đưa đón sân bay', N'Xe riêng đưa đón Nội Bài',     N'chuyến', 350000),
    ('MINIBAR',   N'Minibar',         N'Đồ uống trong phòng',          N'lần',    100000);
END
GO

-- ==========================================
-- 7. TEMPLATE EMAIL (EMAIL_TEMPLATES)
-- ==========================================
IF NOT EXISTS (SELECT 1 FROM dbo.email_templates)
BEGIN
    INSERT INTO dbo.email_templates (template_code, template_name, event_code, subject_template, body_html) VALUES
    ('TPL_VERIFY',      N'Xác thực tài khoản',  'ACCOUNT_VERIFICATION',  N'Xác thực tài khoản Sunrise Hotel',         N'<p>Chào {{full_name}},</p><p>Bấm vào link sau để xác thực email: <a href="{{verify_link}}">Xác thực ngay</a></p><p>Link có hiệu lực 24 giờ.</p>'),
    ('TPL_RESET',       N'Đặt lại mật khẩu',   'PASSWORD_RESET',        N'Đặt lại mật khẩu Sunrise Hotel',          N'<p>Chào {{full_name}},</p><p>Bấm vào link sau để đặt lại mật khẩu: <a href="{{reset_link}}">Đặt lại mật khẩu</a></p><p>Link có hiệu lực 60 phút.</p>'),
    ('TPL_RES_CONFIRM', N'Xác nhận đặt phòng',  'RESERVATION_CONFIRMED', N'Đặt phòng thành công - Mã {{booking_code}}', N'<p>Chào {{full_name}},</p><p>Đơn <b>{{booking_code}}</b> ({{check_in_date}} → {{check_out_date}}) đã được ghi nhận.</p><p>Tổng tiền: {{total_amount}} đ. Vui lòng đặt cọc {{deposit_required}} đ để xác nhận.</p>'),
    ('TPL_RES_UPDATE',  N'Cập nhật đặt phòng', 'RESERVATION_UPDATED',   N'Đơn {{booking_code}} đã được cập nhật',     N'<p>Chào {{full_name}},</p><p>Đơn <b>{{booking_code}}</b> đã đổi thành {{check_in_date}} → {{check_out_date}}. Tổng mới: {{total_amount}} đ.</p>'),
    ('TPL_RES_CANCEL',  N'Hủy đặt phòng',      'RESERVATION_CANCELLED', N'Đơn {{booking_code}} đã hủy',              N'<p>Chào {{full_name}},</p><p>Đơn <b>{{booking_code}}</b> đã được hủy. Lý do: {{cancellation_reason}}</p>'),
    ('TPL_DEPOSIT',     N'Biên nhận đặt cọc',  'DEPOSIT_RECEIPT',       N'Biên nhận đặt cọc - Đơn {{booking_code}}', N'<p>Chào {{full_name}},</p><p>Đã nhận {{amount}} đ tiền cọc cho đơn <b>{{booking_code}}</b> ({{method}}, mã GD: {{reference}}).</p>'),
    ('TPL_INVOICE',     N'Hóa đơn & biên nhận','INVOICE_AND_RECEIPT',    N'Hóa đơn {{invoice_number}} - Đơn {{booking_code}}', N'<p>Chào {{full_name}},</p><p>Hóa đơn <b>{{invoice_number}}</b>: tổng {{total_amount}} đ, đã thanh toán {{paid_amount}} đ. Cảm ơn quý khách!</p>');
END
GO

PRINT N'Chèn dữ liệu thành công!';
GO