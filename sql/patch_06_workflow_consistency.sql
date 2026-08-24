-- =====================================================================
-- SERVICE REQUEST + HOUSEKEEPING WORKFLOW CONSISTENCY
-- Run after SingleHotelManagementDB.sql and the currently required schema/data
-- patches (01, 03, 05; patch 04 only when Manager demo data is wanted).
-- Safe, additive/reference-data repair for SQL Server.
-- =====================================================================
USE [SingleHotelManagementDB];
GO

SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    -- Normalize legacy operational staff roles to the role/department model
    -- used by Java, AuthFilter and the canonical schema. User IDs are retained,
    -- so existing task/maintenance assignments keep pointing to the same user.
    IF EXISTS (
        SELECT 1 FROM dbo.users
        WHERE role_code IN ('HOUSEKEEPING_STAFF', 'MAINTENANCE_STAFF')
    )
    BEGIN
        IF OBJECT_ID('dbo.CK_users_role_department', 'C') IS NOT NULL
            ALTER TABLE dbo.users DROP CONSTRAINT CK_users_role_department;
        IF OBJECT_ID('dbo.CK_users_role', 'C') IS NOT NULL
            ALTER TABLE dbo.users DROP CONSTRAINT CK_users_role;

        UPDATE dbo.users
        SET department_code = CASE role_code
                WHEN 'HOUSEKEEPING_STAFF' THEN 'HOUSEKEEPING'
                WHEN 'MAINTENANCE_STAFF' THEN 'MAINTENANCE'
            END,
            role_code = 'SERVICE_STAFF',
            updated_at = SYSUTCDATETIME()
        WHERE role_code IN ('HOUSEKEEPING_STAFF', 'MAINTENANCE_STAFF');
    END;

    IF EXISTS (
        SELECT 1 FROM dbo.users
        WHERE role_code NOT IN ('CUSTOMER','RECEPTIONIST','SERVICE_STAFF','MANAGER','ADMIN')
           OR (role_code='SERVICE_STAFF'
               AND (department_code IS NULL
                    OR department_code NOT IN ('GENERAL_SERVICE','HOUSEKEEPING','MAINTENANCE')))
           OR (role_code IN ('CUSTOMER','RECEPTIONIST','MANAGER','ADMIN')
               AND department_code IS NOT NULL)
    )
        THROW 51000, 'Users contain unsupported role/department reference data.', 1;

    IF OBJECT_ID('dbo.CK_users_role', 'C') IS NULL
        ALTER TABLE dbo.users WITH CHECK ADD CONSTRAINT CK_users_role CHECK (
            role_code IN ('CUSTOMER','RECEPTIONIST','SERVICE_STAFF','MANAGER','ADMIN')
        );

    IF OBJECT_ID('dbo.CK_users_role_department', 'C') IS NULL
        ALTER TABLE dbo.users WITH CHECK ADD CONSTRAINT CK_users_role_department CHECK (
            (role_code='SERVICE_STAFF'
                AND department_code IN ('GENERAL_SERVICE','HOUSEKEEPING','MAINTENANCE'))
            OR
            (role_code IN ('CUSTOMER','RECEPTIONIST','MANAGER','ADMIN')
                AND department_code IS NULL)
        );

    -- Backfill visible work for DIRTY rooms that currently have no active task.
    -- Reuse the most recent checked-out reservation/physical-room relationship
    -- when one exists; otherwise create a generic deep-clean task without
    -- fabricating a reservation reference.
    INSERT INTO dbo.housekeeping_tasks
        (room_id, reservation_id, assigned_staff_user_id, created_by_user_id,
         task_type, priority_code, status_code, scheduled_at, notes)
    SELECT r.room_id,
           latest.reservation_id,
           NULL,
           NULL,
           CASE WHEN latest.reservation_id IS NULL
                THEN 'DEEP_CLEANING' ELSE 'CHECKOUT_CLEANING' END,
           'NORMAL',
           'PENDING',
           SYSUTCDATETIME(),
           N'[PATCH06] Tạo task cho phòng DIRTY chưa có công việc housekeeping'
    FROM dbo.rooms r
    OUTER APPLY (
        SELECT TOP (1) rr.reservation_id
        FROM dbo.room_assignments ra
        JOIN dbo.reservation_rooms rr
          ON rr.reservation_room_id = ra.reservation_room_id
        JOIN dbo.reservations rv
          ON rv.reservation_id = rr.reservation_id
        WHERE ra.room_id = r.room_id
          AND rv.status_code = 'CHECKED_OUT'
        ORDER BY COALESCE(ra.unassigned_at, ra.assigned_at) DESC
    ) latest
    WHERE r.cleaning_status = 'DIRTY'
      AND NOT EXISTS (
          SELECT 1 FROM dbo.housekeeping_tasks h WITH (UPDLOCK, HOLDLOCK)
          WHERE h.room_id = r.room_id
            AND h.status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')
      );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Verification summary (read-only result sets).
SELECT role_code, department_code, COUNT(*) AS user_count
FROM dbo.users
GROUP BY role_code, department_code
ORDER BY role_code, department_code;

SELECT h.housekeeping_task_id, h.room_id, r.room_number, h.reservation_id,
       h.task_type, h.status_code, h.notes
FROM dbo.housekeeping_tasks h
JOIN dbo.rooms r ON r.room_id = h.room_id
WHERE h.notes LIKE N'[[]PATCH06]%'
ORDER BY h.housekeeping_task_id;
GO
