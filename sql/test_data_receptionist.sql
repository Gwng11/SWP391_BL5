-- =====================================================================
-- DỮ LIỆU TEST NGHIỆP VỤ LỄ TÂN (F09-F13, F16, Walk-in, NO_SHOW)
-- Chạy SAU patch_01 + patch_02. Chạy lại nhiều lần không bị trùng.
--   TEST-PEND-01     : PENDING, chưa cọc        → test chặn check-in + màn Đơn đặt phòng "Thu cọc"
--   TEST-CONF-NODEP  : CONFIRMED, THIẾU cọc     → test chặn check-in khi thiếu cọc
--   TEST-CONF-OK1    : CONFIRMED, đủ cọc (STD)  → chuỗi F10→F13
--   TEST-CONF-OK2    : CONFIRMED, đủ cọc (STD)  → test tranh phòng khi gán
-- =====================================================================
USE [SingleHotelManagementDB]
GO

IF NOT EXISTS (SELECT 1 FROM customers WHERE customer_code = 'TESTKH01')
INSERT INTO customers (customer_code, full_name, phone, id_document_type, id_document_number, nationality)
VALUES ('TESTKH01', N'Trần Văn Táo', '0901111222', 'CCCD', '099000000001', N'Việt Nam');

IF NOT EXISTS (SELECT 1 FROM customers WHERE customer_code = 'TESTKH02')
INSERT INTO customers (customer_code, full_name, phone, id_document_type, id_document_number, nationality)
VALUES ('TESTKH02', N'Lê Thị Mận', '0903333444', 'CCCD', '099000000002', N'Việt Nam');
GO

DECLARE @kh1 bigint = (SELECT customer_id FROM customers WHERE customer_code = 'TESTKH01');
DECLARE @kh2 bigint = (SELECT customer_id FROM customers WHERE customer_code = 'TESTKH02');
DECLARE @std bigint = (SELECT room_type_id FROM room_types WHERE type_code = 'STD');
DECLARE @ci date = CAST(GETDATE() AS date);
DECLARE @co date = DATEADD(DAY, 2, @ci);
DECLARE @rid bigint;

IF NOT EXISTS (SELECT 1 FROM reservations WHERE booking_code = 'TEST-PEND-01')
BEGIN
    INSERT INTO reservations (customer_id, booking_code, source_code, status_code, check_in_date, check_out_date,
        adult_count, child_count, room_subtotal, tax_amount, total_amount, deposit_required)
    VALUES (@kh1, 'TEST-PEND-01', 'RECEPTIONIST', 'PENDING', @ci, @co, 2, 0, 1600000, 160000, 1760000, 528000);
    SET @rid = SCOPE_IDENTITY();
    INSERT INTO reservation_rooms (reservation_id, room_type_id, quantity, adult_count, child_count,
        nightly_price_snapshot, number_of_nights, line_total)
    VALUES (@rid, @std, 1, 2, 0, 800000, 2, 1600000);
END

IF NOT EXISTS (SELECT 1 FROM reservations WHERE booking_code = 'TEST-CONF-NODEP')
BEGIN
    INSERT INTO reservations (customer_id, booking_code, source_code, status_code, check_in_date, check_out_date,
        adult_count, child_count, room_subtotal, tax_amount, total_amount, deposit_required)
    VALUES (@kh1, 'TEST-CONF-NODEP', 'RECEPTIONIST', 'CONFIRMED', @ci, @co, 2, 0, 1600000, 160000, 1760000, 528000);
    SET @rid = SCOPE_IDENTITY();
    INSERT INTO reservation_rooms (reservation_id, room_type_id, quantity, adult_count, child_count,
        nightly_price_snapshot, number_of_nights, line_total)
    VALUES (@rid, @std, 1, 2, 0, 800000, 2, 1600000);
    INSERT INTO payments (reservation_id, payment_type, method_code, amount, status_code, paid_at)
    VALUES (@rid, 'DEPOSIT', 'CASH', 100000, 'SUCCESS', SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM reservations WHERE booking_code = 'TEST-CONF-OK1')
