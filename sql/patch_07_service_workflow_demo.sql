-- ============================================================================
-- SERVICE REQUEST WORKFLOW DEMO DATA
-- Run after: SingleHotelManagementDB.sql, patch_01_required.sql,
--            patch_05_service_data.sql and patch_06_workflow_consistency.sql.
--
-- Demo credentials:
--   service.customer1@test.vn / Customer@123  (CHECKED_IN + sample requests)
--   service.customer2@test.vn / Customer@123  (CHECKED_IN)
--   service.customer3@test.vn / Customer@123  (no active stay; negative test)
--   service.staff1@test.vn    / Staff@123     (GENERAL_SERVICE)
--   service.staff2@test.vn    / Staff@123     (GENERAL_SERVICE)
--
-- The script is additive and rerunnable: deterministic email/code/note markers
-- prevent duplicate users, stays, rooms, assignments and service requests.
-- ============================================================================
USE [SingleHotelManagementDB];
GO

SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
   OR OBJECT_ID(N'dbo.customers', N'U') IS NULL
   OR OBJECT_ID(N'dbo.reservations', N'U') IS NULL
   OR OBJECT_ID(N'dbo.service_requests', N'U') IS NULL
    THROW 51000, 'Run SingleHotelManagementDB.sql before this demo script.', 1;

