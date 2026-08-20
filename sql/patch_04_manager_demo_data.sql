-- =====================================================================
-- MANAGER DEMO DATA
-- Chạy sau patch_01_required.sql, patch_03_manager.sql và patch_02_seed_data.sql.
-- Dữ liệu có mã MGR-DEMO/MGR* để có thể chạy lại mà không nhân bản.
-- =====================================================================
USE [SingleHotelManagementDB];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @Today date = CAST(SYSDATETIME() AS date);
    DECLARE @TodayAt8 datetime2 = DATEADD(hour, 8, CAST(@Today AS datetime2));
    DECLARE @ManagerId bigint = (SELECT user_id FROM users WHERE email = 'manager.demo@hotel.vn');
    DECLARE @StaffId bigint = (SELECT TOP (1) user_id FROM users WHERE role_code = 'SERVICE_STAFF' AND status_code = 'ACTIVE' ORDER BY user_id);
    DECLARE @ReceptionistId bigint = (SELECT TOP (1) user_id FROM users WHERE role_code = 'RECEPTIONIST' AND status_code = 'ACTIVE' ORDER BY user_id);
    DECLARE @CustomerId bigint = (SELECT TOP (1) customer_id FROM customers WHERE email = 'customer@test.vn' ORDER BY customer_id);

    IF @ManagerId IS NULL THROW 51000, 'Missing manager.demo@hotel.vn. Run patch_02_seed_data.sql first.', 1;
    IF @StaffId IS NULL THROW 51000, 'Missing active SERVICE_STAFF account.', 1;
    IF @ReceptionistId IS NULL THROW 51000, 'Missing active RECEPTIONIST account.', 1;
    IF @CustomerId IS NULL THROW 51000, 'Missing customer@test.vn profile.', 1;

    -- UC60-UC63: room types, amenities, images and activation state.
    DECLARE @Types TABLE (
        type_code varchar(20) PRIMARY KEY,
        type_name nvarchar(100),
        description nvarchar(max),
        max_adults smallint,
        max_children smallint,
        bed_type nvarchar(50),
        room_size_m2 decimal(6,2),
        base_price decimal(12,2),
        amenities_json nvarchar(max),
        images_json nvarchar(max),
        is_active bit
    );

    INSERT INTO @Types VALUES
    ('MGRSTD', N'Manager Demo Standard', N'Phòng tiêu chuẩn dùng để kiểm thử chức năng Manager.', 2, 1, N'1 giường Queen', 24, 900000,
     N'["WiFi","Smart TV","Điều hòa","Két an toàn"]',
     N'["https://picsum.photos/seed/manager-standard-1/900/600.jpg","https://picsum.photos/seed/manager-standard-2/900/600.jpg"]', 1),
    ('MGRDLX', N'Manager Demo Deluxe', N'Phòng deluxe có ban công và minibar.', 2, 2, N'1 giường King', 34, 1400000,
     N'["WiFi","Smart TV","Điều hòa","Minibar","Ban công","Bồn tắm"]',
     N'["https://picsum.photos/seed/manager-deluxe-1/900/600.jpg","https://picsum.photos/seed/manager-deluxe-2/900/600.jpg"]', 1),
    ('MGRSUT', N'Manager Demo Suite (Inactive)', N'Room type inactive để kiểm thử activate/deactivate.', 4, 2, N'2 giường King', 58, 2800000,
     N'["WiFi","Smart TV","Minibar","Phòng khách","Bồn tắm","Máy pha cà phê"]',
     N'["https://picsum.photos/seed/manager-suite-1/900/600.jpg"]', 0);

    UPDATE target
    SET target.type_name = source.type_name,
        target.description = source.description,
        target.max_adults = source.max_adults,
        target.max_children = source.max_children,
        target.bed_type = source.bed_type,
        target.room_size_m2 = source.room_size_m2,
        target.base_price = source.base_price,
        target.amenities_json = source.amenities_json,
        target.images_json = source.images_json,
        target.is_active = source.is_active,
        target.updated_at = SYSUTCDATETIME()
    FROM room_types target
    JOIN @Types source ON source.type_code = target.type_code;

    INSERT INTO room_types
        (type_code, type_name, description, max_adults, max_children, bed_type,
         room_size_m2, base_price, amenities_json, images_json, is_active)
    SELECT source.type_code, source.type_name, source.description, source.max_adults,
           source.max_children, source.bed_type, source.room_size_m2, source.base_price,
           source.amenities_json, source.images_json, source.is_active
    FROM @Types source
    WHERE NOT EXISTS (SELECT 1 FROM room_types target WHERE target.type_code = source.type_code);

    -- UC58-UC59: physical rooms with representative operational/cleaning states.
    DECLARE @Rooms TABLE (
        type_code varchar(20), room_number varchar(20) PRIMARY KEY, floor_number smallint,
        operational_status varchar(20), cleaning_status varchar(20), notes nvarchar(500), is_active bit
    );

    INSERT INTO @Rooms VALUES
    ('MGRSTD','MGR101',1,'AVAILABLE','READY',N'MGR-DEMO: phòng sẵn sàng bán',1),
    ('MGRSTD','MGR102',1,'AVAILABLE','DIRTY',N'MGR-DEMO: đang chờ housekeeping',1),
    ('MGRSTD','MGR201',2,'OCCUPIED','READY',N'MGR-DEMO: phòng đang có khách',1),
    ('MGRSTD','MGR202',2,'MAINTENANCE','DIRTY',N'MGR-DEMO: inspection fail, có maintenance issue',1),
    ('MGRDLX','MGR301',3,'BLOCKED','READY',N'MGR-DEMO: Manager chủ động block',1),
    ('MGRDLX','MGR302',3,'OUT_OF_SERVICE','DIRTY',N'MGR-DEMO: đang sửa khóa cửa',1),
    ('MGRDLX','MGR401',4,'MAINTENANCE','CLEAN',N'MGR-DEMO: issue đã resolved, chờ Manager đóng',1),
    ('MGRDLX','MGR402',4,'AVAILABLE','CLEANING',N'MGR-DEMO: housekeeping đang thực hiện',1),
    ('MGRSUT','MGR501',5,'OUT_OF_SERVICE','READY',N'MGR-DEMO: phòng thuộc room type inactive',0),
    ('MGRSTD','MGR103',1,'AVAILABLE','INSPECTED',N'MGR-DEMO: inspection PASS',1);

    UPDATE target
    SET target.room_type_id = rt.room_type_id,
        target.floor_number = source.floor_number,
        target.operational_status = source.operational_status,
        target.cleaning_status = source.cleaning_status,
        target.notes = source.notes,
        target.is_active = source.is_active,
        target.updated_at = SYSUTCDATETIME()
    FROM rooms target
    JOIN @Rooms source ON source.room_number = target.room_number
    JOIN room_types rt ON rt.type_code = source.type_code;

    INSERT INTO rooms
        (room_type_id, room_number, floor_number, operational_status, cleaning_status, notes, is_active)
    SELECT rt.room_type_id, source.room_number, source.floor_number, source.operational_status,
           source.cleaning_status, source.notes, source.is_active
    FROM @Rooms source
    JOIN room_types rt ON rt.type_code = source.type_code
    WHERE NOT EXISTS (SELECT 1 FROM rooms target WHERE target.room_number = source.room_number);

    -- UC64: daily/future rates and one stop-sell date.
    DECLARE @Rates TABLE (
        type_code varchar(20), rate_date date, nightly_price decimal(12,2), stop_sell bit,
        PRIMARY KEY(type_code, rate_date)
    );
    INSERT INTO @Rates VALUES
    ('MGRSTD',DATEADD(day,1,@Today),950000,0),
    ('MGRSTD',DATEADD(day,2,@Today),1050000,0),
    ('MGRSTD',DATEADD(day,3,@Today),1050000,1),
    ('MGRSTD',DATEADD(day,4,@Today),980000,0),
    ('MGRDLX',DATEADD(day,1,@Today),1500000,0),
    ('MGRDLX',DATEADD(day,2,@Today),1650000,0),
    ('MGRDLX',DATEADD(day,3,@Today),1650000,0);

    UPDATE target
    SET target.nightly_price = source.nightly_price,
        target.stop_sell = source.stop_sell,
        target.updated_at = SYSUTCDATETIME()
    FROM room_rates target
    JOIN room_types rt ON rt.room_type_id = target.room_type_id
    JOIN @Rates source ON source.type_code = rt.type_code AND source.rate_date = target.rate_date;

    INSERT INTO room_rates (room_type_id, rate_date, nightly_price, stop_sell)
    SELECT rt.room_type_id, source.rate_date, source.nightly_price, source.stop_sell
    FROM @Rates source
    JOIN room_types rt ON rt.type_code = source.type_code
    WHERE NOT EXISTS (
        SELECT 1 FROM room_rates target
        WHERE target.room_type_id = rt.room_type_id AND target.rate_date = source.rate_date
    );

    -- UC65-UC66: reservations provide arrivals, departures, new bookings and reports.
    DECLARE @Reservations TABLE (
        booking_code varchar(30) PRIMARY KEY, source_code varchar(20), status_code varchar(20),
        booked_at datetime2, check_in_date date, check_out_date date,
        actual_check_in_at datetime2 NULL, actual_check_out_at datetime2 NULL,
        adult_count smallint, child_count smallint, room_subtotal decimal(12,2),
        service_total decimal(12,2), tax_amount decimal(12,2), total_amount decimal(12,2),
        deposit_required decimal(12,2), cancellation_reason nvarchar(255) NULL
    );

    INSERT INTO @Reservations VALUES
    ('MGR-DEMO-ARRIVAL','ONLINE','CONFIRMED',DATEADD(hour,1,@TodayAt8),@Today,DATEADD(day,2,@Today),NULL,NULL,2,0,1800000,0,180000,1980000,594000,NULL),
    ('MGR-DEMO-DEPARTURE','RECEPTIONIST','CHECKED_IN',DATEADD(hour,1,CAST(DATEFROMPARTS(YEAR(@Today),MONTH(@Today),1) AS datetime2)),DATEADD(day,-2,@Today),@Today,DATEADD(hour,14,CAST(DATEADD(day,-2,@Today) AS datetime2)),NULL,2,1,2800000,500000,280000,3580000,924000,NULL),
    ('MGR-DEMO-CANCELLED','ONLINE','CANCELLED',DATEADD(hour,2,CAST(DATEFROMPARTS(YEAR(@Today),MONTH(@Today),1) AS datetime2)),DATEADD(day,7,@Today),DATEADD(day,9,@Today),NULL,NULL,2,0,1800000,0,180000,1980000,594000,N'Khách thay đổi kế hoạch'),
    ('MGR-DEMO-NEW','ONLINE','PENDING',DATEADD(hour,2,@TodayAt8),DATEADD(day,3,@Today),DATEADD(day,5,@Today),NULL,NULL,2,1,2800000,0,280000,3080000,924000,NULL),
    ('MGR-DEMO-CHECKEDOUT','RECEPTIONIST','CHECKED_OUT',DATEADD(hour,3,CAST(DATEFROMPARTS(YEAR(@Today),MONTH(@Today),1) AS datetime2)),DATEADD(day,-8,@Today),DATEADD(day,-5,@Today),DATEADD(hour,14,CAST(DATEADD(day,-8,@Today) AS datetime2)),DATEADD(hour,11,CAST(DATEADD(day,-5,@Today) AS datetime2)),2,0,2700000,0,270000,2970000,891000,NULL);

    UPDATE target
    SET target.customer_id = @CustomerId,
        target.created_by_user_id = CASE WHEN source.source_code='RECEPTIONIST' THEN @ReceptionistId ELSE NULL END,
        target.source_code = source.source_code,
        target.status_code = source.status_code,
        target.booked_at = source.booked_at,
        target.check_in_date = source.check_in_date,
        target.check_out_date = source.check_out_date,
        target.actual_check_in_at = source.actual_check_in_at,
        target.actual_check_out_at = source.actual_check_out_at,
        target.adult_count = source.adult_count,
        target.child_count = source.child_count,
        target.room_subtotal = source.room_subtotal,
        target.service_total = source.service_total,
        target.tax_amount = source.tax_amount,
        target.total_amount = source.total_amount,
        target.deposit_required = source.deposit_required,
        target.cancellation_reason = source.cancellation_reason,
        target.special_requests = N'MGR-DEMO: dữ liệu kiểm thử Manager',
        target.updated_at = SYSUTCDATETIME()
    FROM reservations target
    JOIN @Reservations source ON source.booking_code = target.booking_code;

    INSERT INTO reservations
        (customer_id, created_by_user_id, booking_code, source_code, status_code, booked_at,
         check_in_date, check_out_date, actual_check_in_at, actual_check_out_at,
         adult_count, child_count, room_subtotal, service_total, tax_amount, total_amount,
         deposit_required, special_requests, cancellation_reason)
    SELECT @CustomerId,
           CASE WHEN source.source_code='RECEPTIONIST' THEN @ReceptionistId ELSE NULL END,
           source.booking_code, source.source_code, source.status_code, source.booked_at,
           source.check_in_date, source.check_out_date, source.actual_check_in_at,
           source.actual_check_out_at, source.adult_count, source.child_count,
           source.room_subtotal, source.service_total, source.tax_amount, source.total_amount,
           source.deposit_required, N'MGR-DEMO: dữ liệu kiểm thử Manager', source.cancellation_reason
    FROM @Reservations source
    WHERE NOT EXISTS (SELECT 1 FROM reservations target WHERE target.booking_code = source.booking_code);

    DECLARE @ReservationLines TABLE (
        booking_code varchar(30), type_code varchar(20), quantity smallint,
        adults smallint, children smallint, nightly decimal(12,2), nights smallint,
        line_total decimal(12,2), PRIMARY KEY(booking_code,type_code)
    );
    INSERT INTO @ReservationLines VALUES
    ('MGR-DEMO-ARRIVAL','MGRSTD',1,2,0,900000,2,1800000),
    ('MGR-DEMO-DEPARTURE','MGRDLX',1,2,1,1400000,2,2800000),
    ('MGR-DEMO-CANCELLED','MGRSTD',1,2,0,900000,2,1800000),
    ('MGR-DEMO-NEW','MGRDLX',1,2,1,1400000,2,2800000),
    ('MGR-DEMO-CHECKEDOUT','MGRSTD',1,2,0,900000,3,2700000);

    UPDATE target
    SET target.quantity = source.quantity,
        target.adult_count = source.adults,
        target.child_count = source.children,
        target.nightly_price_snapshot = source.nightly,
        target.number_of_nights = source.nights,
        target.line_total = source.line_total,
        target.notes = N'MGR-DEMO'
    FROM reservation_rooms target
    JOIN reservations r ON r.reservation_id = target.reservation_id
    JOIN room_types rt ON rt.room_type_id = target.room_type_id
    JOIN @ReservationLines source ON source.booking_code = r.booking_code AND source.type_code = rt.type_code;

    INSERT INTO reservation_rooms
        (reservation_id, room_type_id, quantity, adult_count, child_count,
         nightly_price_snapshot, number_of_nights, line_total, notes)
    SELECT r.reservation_id, rt.room_type_id, source.quantity, source.adults, source.children,
           source.nightly, source.nights, source.line_total, N'MGR-DEMO'
    FROM @ReservationLines source
    JOIN reservations r ON r.booking_code = source.booking_code
    JOIN room_types rt ON rt.type_code = source.type_code
    WHERE NOT EXISTS (
        SELECT 1 FROM reservation_rooms target
        WHERE target.reservation_id = r.reservation_id AND target.room_type_id = rt.room_type_id
    );

    -- Revenue/payment samples. Only records with the MGR-DEMO provider reference are reset.
    DELETE FROM payments WHERE provider_reference LIKE 'MGR-DEMO-PAY-%';
    INSERT INTO payments
        (reservation_id, recorded_by_user_id, payment_type, method_code, amount,
         currency_code, status_code, provider_name, provider_reference, failure_reason, paid_at)
    SELECT r.reservation_id, @ReceptionistId, p.payment_type, p.method_code, p.amount,
           'VND', p.status_code, 'MANAGER_DEMO', p.reference,
           CASE WHEN p.status_code='FAILED' THEN N'Giao dịch demo thất bại' ELSE NULL END,
           CASE WHEN p.status_code='SUCCESS' THEN p.paid_at ELSE NULL END
    FROM (VALUES
        ('MGR-DEMO-ARRIVAL','DEPOSIT','CASH',CAST(594000 AS decimal(12,2)),'SUCCESS','MGR-DEMO-PAY-ARRIVAL',DATEADD(hour,10,CAST(@Today AS datetime2))),
        ('MGR-DEMO-DEPARTURE','FINAL_PAYMENT','CARD',CAST(2656000 AS decimal(12,2)),'SUCCESS','MGR-DEMO-PAY-DEPARTURE',DATEADD(hour,11,CAST(@Today AS datetime2))),
        ('MGR-DEMO-CHECKEDOUT','FINAL_PAYMENT','BANK_TRANSFER',CAST(2970000 AS decimal(12,2)),'SUCCESS','MGR-DEMO-PAY-PAST',DATEADD(hour,10,CAST(DATEFROMPARTS(YEAR(@Today),MONTH(@Today),1) AS datetime2))),
        ('MGR-DEMO-NEW','DEPOSIT','ONLINE',CAST(924000 AS decimal(12,2)),'FAILED','MGR-DEMO-PAY-FAILED',CAST(NULL AS datetime2))
    ) p(booking_code,payment_type,method_code,amount,status_code,reference,paid_at)
    JOIN reservations r ON r.booking_code = p.booking_code;

    -- Service performance report samples.
    DELETE FROM service_requests WHERE notes LIKE N'MGR-DEMO:%';
    DECLARE @StayReservationId bigint = (SELECT reservation_id FROM reservations WHERE booking_code='MGR-DEMO-DEPARTURE');
    DECLARE @SpaId bigint = (SELECT hotel_service_id FROM hotel_services WHERE service_code='SPA');
    DECLARE @LaundryId bigint = (SELECT hotel_service_id FROM hotel_services WHERE service_code='LAUNDRY');
    DECLARE @AirportId bigint = (SELECT hotel_service_id FROM hotel_services WHERE service_code='AIRPORT');
    IF @SpaId IS NULL OR @LaundryId IS NULL OR @AirportId IS NULL
        THROW 51000, 'Missing demo hotel services. Run patch_02_seed_data.sql first.', 1;

    INSERT INTO service_requests
        (reservation_id, customer_id, hotel_service_id, assigned_staff_user_id,
         quantity, unit_price_snapshot, total_amount, status_code, requested_at,
         assigned_at, started_at, completed_at, notes, scheduled_at)
    VALUES
    (@StayReservationId,@CustomerId,@SpaId,@StaffId,1,500000,500000,'COMPLETED',@TodayAt8,DATEADD(hour,1,@TodayAt8),DATEADD(hour,2,@TodayAt8),DATEADD(hour,3,@TodayAt8),N'MGR-DEMO: Spa đã hoàn tất',DATEADD(hour,2,@TodayAt8)),
    (@StayReservationId,@CustomerId,@LaundryId,@StaffId,3,50000,150000,'IN_PROGRESS',DATEADD(hour,1,@TodayAt8),DATEADD(hour,2,@TodayAt8),DATEADD(hour,3,@TodayAt8),NULL,N'MGR-DEMO: Giặt ủi đang xử lý',DATEADD(hour,4,@TodayAt8)),
    (@StayReservationId,@CustomerId,@AirportId,NULL,1,350000,350000,'PENDING',DATEADD(hour,2,@TodayAt8),NULL,NULL,NULL,N'MGR-DEMO: Đưa đón sân bay đang chờ',DATEADD(day,1,@TodayAt8));

    -- UC48: pending, assigned, in-progress, PASS and FAIL examples.
    DELETE FROM housekeeping_tasks WHERE notes LIKE N'MGR-DEMO:%';
    INSERT INTO housekeeping_tasks
        (room_id, assigned_staff_user_id, created_by_user_id, task_type, priority_code,
         status_code, scheduled_at, started_at, completed_at, notes,
         inspection_status, inspection_notes, inspected_by_user_id, inspected_at)
    SELECT r.room_id, d.staff_id, @ManagerId, d.task_type, d.priority_code,
           d.status_code, d.scheduled_at, d.started_at, d.completed_at, d.notes,
           d.inspection_status, d.inspection_notes,
           CASE WHEN d.inspection_status IS NULL THEN NULL ELSE @StaffId END, d.inspected_at
    FROM (VALUES
        ('MGR102',CAST(NULL AS bigint),'CHECKOUT_CLEANING','HIGH','PENDING',DATEADD(hour,4,@TodayAt8),CAST(NULL AS datetime2),CAST(NULL AS datetime2),N'MGR-DEMO: task chờ phân công',CAST(NULL AS varchar(20)),CAST(NULL AS nvarchar(1000)),CAST(NULL AS datetime2)),
        ('MGR302',@StaffId,'DEEP_CLEANING','URGENT','ASSIGNED',DATEADD(hour,1,@TodayAt8),NULL,NULL,N'MGR-DEMO: task đã giao nhân viên',NULL,NULL,NULL),
        ('MGR402',@StaffId,'STAYOVER_CLEANING','NORMAL','IN_PROGRESS',@TodayAt8,DATEADD(hour,1,@TodayAt8),NULL,N'MGR-DEMO: cleaning đang thực hiện',NULL,NULL,NULL),
        ('MGR103',@StaffId,'CHECKOUT_CLEANING','NORMAL','COMPLETED',DATEADD(day,-1,@TodayAt8),DATEADD(hour,1,DATEADD(day,-1,@TodayAt8)),DATEADD(hour,2,DATEADD(day,-1,@TodayAt8)),N'MGR-DEMO: inspection thành công','PASS',N'Phòng sạch và đầy đủ tiện nghi',DATEADD(hour,3,DATEADD(day,-1,@TodayAt8))),
        ('MGR202',@StaffId,'DEEP_CLEANING','HIGH','COMPLETED',DATEADD(day,-1,@TodayAt8),DATEADD(hour,1,DATEADD(day,-1,@TodayAt8)),DATEADD(hour,2,DATEADD(day,-1,@TodayAt8)),N'MGR-DEMO: inspection thất bại','FAIL',N'Phát hiện điều hòa chảy nước',DATEADD(hour,3,DATEADD(day,-1,@TodayAt8)))
    ) d(room_number,staff_id,task_type,priority_code,status_code,scheduled_at,started_at,completed_at,notes,inspection_status,inspection_notes,inspected_at)
    JOIN rooms r ON r.room_number = d.room_number;

    -- UC54/UC57: open, in-progress, resolved-awaiting-review and closed issues.
    DELETE FROM maintenance_tickets WHERE ticket_code LIKE 'MGR-DEMO-MT-%';
    INSERT INTO maintenance_tickets
        (room_id, reported_by_user_id, assigned_staff_user_id, ticket_code, title,
         description, priority_code, status_code, reported_at, started_at,
         resolved_at, resolution_note, closed_at)
    SELECT r.room_id, @StaffId, d.staff_id, d.ticket_code, d.title, d.description,
           d.priority_code, d.status_code, d.reported_at, d.started_at,
           d.resolved_at, d.resolution_note, d.closed_at
    FROM (VALUES
        ('MGR202',CAST(NULL AS bigint),'MGR-DEMO-MT-001',N'Điều hòa chảy nước',N'Phát hiện trong lần inspection housekeeping.','HIGH','OPEN',DATEADD(hour,3,DATEADD(day,-1,@TodayAt8)),CAST(NULL AS datetime2),CAST(NULL AS datetime2),CAST(NULL AS nvarchar(500)),CAST(NULL AS datetime2)),
        ('MGR302',@StaffId,'MGR-DEMO-MT-002',N'Khóa cửa điện tử không phản hồi',N'Nhân viên đang thay pin và kiểm tra bộ điều khiển.','URGENT','IN_PROGRESS',DATEADD(day,-1,@TodayAt8),DATEADD(hour,1,@TodayAt8),NULL,NULL,NULL),
        ('MGR401',@StaffId,'MGR-DEMO-MT-003',N'Đèn trần chập chờn',N'Đã thay driver, chờ Manager kiểm tra và đóng issue.','NORMAL','RESOLVED',DATEADD(day,-2,@TodayAt8),DATEADD(day,-1,@TodayAt8),DATEADD(hour,2,@TodayAt8),N'Đã thay driver LED và kiểm tra 30 phút.',NULL),
        ('MGR101',@StaffId,'MGR-DEMO-MT-004',N'Điều khiển TV hết pin',N'Issue lịch sử đã hoàn tất.','LOW','CLOSED',DATEADD(day,-5,@TodayAt8),DATEADD(day,-5,@TodayAt8),DATEADD(day,-4,@TodayAt8),N'Đã thay pin mới.',DATEADD(day,-4,@TodayAt8))
    ) d(room_number,staff_id,ticket_code,title,description,priority_code,status_code,reported_at,started_at,resolved_at,resolution_note,closed_at)
    JOIN rooms r ON r.room_number = d.room_number;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT 'Manager demo data ready' AS result,
       (SELECT COUNT(*) FROM room_types WHERE type_code LIKE 'MGR%') AS room_types,
       (SELECT COUNT(*) FROM rooms WHERE room_number LIKE 'MGR%') AS rooms,
       (SELECT COUNT(*) FROM reservations WHERE booking_code LIKE 'MGR-DEMO-%') AS reservations,
       (SELECT COUNT(*) FROM housekeeping_tasks WHERE notes LIKE N'MGR-DEMO:%') AS housekeeping_tasks,
       (SELECT COUNT(*) FROM maintenance_tickets WHERE ticket_code LIKE 'MGR-DEMO-MT-%') AS maintenance_issues;
GO
