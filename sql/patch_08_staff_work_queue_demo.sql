/*
  Demo data for the shared Service Staff work queues.

  Prerequisite:
    - SingleHotelManagementDB.sql
    - patch_03_manager.sql (adds maintenance/housekeeping audit columns)

  Safe to run more than once. Existing demo rows are preserved and duplicate
  queue items are not created. The script uses currently unoccupied rooms only.
*/

USE SingleHotelManagementDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

IF COL_LENGTH('dbo.maintenance_tickets', 'updated_at') IS NULL
    THROW 51000, 'Run sql/patch_03_manager.sql before this demo script.', 1;
GO

DECLARE @ReporterUserId BIGINT;

SELECT TOP (1) @ReporterUserId = user_id
FROM dbo.users
WHERE status_code = 'ACTIVE'
  AND role_code IN ('SERVICE_STAFF', 'MANAGER', 'ADMIN')
ORDER BY CASE role_code WHEN 'SERVICE_STAFF' THEN 1 WHEN 'MANAGER' THEN 2 ELSE 3 END,
         user_id;

IF @ReporterUserId IS NULL
    THROW 51001, 'No active Service Staff, Manager, or Admin account is available as demo reporter.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @HousekeepingRooms TABLE (room_id BIGINT PRIMARY KEY);

    ;WITH CandidateRooms AS
    (
        SELECT TOP (4)
               r.room_id,
               ROW_NUMBER() OVER (ORDER BY r.room_id) AS row_no
        FROM dbo.rooms r
        JOIN dbo.room_types rt ON rt.room_type_id = r.room_type_id
        WHERE r.is_active = 1
          AND rt.is_active = 1
          AND r.operational_status <> 'OCCUPIED'
          AND NOT EXISTS
              (SELECT 1 FROM dbo.room_assignments ra
               WHERE ra.room_id = r.room_id AND ra.is_current = 1)
          AND NOT EXISTS
              (SELECT 1 FROM dbo.housekeeping_tasks h
               WHERE h.room_id = r.room_id
                 AND h.status_code IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS'))
        ORDER BY r.room_id
    )
    INSERT INTO dbo.housekeeping_tasks
        (room_id, reservation_id, assigned_staff_user_id, created_by_user_id,
         task_type, priority_code, status_code, scheduled_at, notes)
    OUTPUT inserted.room_id INTO @HousekeepingRooms(room_id)
    SELECT c.room_id,
           NULL,
           NULL,
           @ReporterUserId,
           CASE WHEN c.row_no % 2 = 0 THEN 'DEEP_CLEANING' ELSE 'CHECKOUT_CLEANING' END,
           CASE c.row_no WHEN 1 THEN 'URGENT' WHEN 2 THEN 'HIGH' ELSE 'NORMAL' END,
           'PENDING',
           DATEADD(minute, -c.row_no * 15, SYSUTCDATETIME()),
           CONCAT(N'STAFF-DEMO-HK-', RIGHT('000' + CONVERT(varchar(3), c.row_no), 3),
                  N': task chưa có người nhận')
    FROM CandidateRooms c
    WHERE NOT EXISTS
        (SELECT 1 FROM dbo.housekeeping_tasks h
         WHERE h.notes LIKE CONCAT(N'STAFF-DEMO-HK-',
               RIGHT('000' + CONVERT(varchar(3), c.row_no), 3), N':%'));

    -- Earlier versions scheduled demo work 1-4 hours in the future. Correct
    -- those existing unstarted demo rows so they can be tested immediately.
    UPDATE dbo.housekeeping_tasks
    SET scheduled_at = DATEADD(minute, -5, SYSUTCDATETIME()),
        updated_at = SYSUTCDATETIME()
    WHERE notes LIKE N'STAFF-DEMO-HK-%'
      AND status_code IN ('PENDING', 'ASSIGNED')
      AND (scheduled_at IS NULL OR scheduled_at > SYSUTCDATETIME());

    UPDATE r
    SET cleaning_status = 'DIRTY',
        updated_at = SYSUTCDATETIME()
    FROM dbo.rooms r
    JOIN @HousekeepingRooms d ON d.room_id = r.room_id;

    DECLARE @MaintenanceRooms TABLE (room_id BIGINT PRIMARY KEY);

    ;WITH CandidateRooms AS
    (
        SELECT TOP (4)
               r.room_id,
               ROW_NUMBER() OVER (ORDER BY r.room_id DESC) AS row_no
        FROM dbo.rooms r
        JOIN dbo.room_types rt ON rt.room_type_id = r.room_type_id
        WHERE r.is_active = 1
          AND rt.is_active = 1
          AND r.operational_status <> 'OCCUPIED'
          AND NOT EXISTS
              (SELECT 1 FROM dbo.room_assignments ra
               WHERE ra.room_id = r.room_id AND ra.is_current = 1)
          AND NOT EXISTS
              (SELECT 1 FROM dbo.maintenance_tickets m
               WHERE m.room_id = r.room_id
                 AND m.status_code NOT IN ('CLOSED', 'CANCELLED'))
        ORDER BY r.room_id DESC
    )
    INSERT INTO dbo.maintenance_tickets
        (room_id, reported_by_user_id, assigned_staff_user_id, ticket_code,
         title, description, priority_code, status_code, reported_at)
    OUTPUT inserted.room_id INTO @MaintenanceRooms(room_id)
    SELECT c.room_id,
           @ReporterUserId,
           NULL,
           CONCAT('STAFF-DEMO-MT-', RIGHT('000' + CONVERT(varchar(3), c.row_no), 3)),
           CASE c.row_no
               WHEN 1 THEN N'Điều hòa không làm lạnh'
               WHEN 2 THEN N'Đèn phòng chập chờn'
               WHEN 3 THEN N'Khóa cửa điện tử phản hồi chậm'
               ELSE N'Vòi nước bị rò rỉ'
           END,
           N'Dữ liệu demo cho hàng đợi Maintenance chung; chưa có nhân viên nhận.',
           CASE c.row_no WHEN 1 THEN 'URGENT' WHEN 2 THEN 'HIGH' ELSE 'NORMAL' END,
           'OPEN',
           DATEADD(minute, -c.row_no * 15, SYSUTCDATETIME())
    FROM CandidateRooms c
    WHERE NOT EXISTS
        (SELECT 1 FROM dbo.maintenance_tickets m
         WHERE m.ticket_code = CONCAT('STAFF-DEMO-MT-',
               RIGHT('000' + CONVERT(varchar(3), c.row_no), 3)));

    UPDATE r
    SET operational_status = CASE WHEN rt.is_active = 1 THEN 'MAINTENANCE' ELSE 'OUT_OF_SERVICE' END,
        updated_at = SYSUTCDATETIME()
    FROM dbo.rooms r
    JOIN @MaintenanceRooms d ON d.room_id = r.room_id
    JOIN dbo.room_types rt ON rt.room_type_id = r.room_type_id
    WHERE r.operational_status <> 'OCCUPIED';

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT
    (SELECT COUNT(*) FROM dbo.housekeeping_tasks
     WHERE notes LIKE N'STAFF-DEMO-HK-%') AS demo_housekeeping_tasks,
    (SELECT COUNT(*) FROM dbo.maintenance_tickets
     WHERE ticket_code LIKE 'STAFF-DEMO-MT-%') AS demo_maintenance_tickets;
GO