BEGIN
    INSERT INTO reservations (customer_id, booking_code, source_code, status_code, check_in_date, check_out_date,
        adult_count, child_count, room_subtotal, tax_amount, total_amount, deposit_required)
    VALUES (@kh1, 'TEST-CONF-OK1', 'RECEPTIONIST', 'CONFIRMED', @ci, @co, 2, 0, 1600000, 160000, 1760000, 528000);
    SET @rid = SCOPE_IDENTITY();
    INSERT INTO reservation_rooms (reservation_id, room_type_id, quantity, adult_count, child_count,
        nightly_price_snapshot, number_of_nights, line_total)
    VALUES (@rid, @std, 1, 2, 0, 800000, 2, 1600000);
    INSERT INTO reservation_guests (reservation_id, full_name, id_document_type, id_document_number, is_primary_guest)
    VALUES (@rid, N'Trần Văn Táo', 'CCCD', '099000000001', 1);
    INSERT INTO payments (reservation_id, payment_type, method_code, amount, status_code, paid_at)
    VALUES (@rid, 'DEPOSIT', 'CASH', 528000, 'SUCCESS', SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM reservations WHERE booking_code = 'TEST-CONF-OK2')
BEGIN
    INSERT INTO reservations (customer_id, booking_code, source_code, status_code, check_in_date, check_out_date,
        adult_count, child_count, room_subtotal, tax_amount, total_amount, deposit_required)
    VALUES (@kh2, 'TEST-CONF-OK2', 'RECEPTIONIST', 'CONFIRMED', @ci, @co, 2, 0, 1600000, 160000, 1760000, 528000);
    SET @rid = SCOPE_IDENTITY();
    INSERT INTO reservation_rooms (reservation_id, room_type_id, quantity, adult_count, child_count,
        nightly_price_snapshot, number_of_nights, line_total)
    VALUES (@rid, @std, 1, 2, 0, 800000, 2, 1600000);
    INSERT INTO reservation_guests (reservation_id, full_name, id_document_type, id_document_number, is_primary_guest)
    VALUES (@rid, N'Lê Thị Mận', 'CCCD', '099000000002', 1);
    INSERT INTO payments (reservation_id, payment_type, method_code, amount, status_code, paid_at)
    VALUES (@rid, 'DEPOSIT', 'CASH', 528000, 'SUCCESS', SYSUTCDATETIME());
END
GO

SELECT booking_code, status_code, deposit_required,
       (SELECT COALESCE(SUM(amount),0) FROM payments p
        WHERE p.reservation_id = r.reservation_id AND p.status_code='SUCCESS') AS da_coc
FROM reservations r WHERE booking_code LIKE 'TEST-%';
GO

-- DỌN DẸP sau khi test (bỏ comment để chạy):
-- DELETE FROM room_assignments WHERE reservation_room_id IN (SELECT reservation_room_id FROM reservation_rooms rr JOIN reservations r ON r.reservation_id=rr.reservation_id WHERE r.booking_code LIKE 'TEST-%');
-- DELETE FROM invoice_items WHERE invoice_id IN (SELECT invoice_id FROM invoices WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%'));
-- DELETE FROM payments WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%');
-- DELETE FROM invoices WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%');
-- DELETE FROM service_requests WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%');
-- DELETE FROM reservation_guests WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%');
-- DELETE FROM reservation_rooms WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%');
-- DELETE FROM email_logs WHERE reservation_id IN (SELECT reservation_id FROM reservations WHERE booking_code LIKE 'TEST-%');
-- DELETE FROM reservations WHERE booking_code LIKE 'TEST-%';
-- DELETE FROM customers WHERE customer_code IN ('TESTKH01','TESTKH02');
