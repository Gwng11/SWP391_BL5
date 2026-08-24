/*
    Demo inventory for customer room-search and reservation testing.

    Safe to run more than once:
    - the room type is identified by the unique code TESTAVL;
    - physical rooms are identified by their unique room numbers;
    - existing reservations, assignments and operational rooms are not changed.
*/

USE SingleHotelManagementDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM dbo.room_types WHERE type_code = 'TESTAVL')
    BEGIN
        INSERT INTO dbo.room_types
            (type_code, type_name, [description], max_adults, max_children,
             bed_type, room_size_m2, base_price, amenities_json, images_json,
             is_active)
        VALUES
            ('TESTAVL', N'Phòng kiểm thử còn trống',
             N'Loại phòng dùng để kiểm thử tìm phòng, đặt phòng và check-in.',
             2, 2, N'1 giường đôi', 28.00, 900000.00,
             N'["WiFi","TV","Điều hòa","Nước uống"]', NULL, 1);
    END;

    DECLARE @RoomTypeId bigint =
        (SELECT room_type_id FROM dbo.room_types WHERE type_code = 'TESTAVL');

    IF @RoomTypeId IS NULL
        THROW 51000, 'Cannot create or find TESTAVL room type.', 1;

    -- Make the dedicated demo type requestable again without touching its history.
    UPDATE dbo.room_types
    SET type_name = N'Phòng kiểm thử còn trống',
        [description] = N'Loại phòng dùng để kiểm thử tìm phòng, đặt phòng và check-in.',
        max_adults = 2,
        max_children = 2,
        bed_type = N'1 giường đôi',
        room_size_m2 = 28.00,
        base_price = 900000.00,
        amenities_json = N'["WiFi","TV","Điều hòa","Nước uống"]',
        is_active = 1,
        updated_at = SYSUTCDATETIME()
    WHERE room_type_id = @RoomTypeId;

    DECLARE @DemoRooms TABLE
    (
        room_number varchar(20) PRIMARY KEY,
        floor_number smallint NOT NULL
    );

    INSERT INTO @DemoRooms (room_number, floor_number)
    VALUES ('TEST-801', 8), ('TEST-802', 8), ('TEST-803', 8),
           ('TEST-804', 8), ('TEST-805', 8), ('TEST-806', 8);

    INSERT INTO dbo.rooms
        (room_type_id, room_number, floor_number, operational_status,
         cleaning_status, notes, is_active)
    SELECT @RoomTypeId, source.room_number, source.floor_number,
           'AVAILABLE', 'INSPECTED', N'[ROOM-AVAILABILITY-DEMO]', 1
    FROM @DemoRooms source
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.rooms target
        WHERE target.room_number = source.room_number
    );

    COMMIT TRANSACTION;

    DECLARE @CheckIn date = CAST(GETDATE() AS date);
    DECLARE @CheckOut date = DATEADD(day, 2, @CheckIn);

    SELECT rt.room_type_id,
           rt.type_code,
           rt.type_name,
           rt.max_adults,
           rt.max_children,
           rt.base_price,
           (SELECT COUNT(*) FROM dbo.rooms demo
            WHERE demo.room_type_id = rt.room_type_id
              AND demo.room_number LIKE 'TEST-80[1-6]') AS demo_room_count,
           (SELECT COUNT(*) FROM dbo.rooms sellable
            WHERE sellable.room_type_id = rt.room_type_id
              AND sellable.room_number LIKE 'TEST-80[1-6]'
              AND sellable.is_active = 1
              AND sellable.operational_status = 'AVAILABLE'
              AND NOT EXISTS
                  (SELECT 1 FROM dbo.maintenance_tickets mt
                   WHERE mt.room_id = sellable.room_id
                     AND mt.status_code NOT IN ('CLOSED', 'CANCELLED'))
           ) AS sellable_room_count,
           @CheckIn AS suggested_check_in,
           @CheckOut AS suggested_check_out
    FROM dbo.room_types rt
    WHERE rt.room_type_id = @RoomTypeId;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
