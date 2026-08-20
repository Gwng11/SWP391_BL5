-- =====================================================================
-- MANAGER FEATURES (UC06/07/10/11/48/54/57-66)
-- Safe, additive and idempotent migration for SQL Server.
-- =====================================================================
USE [SingleHotelManagementDB]
GO

SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

-- UC10/UC11: employee personal profile fields allowed by the SRS.
IF COL_LENGTH('dbo.users', 'address') IS NULL
    ALTER TABLE dbo.users ADD address nvarchar(500) NULL;
GO

IF COL_LENGTH('dbo.users', 'identification_number') IS NULL
    ALTER TABLE dbo.users ADD identification_number varchar(100) NULL;
GO

-- UC59: retain existing occupancy-compatible states and add explicit
-- blocked/inactive states. is_active remains a separate sales switch.
IF OBJECT_ID('dbo.CK_rooms_operational_status', 'C') IS NOT NULL
    ALTER TABLE dbo.rooms DROP CONSTRAINT CK_rooms_operational_status;
GO
ALTER TABLE dbo.rooms WITH CHECK ADD CONSTRAINT CK_rooms_operational_status CHECK (
    operational_status IN ('AVAILABLE','OCCUPIED','MAINTENANCE','OUT_OF_SERVICE','BLOCKED','INACTIVE')
);
GO

-- READY is the post-inspection state. INSPECTED remains accepted for
-- backward compatibility with existing data and older deployments.
IF OBJECT_ID('dbo.CK_rooms_cleaning_status', 'C') IS NOT NULL
    ALTER TABLE dbo.rooms DROP CONSTRAINT CK_rooms_cleaning_status;
GO
ALTER TABLE dbo.rooms WITH CHECK ADD CONSTRAINT CK_rooms_cleaning_status CHECK (
    cleaning_status IN ('DIRTY','CLEANING','CLEAN','INSPECTED','READY')
);
GO

-- UC63 enforcement/backfill: rooms of an inactive type cannot remain sellable.
-- Occupied rooms keep serving their current guest and are moved to
-- OUT_OF_SERVICE by the checkout transaction.
UPDATE r
SET operational_status = CASE WHEN r.operational_status='OCCUPIED' THEN 'OCCUPIED' ELSE 'OUT_OF_SERVICE' END,
    is_active = 0,
    updated_at = SYSUTCDATETIME()
FROM dbo.rooms r
JOIN dbo.room_types rt ON rt.room_type_id = r.room_type_id
WHERE rt.is_active = 0;
GO

-- UC48: inspection result and audit fields used by Service Staff while
-- Manager can create, assign, reassign and monitor tasks.
IF COL_LENGTH('dbo.housekeeping_tasks', 'inspection_status') IS NULL
    ALTER TABLE dbo.housekeeping_tasks ADD inspection_status varchar(20) NULL;
GO
IF COL_LENGTH('dbo.housekeeping_tasks', 'inspection_notes') IS NULL
    ALTER TABLE dbo.housekeeping_tasks ADD inspection_notes nvarchar(1000) NULL;
GO
IF COL_LENGTH('dbo.housekeeping_tasks', 'inspected_by_user_id') IS NULL
    ALTER TABLE dbo.housekeeping_tasks ADD inspected_by_user_id bigint NULL;
GO
IF COL_LENGTH('dbo.housekeeping_tasks', 'inspected_at') IS NULL
    ALTER TABLE dbo.housekeeping_tasks ADD inspected_at datetime2(0) NULL;
GO
IF COL_LENGTH('dbo.housekeeping_tasks', 'updated_at') IS NULL
    ALTER TABLE dbo.housekeeping_tasks ADD updated_at datetime2(0) NOT NULL
        CONSTRAINT DF_housekeeping_tasks_updated DEFAULT sysutcdatetime();
GO

IF OBJECT_ID('dbo.CK_housekeeping_inspection', 'C') IS NULL
    ALTER TABLE dbo.housekeeping_tasks WITH CHECK ADD CONSTRAINT CK_housekeeping_inspection
        CHECK (inspection_status IS NULL OR inspection_status IN ('PASS','FAIL'));
GO

IF OBJECT_ID('dbo.FK_housekeeping_inspected_by', 'F') IS NULL
    ALTER TABLE dbo.housekeeping_tasks WITH CHECK ADD CONSTRAINT FK_housekeeping_inspected_by
        FOREIGN KEY (inspected_by_user_id) REFERENCES dbo.users(user_id);
GO

-- SQL Server filtered indexes do not support the required multi-status
-- predicate. Duplicate active work is therefore enforced by a SERIALIZABLE
-- range lock in HousekeepingRepository.insert. Remove the helper column from
-- early development versions of this patch if it is present and unused.
IF COL_LENGTH('dbo.housekeeping_tasks', 'active_room_id') IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys.index_columns WHERE object_id=OBJECT_ID('dbo.housekeeping_tasks')
                AND column_id=COLUMNPROPERTY(OBJECT_ID('dbo.housekeeping_tasks'),'active_room_id','ColumnId'))
    ALTER TABLE dbo.housekeeping_tasks DROP COLUMN active_room_id;
GO

-- UC54/UC57: update/close audit fields for the existing maintenance state machine.
IF COL_LENGTH('dbo.maintenance_tickets', 'closed_at') IS NULL
    ALTER TABLE dbo.maintenance_tickets ADD closed_at datetime2(0) NULL;
GO
IF COL_LENGTH('dbo.maintenance_tickets', 'updated_at') IS NULL
    ALTER TABLE dbo.maintenance_tickets ADD updated_at datetime2(0) NOT NULL
        CONSTRAINT DF_maintenance_tickets_updated DEFAULT sysutcdatetime();
GO

-- Supporting indexes for live dashboard/report queries.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.reservations')
               AND name = 'IX_reservations_management_dates')
    CREATE INDEX IX_reservations_management_dates
        ON dbo.reservations(check_in_date, check_out_date, status_code)
        INCLUDE (booked_at, total_amount);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.payments')
               AND name = 'IX_payments_management_paid')
    CREATE INDEX IX_payments_management_paid
        ON dbo.payments(status_code, paid_at)
        INCLUDE (amount, payment_type, method_code);
GO