IF COL_LENGTH('dbo.users', 'email_verified_at') IS NULL
   OR COL_LENGTH('dbo.users', 'failed_login_attempts') IS NULL
   OR COL_LENGTH('dbo.users', 'locked_until') IS NULL
   OR COL_LENGTH('dbo.service_requests', 'requested_for_at') IS NULL
    THROW 51000, 'Run patch_01_required.sql before this demo script.', 1;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @CustomerPassword varchar(255) =
        '65536:PUc8F/jYWpQ79CzcTU96gw==:vOv0AAX35PUtuXIzg1aq7iYtEUihQgxvbd37yR7ro5c='; -- Customer@123
    DECLARE @StaffPassword varchar(255) =
        '65536:4A7+JwhhVQrGkzAr3bd1aA==:+2sNrGOO485fdEui7NxKEVCd55sy0aHe9JUag2VmIcg='; -- Staff@123

    DECLARE @DemoUsers TABLE
    (
        email varchar(255) PRIMARY KEY,
        password_hash varchar(255) NOT NULL,
        full_name nvarchar(150) NOT NULL,
        phone varchar(30) NULL,
        role_code varchar(20) NOT NULL,
        department_code varchar(20) NULL
    );

    INSERT INTO @DemoUsers VALUES
        ('service.customer1@test.vn', @CustomerPassword, N'Khách Dịch Vụ An',   '0911000101', 'CUSTOMER', NULL),
        ('service.customer2@test.vn', @CustomerPassword, N'Khách Dịch Vụ Bình', '0911000102', 'CUSTOMER', NULL),
        ('service.customer3@test.vn', @CustomerPassword, N'Khách Chưa Check-in', '0911000103', 'CUSTOMER', NULL),
        ('service.staff1@test.vn',    @StaffPassword,    N'Nhân viên Dịch vụ Lan', '0922000201', 'SERVICE_STAFF', 'GENERAL_SERVICE'),
        ('service.staff2@test.vn',    @StaffPassword,    N'Nhân viên Dịch vụ Minh','0922000202', 'SERVICE_STAFF', 'GENERAL_SERVICE');

    MERGE dbo.users WITH (HOLDLOCK) AS target
    USING @DemoUsers AS source
       ON target.email = source.email
    WHEN MATCHED THEN UPDATE SET
        password_hash = source.password_hash,
        full_name = source.full_name,
        phone = source.phone,
        role_code = source.role_code,
        department_code = source.department_code,
        status_code = 'ACTIVE',
        failed_login_attempts = 0,
        locked_until = NULL,
        email_verified_at = COALESCE(target.email_verified_at, SYSUTCDATETIME()),
        updated_at = SYSUTCDATETIME()
    WHEN NOT MATCHED THEN
        INSERT (email, password_hash, full_name, phone, role_code,
                department_code, status_code, email_verified_at)
        VALUES (source.email, source.password_hash, source.full_name, source.phone,
                source.role_code, source.department_code, 'ACTIVE', SYSUTCDATETIME());

    DECLARE @DemoCustomers TABLE
    (
        email varchar(255) PRIMARY KEY,
        customer_code varchar(20) NOT NULL,
        full_name nvarchar(150) NOT NULL,
        phone varchar(30) NOT NULL,
        document_number varchar(50) NOT NULL
    );

    INSERT INTO @DemoCustomers VALUES
        ('service.customer1@test.vn', 'SVC-CUS-001', N'Khách Dịch Vụ An',    '0911000101', 'SVCID001'),
        ('service.customer2@test.vn', 'SVC-CUS-002', N'Khách Dịch Vụ Bình',  '0911000102', 'SVCID002'),
        ('service.customer3@test.vn', 'SVC-CUS-003', N'Khách Chưa Check-in', '0911000103', 'SVCID003');

    MERGE dbo.customers WITH (HOLDLOCK) AS target
    USING (
        SELECT u.user_id, d.email, d.customer_code, d.full_name, d.phone, d.document_number
        FROM @DemoCustomers d
        JOIN dbo.users u ON u.email = d.email
    ) AS source
       ON target.customer_code = source.customer_code
    WHEN MATCHED THEN UPDATE SET
        user_id = source.user_id,
        full_name = source.full_name,
        email = source.email,
        phone = source.phone,
        id_document_type = 'CCCD',
        id_document_number = source.document_number,
        nationality = N'Việt Nam',
        status_code = 'ACTIVE',
        updated_at = SYSUTCDATETIME()
    WHEN NOT MATCHED THEN
        INSERT (user_id, customer_code, full_name, email, phone,
                id_document_type, id_document_number, nationality, status_code)
        VALUES (source.user_id, source.customer_code, source.full_name, source.email,
                source.phone, 'CCCD', source.document_number, N'Việt Nam', 'ACTIVE');

    DECLARE @RoomTypeId bigint;
    DECLARE @NightlyPrice decimal(12,2);
    SELECT TOP (1) @RoomTypeId = room_type_id, @NightlyPrice = base_price
    FROM dbo.room_types
    WHERE is_active = 1
    ORDER BY room_type_id;

    IF @RoomTypeId IS NULL
        THROW 51000, 'At least one active room type is required for service demo stays.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.rooms WHERE room_number = 'SVCTEST-901')
        INSERT INTO dbo.rooms
            (room_type_id, room_number, floor_number, operational_status,
             cleaning_status, notes, is_active)
        VALUES
            (@RoomTypeId, 'SVCTEST-901', 9, 'AVAILABLE', 'CLEAN', N'[SVC-DEMO] Phòng test khách 1', 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.rooms WHERE room_number = 'SVCTEST-902')
        INSERT INTO dbo.rooms
            (room_type_id, room_number, floor_number, operational_status,
             cleaning_status, notes, is_active)
        VALUES
            (@RoomTypeId, 'SVCTEST-902', 9, 'AVAILABLE', 'CLEAN', N'[SVC-DEMO] Phòng test khách 2', 1);

    DECLARE @Customer1Id bigint =
        (SELECT customer_id FROM dbo.customers WHERE customer_code = 'SVC-CUS-001');
    DECLARE @Customer2Id bigint =
        (SELECT customer_id FROM dbo.customers WHERE customer_code = 'SVC-CUS-002');
    DECLARE @Staff1Id bigint =
        (SELECT user_id FROM dbo.users WHERE email = 'service.staff1@test.vn');
    DECLARE @Staff2Id bigint =
        (SELECT user_id FROM dbo.users WHERE email = 'service.staff2@test.vn');
    DECLARE @ReceptionistId bigint =
        (SELECT TOP (1) user_id FROM dbo.users
         WHERE role_code = 'RECEPTIONIST' AND status_code = 'ACTIVE' ORDER BY user_id);
    DECLARE @CheckInDate date = DATEADD(day, -1, CAST(GETDATE() AS date));
    DECLARE @CheckOutDate date = DATEADD(day, 3, CAST(GETDATE() AS date));
    DECLARE @Nights smallint = DATEDIFF(day, @CheckInDate, @CheckOutDate);
    DECLARE @RoomSubtotal decimal(12,2) = @NightlyPrice * DATEDIFF(day, @CheckInDate, @CheckOutDate);
    DECLARE @Deposit decimal(12,2) = ROUND(@RoomSubtotal * 0.20, 2);

    IF NOT EXISTS (SELECT 1 FROM dbo.reservations WHERE booking_code = 'SVC-DEMO-STAY-001')
        INSERT INTO dbo.reservations
            (customer_id, checked_in_by_user_id, booking_code, source_code, status_code,
             booked_at, check_in_date, check_out_date, actual_check_in_at,
             adult_count, child_count, room_subtotal, service_total, tax_amount,
             total_amount, deposit_required, special_requests)
        VALUES
            (@Customer1Id, @ReceptionistId, 'SVC-DEMO-STAY-001', 'ONLINE', 'CHECKED_IN',
             DATEADD(day,-5,SYSUTCDATETIME()), @CheckInDate, @CheckOutDate,
             DATEADD(day,-1,SYSUTCDATETIME()), 2, 0, @RoomSubtotal, 0, 0,
             @RoomSubtotal, @Deposit, N'[SVC-DEMO] Kỳ ở để test toàn bộ yêu cầu dịch vụ');

    IF NOT EXISTS (SELECT 1 FROM dbo.reservations WHERE booking_code = 'SVC-DEMO-STAY-002')
        INSERT INTO dbo.reservations
            (customer_id, checked_in_by_user_id, booking_code, source_code, status_code,
             booked_at, check_in_date, check_out_date, actual_check_in_at,
             adult_count, child_count, room_subtotal, service_total, tax_amount,
             total_amount, deposit_required, special_requests)
        VALUES
            (@Customer2Id, @ReceptionistId, 'SVC-DEMO-STAY-002', 'ONLINE', 'CHECKED_IN',
             DATEADD(day,-4,SYSUTCDATETIME()), @CheckInDate, @CheckOutDate,
             DATEADD(day,-1,SYSUTCDATETIME()), 1, 0, @RoomSubtotal, 0, 0,
             @RoomSubtotal, @Deposit, N'[SVC-DEMO] Kỳ ở trống để test đặt yêu cầu mới');

    DECLARE @Reservation1Id bigint =
        (SELECT reservation_id FROM dbo.reservations WHERE booking_code = 'SVC-DEMO-STAY-001');
    DECLARE @Reservation2Id bigint =
        (SELECT reservation_id FROM dbo.reservations WHERE booking_code = 'SVC-DEMO-STAY-002');

    IF NOT EXISTS (SELECT 1 FROM dbo.reservation_rooms WHERE reservation_id = @Reservation1Id)
        INSERT INTO dbo.reservation_rooms
            (reservation_id, room_type_id, quantity, adult_count, child_count,
             nightly_price_snapshot, number_of_nights, line_total, notes)
        VALUES (@Reservation1Id, @RoomTypeId, 1, 2, 0, @NightlyPrice, @Nights,
                @RoomSubtotal, N'[SVC-DEMO] Room line 1');

    IF NOT EXISTS (SELECT 1 FROM dbo.reservation_rooms WHERE reservation_id = @Reservation2Id)
        INSERT INTO dbo.reservation_rooms
            (reservation_id, room_type_id, quantity, adult_count, child_count,
             nightly_price_snapshot, number_of_nights, line_total, notes)
        VALUES (@Reservation2Id, @RoomTypeId, 1, 1, 0, @NightlyPrice, @Nights,
                @RoomSubtotal, N'[SVC-DEMO] Room line 2');

    DECLARE @ReservationRoom1Id bigint =
        (SELECT TOP (1) reservation_room_id FROM dbo.reservation_rooms
         WHERE reservation_id = @Reservation1Id ORDER BY reservation_room_id);
    DECLARE @ReservationRoom2Id bigint =
        (SELECT TOP (1) reservation_room_id FROM dbo.reservation_rooms
         WHERE reservation_id = @Reservation2Id ORDER BY reservation_room_id);
    DECLARE @Room1Id bigint = (SELECT room_id FROM dbo.rooms WHERE room_number = 'SVCTEST-901');
    DECLARE @Room2Id bigint = (SELECT room_id FROM dbo.rooms WHERE room_number = 'SVCTEST-902');

    IF EXISTS (SELECT 1 FROM dbo.room_assignments ra
               JOIN dbo.reservation_rooms rr ON rr.reservation_room_id = ra.reservation_room_id
               WHERE ra.room_id = @Room1Id AND ra.is_current = 1
                 AND rr.reservation_id <> @Reservation1Id)
        THROW 51000, 'SVCTEST-901 already belongs to another active reservation.', 1;

    IF EXISTS (SELECT 1 FROM dbo.room_assignments ra
               JOIN dbo.reservation_rooms rr ON rr.reservation_room_id = ra.reservation_room_id
               WHERE ra.room_id = @Room2Id AND ra.is_current = 1
                 AND rr.reservation_id <> @Reservation2Id)
        THROW 51000, 'SVCTEST-902 already belongs to another active reservation.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.room_assignments
                   WHERE reservation_room_id = @ReservationRoom1Id AND is_current = 1)
        INSERT INTO dbo.room_assignments
            (reservation_room_id, room_id, assigned_by_user_id, assigned_at, is_current)
        VALUES (@ReservationRoom1Id, @Room1Id, @ReceptionistId,
                DATEADD(day,-1,SYSUTCDATETIME()), 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.room_assignments
                   WHERE reservation_room_id = @ReservationRoom2Id AND is_current = 1)
        INSERT INTO dbo.room_assignments
            (reservation_room_id, room_id, assigned_by_user_id, assigned_at, is_current)
        VALUES (@ReservationRoom2Id, @Room2Id, @ReceptionistId,
                DATEADD(day,-1,SYSUTCDATETIME()), 1);

    UPDATE dbo.rooms
    SET operational_status = 'OCCUPIED', cleaning_status = 'CLEAN', is_active = 1,
        updated_at = SYSUTCDATETIME()
    WHERE room_id IN (@Room1Id, @Room2Id);

    IF NOT EXISTS (SELECT 1 FROM dbo.reservation_guests
                   WHERE reservation_id = @Reservation1Id AND is_primary_guest = 1)
        INSERT INTO dbo.reservation_guests
            (reservation_id, customer_id, full_name, id_document_type,
             id_document_number, nationality, is_primary_guest)
        VALUES (@Reservation1Id, @Customer1Id, N'Khách Dịch Vụ An',
                'CCCD', 'SVCID001', N'Việt Nam', 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.reservation_guests
                   WHERE reservation_id = @Reservation2Id AND is_primary_guest = 1)
        INSERT INTO dbo.reservation_guests
            (reservation_id, customer_id, full_name, id_document_type,
             id_document_number, nationality, is_primary_guest)
        VALUES (@Reservation2Id, @Customer2Id, N'Khách Dịch Vụ Bình',
                'CCCD', 'SVCID002', N'Việt Nam', 1);

    DECLARE @Service1Id bigint;
    DECLARE @Service2Id bigint;
    DECLARE @Service3Id bigint;
    SELECT @Service1Id = MIN(hotel_service_id) FROM dbo.hotel_services WHERE is_active = 1;
    SELECT @Service2Id = MIN(hotel_service_id) FROM dbo.hotel_services
    WHERE is_active = 1 AND hotel_service_id > @Service1Id;
    SELECT @Service3Id = MIN(hotel_service_id) FROM dbo.hotel_services
    WHERE is_active = 1 AND hotel_service_id > COALESCE(@Service2Id, @Service1Id);
    SET @Service2Id = COALESCE(@Service2Id, @Service1Id);
    SET @Service3Id = COALESCE(@Service3Id, @Service2Id, @Service1Id);

    IF @Service1Id IS NULL
        THROW 51000, 'Run patch_05_service_data.sql to create active hotel services.', 1;

    DECLARE @Price1 decimal(12,2) =
        (SELECT unit_price FROM dbo.hotel_services WHERE hotel_service_id = @Service1Id);
    DECLARE @Price2 decimal(12,2) =
        (SELECT unit_price FROM dbo.hotel_services WHERE hotel_service_id = @Service2Id);
    DECLARE @Price3 decimal(12,2) =
        (SELECT unit_price FROM dbo.hotel_services WHERE hotel_service_id = @Service3Id);

    IF NOT EXISTS (SELECT 1 FROM dbo.service_requests
                   WHERE reservation_id = @Reservation1Id AND notes LIKE N'[[]SVC-DEMO] PENDING%')
        INSERT INTO dbo.service_requests
            (reservation_id, customer_id, hotel_service_id, quantity,
             unit_price_snapshot, total_amount, status_code,
             requested_at, requested_for_at, notes)
        VALUES (@Reservation1Id, @Customer1Id, @Service1Id, 1,
                @Price1, @Price1, 'PENDING', SYSUTCDATETIME(),
                DATEADD(hour,2,SYSUTCDATETIME()), N'[SVC-DEMO] PENDING');

    IF NOT EXISTS (SELECT 1 FROM dbo.service_requests
                   WHERE reservation_id = @Reservation1Id AND notes LIKE N'[[]SVC-DEMO] ASSIGNED%')
        INSERT INTO dbo.service_requests
            (reservation_id, customer_id, hotel_service_id, assigned_staff_user_id,
             quantity, unit_price_snapshot, total_amount, status_code,
             requested_at, requested_for_at, assigned_at, notes)
        VALUES (@Reservation1Id, @Customer1Id, @Service2Id, @Staff1Id,
                2, @Price2, @Price2 * 2, 'ASSIGNED', SYSUTCDATETIME(),
                DATEADD(hour,3,SYSUTCDATETIME()), SYSUTCDATETIME(), N'[SVC-DEMO] ASSIGNED');

    IF NOT EXISTS (SELECT 1 FROM dbo.service_requests
                   WHERE reservation_id = @Reservation1Id AND notes LIKE N'[[]SVC-DEMO] IN_PROGRESS%')
        INSERT INTO dbo.service_requests
            (reservation_id, customer_id, hotel_service_id, assigned_staff_user_id,
             quantity, unit_price_snapshot, total_amount, status_code,
             requested_at, requested_for_at, assigned_at, started_at, notes)
        VALUES (@Reservation1Id, @Customer1Id, @Service3Id, @Staff2Id,
                1, @Price3, @Price3, 'IN_PROGRESS', DATEADD(hour,-1,SYSUTCDATETIME()),
                SYSUTCDATETIME(), DATEADD(minute,-50,SYSUTCDATETIME()),
                DATEADD(minute,-20,SYSUTCDATETIME()), N'[SVC-DEMO] IN_PROGRESS');

    IF NOT EXISTS (SELECT 1 FROM dbo.service_requests
                   WHERE reservation_id = @Reservation1Id AND notes LIKE N'[[]SVC-DEMO] COMPLETED%')
    BEGIN
        INSERT INTO dbo.service_requests
            (reservation_id, customer_id, hotel_service_id, assigned_staff_user_id,
             quantity, unit_price_snapshot, total_amount, status_code,
             requested_at, requested_for_at, assigned_at, started_at, completed_at, notes)
        VALUES (@Reservation1Id, @Customer1Id, @Service1Id, @Staff1Id,
                1, @Price1, @Price1, 'COMPLETED', DATEADD(hour,-6,SYSUTCDATETIME()),
                DATEADD(hour,-5,SYSUTCDATETIME()), DATEADD(hour,-5,SYSUTCDATETIME()),
                DATEADD(hour,-4,SYSUTCDATETIME()), DATEADD(hour,-3,SYSUTCDATETIME()),
                N'[SVC-DEMO] COMPLETED');

        UPDATE dbo.reservations
        SET service_total = service_total + @Price1,
            total_amount = total_amount + @Price1,
            updated_at = SYSUTCDATETIME()
        WHERE reservation_id = @Reservation1Id;
    END;

    IF NOT EXISTS (SELECT 1 FROM dbo.service_requests
                   WHERE reservation_id = @Reservation2Id AND notes LIKE N'[[]SVC-DEMO] CANCELLED%')
        INSERT INTO dbo.service_requests
            (reservation_id, customer_id, hotel_service_id, quantity,
             unit_price_snapshot, total_amount, status_code,
             requested_at, requested_for_at, notes)
        VALUES (@Reservation2Id, @Customer2Id, @Service2Id, 1,
                @Price2, @Price2, 'CANCELLED', DATEADD(hour,-2,SYSUTCDATETIME()),
                DATEADD(hour,-1,SYSUTCDATETIME()), N'[SVC-DEMO] CANCELLED');

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Verification results
SELECT email, full_name, role_code, department_code, status_code, email_verified_at
FROM dbo.users
WHERE email LIKE 'service.%@test.vn'
ORDER BY role_code, email;

SELECT c.customer_code, c.full_name, r.booking_code, r.status_code,
       rm.room_number, r.check_in_date, r.check_out_date
FROM dbo.customers c
LEFT JOIN dbo.reservations r ON r.customer_id = c.customer_id
LEFT JOIN dbo.reservation_rooms rr ON rr.reservation_id = r.reservation_id
LEFT JOIN dbo.room_assignments ra
  ON ra.reservation_room_id = rr.reservation_room_id AND ra.is_current = 1
LEFT JOIN dbo.rooms rm ON rm.room_id = ra.room_id
WHERE c.customer_code LIKE 'SVC-CUS-%'
ORDER BY c.customer_code;

SELECT sr.service_request_id, c.customer_code, r.booking_code, hs.service_name,
       sr.status_code, u.email AS assigned_staff, sr.quantity,
       sr.total_amount, sr.requested_for_at, sr.notes
FROM dbo.service_requests sr
JOIN dbo.customers c ON c.customer_id = sr.customer_id
JOIN dbo.reservations r ON r.reservation_id = sr.reservation_id
JOIN dbo.hotel_services hs ON hs.hotel_service_id = sr.hotel_service_id
LEFT JOIN dbo.users u ON u.user_id = sr.assigned_staff_user_id
WHERE sr.notes LIKE N'[[]SVC-DEMO]%'
ORDER BY sr.service_request_id;
GO
