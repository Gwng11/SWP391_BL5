/*
  Single Hotel Management System
  Database engine : Microsoft SQL Server 2019+
  Backend         : Java
  Frontend        : HTML + CSS
  Scope           : 5 main actors, 2 external actors, 26 features,
                    19 tables, one hotel

  Main actors     : CUSTOMER, RECEPTIONIST, SERVICE_STAFF, MANAGER, ADMIN
  External actors : Payment Gateway (sandbox/mock; no separate table)
                    Email Service (delivery tracked through email_logs)

  Customer combines Guest and registered Customer:
  - Public browsing/searching does not create a database row.
  - Registration creates users + customers records.
  - A walk-in Customer may have customers.user_id = NULL.

  Feature coverage:
  F01 hotel_profile
  F02 room_types, rooms, room_rates, reservations, reservation_rooms
  F03 room_types, rooms, room_rates
  F04 users, customers, email_templates, email_logs
  F05 users, customers
  F06 reservations, reservation_rooms, reservation_guests, email_logs
  F07 reservations, reservation_rooms, reservation_guests, email_logs
  F08 payments, email_logs
  F09 customers, users
  F10 reservations, reservation_guests, payments
  F11 reservation_rooms, room_assignments, rooms
  F12 reservations, room_assignments, invoice_items
  F13 reservations, invoices, rooms, housekeeping_tasks
  F14 invoices, invoice_items, payments, email_logs
  F15 hotel_services, service_requests
  F16 service_requests, invoice_items, users
  F17 housekeeping_tasks, rooms, users
  F18 housekeeping_tasks, rooms, maintenance_tickets
  F19 maintenance_tickets, rooms, users
  F20 rooms, room_types
  F21 room_types
  F22 room_types, room_rates
  F23 dashboard queries only (no additional table)
  F24 report queries only (no additional table)
  F25 users, email_logs
  F26 email_templates, email_logs

  Run this file once in SQL Server Management Studio (SSMS).
  This script is non-destructive: it stops if the schema already exists.
*/

USE [master];
GO

IF DB_ID(N'SingleHotelManagementDB') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE [SingleHotelManagementDB]');
END;
GO

USE [SingleHotelManagementDB];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET XACT_ABORT ON;
GO

IF OBJECT_ID(N'dbo.hotel_profile', N'U') IS NOT NULL
BEGIN
    THROW 50001, N'The hotel schema already exists. Use a migration instead of running this initialization script again.', 1;
END;
GO

/* ==============================================================
   01. HOTEL AND ACCOUNTS
   ============================================================== */

CREATE TABLE dbo.hotel_profile
(
    profile_id       TINYINT        NOT NULL,
    hotel_name       NVARCHAR(150)  NOT NULL,
    [description]    NVARCHAR(MAX)  NULL,
    [address]        NVARCHAR(255)  NOT NULL,
    phone            VARCHAR(30)    NOT NULL,
    email            VARCHAR(255)   NULL,
    check_in_time    TIME(0)        NOT NULL,
    check_out_time   TIME(0)        NOT NULL,
    currency_code    CHAR(3)        NOT NULL,
    logo_url         NVARCHAR(500)  NULL,
    cover_image_url  NVARCHAR(500)  NULL,
    updated_at       DATETIME2(0)   NOT NULL
        CONSTRAINT DF_hotel_profile_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_hotel_profile PRIMARY KEY (profile_id),
    CONSTRAINT CK_hotel_profile_single_row CHECK (profile_id = 1),
    CONSTRAINT CK_hotel_profile_currency CHECK (currency_code LIKE '[A-Z][A-Z][A-Z]')
);
GO

CREATE TABLE dbo.[users]
(
    user_id          BIGINT IDENTITY(1,1) NOT NULL,
    email            VARCHAR(255)   NOT NULL,
    password_hash    VARCHAR(255)   NOT NULL,
    full_name        NVARCHAR(150)  NOT NULL,
    phone            VARCHAR(30)    NULL,
    role_code        VARCHAR(20)    NOT NULL,
    department_code  VARCHAR(20)    NULL,
    status_code      VARCHAR(20)    NOT NULL
        CONSTRAINT DF_users_status DEFAULT ('ACTIVE'),
    last_login_at    DATETIME2(0)   NULL,
    created_at       DATETIME2(0)   NOT NULL
        CONSTRAINT DF_users_created_at DEFAULT SYSUTCDATETIME(),
    updated_at       DATETIME2(0)   NOT NULL
        CONSTRAINT DF_users_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_users PRIMARY KEY (user_id),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT CK_users_role CHECK
    (
        role_code IN
            ('CUSTOMER', 'RECEPTIONIST', 'SERVICE_STAFF', 'MANAGER', 'ADMIN')
    ),
    CONSTRAINT CK_users_status CHECK
        (status_code IN ('ACTIVE', 'LOCKED', 'INACTIVE')),
    CONSTRAINT CK_users_role_department CHECK
    (
        (role_code = 'SERVICE_STAFF' AND department_code IN
            ('GENERAL_SERVICE', 'HOUSEKEEPING', 'MAINTENANCE'))
        OR
        (role_code IN ('CUSTOMER', 'RECEPTIONIST', 'MANAGER', 'ADMIN')
            AND department_code IS NULL)
    )
);
GO

CREATE TABLE dbo.customers
(
    customer_id          BIGINT IDENTITY(1,1) NOT NULL,
    user_id              BIGINT         NULL,
    created_by_user_id   BIGINT         NULL,
    customer_code        VARCHAR(20)    NOT NULL,
    full_name            NVARCHAR(150)  NOT NULL,
    email                VARCHAR(255)   NULL,
    phone                VARCHAR(30)    NULL,
    date_of_birth        DATE           NULL,
    id_document_type     VARCHAR(30)    NULL,
    id_document_number   VARCHAR(50)    NULL,
    nationality          NVARCHAR(80)   NULL,
    [address]            NVARCHAR(255)  NULL,
    status_code          VARCHAR(20)    NOT NULL
        CONSTRAINT DF_customers_status DEFAULT ('ACTIVE'),
    created_at           DATETIME2(0)   NOT NULL
        CONSTRAINT DF_customers_created_at DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2(0)   NOT NULL
        CONSTRAINT DF_customers_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_customers PRIMARY KEY (customer_id),
    CONSTRAINT UQ_customers_code UNIQUE (customer_code),
    CONSTRAINT FK_customers_user FOREIGN KEY (user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_customers_created_by FOREIGN KEY (created_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_customers_status CHECK
        (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT CK_customers_document CHECK
    (
        (id_document_type IS NULL AND id_document_number IS NULL)
        OR
        (id_document_type IS NOT NULL AND id_document_number IS NOT NULL)
    )
);
GO

-- SQL Server UNIQUE constraints allow only one NULL, so filtered indexes
-- are used for optional one-to-one and optional unique values.
CREATE UNIQUE INDEX UX_customers_user_id
    ON dbo.customers (user_id)
    WHERE user_id IS NOT NULL;
GO

CREATE UNIQUE INDEX UX_customers_identity_document
    ON dbo.customers (id_document_type, id_document_number)
    WHERE id_document_number IS NOT NULL;
GO

/* ==============================================================
   02. ROOMS AND PRICING
   ============================================================== */

CREATE TABLE dbo.room_types
(
    room_type_id    BIGINT IDENTITY(1,1) NOT NULL,
    type_code       VARCHAR(20)     NOT NULL,
    type_name       NVARCHAR(100)   NOT NULL,
    [description]   NVARCHAR(MAX)   NULL,
    max_adults      SMALLINT        NOT NULL,
    max_children    SMALLINT        NOT NULL,
    bed_type        NVARCHAR(50)    NULL,
    room_size_m2    DECIMAL(6,2)    NULL,
    base_price      DECIMAL(12,2)   NOT NULL,
    amenities_json  NVARCHAR(MAX)   NULL,
    images_json     NVARCHAR(MAX)   NULL,
    is_active       BIT             NOT NULL
        CONSTRAINT DF_room_types_active DEFAULT (1),
    created_at      DATETIME2(0)    NOT NULL
        CONSTRAINT DF_room_types_created_at DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2(0)    NOT NULL
        CONSTRAINT DF_room_types_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_room_types PRIMARY KEY (room_type_id),
    CONSTRAINT UQ_room_types_code UNIQUE (type_code),
    CONSTRAINT CK_room_types_capacity CHECK
        (max_adults > 0 AND max_children >= 0),
    CONSTRAINT CK_room_types_size CHECK
        (room_size_m2 IS NULL OR room_size_m2 > 0),
    CONSTRAINT CK_room_types_price CHECK (base_price >= 0),
    CONSTRAINT CK_room_types_amenities_json CHECK
        (amenities_json IS NULL OR ISJSON(amenities_json) = 1),
    CONSTRAINT CK_room_types_images_json CHECK
        (images_json IS NULL OR ISJSON(images_json) = 1)
);
GO

CREATE TABLE dbo.rooms
(
    room_id             BIGINT IDENTITY(1,1) NOT NULL,
    room_type_id        BIGINT         NOT NULL,
    room_number         VARCHAR(20)    NOT NULL,
    floor_number        SMALLINT       NULL,
    operational_status  VARCHAR(20)    NOT NULL
        CONSTRAINT DF_rooms_operational_status DEFAULT ('AVAILABLE'),
    cleaning_status     VARCHAR(20)    NOT NULL
        CONSTRAINT DF_rooms_cleaning_status DEFAULT ('CLEAN'),
    notes               NVARCHAR(500)  NULL,
    is_active           BIT            NOT NULL
        CONSTRAINT DF_rooms_active DEFAULT (1),
    created_at          DATETIME2(0)   NOT NULL
        CONSTRAINT DF_rooms_created_at DEFAULT SYSUTCDATETIME(),
    updated_at          DATETIME2(0)   NOT NULL
        CONSTRAINT DF_rooms_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_rooms PRIMARY KEY (room_id),
    CONSTRAINT UQ_rooms_number UNIQUE (room_number),
    CONSTRAINT FK_rooms_room_type FOREIGN KEY (room_type_id)
        REFERENCES dbo.room_types (room_type_id),
    CONSTRAINT CK_rooms_operational_status CHECK
        (operational_status IN
            ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE')),
    CONSTRAINT CK_rooms_cleaning_status CHECK
        (cleaning_status IN ('CLEAN', 'DIRTY', 'CLEANING', 'INSPECTED'))
);
GO

CREATE INDEX IX_rooms_search
    ON dbo.rooms (room_type_id, operational_status, cleaning_status, is_active);
GO

CREATE TABLE dbo.room_rates
(
    room_rate_id   BIGINT IDENTITY(1,1) NOT NULL,
    room_type_id   BIGINT         NOT NULL,
    rate_date      DATE           NOT NULL,
    nightly_price  DECIMAL(12,2)  NOT NULL,
    stop_sell      BIT            NOT NULL
        CONSTRAINT DF_room_rates_stop_sell DEFAULT (0),
    created_at     DATETIME2(0)   NOT NULL
        CONSTRAINT DF_room_rates_created_at DEFAULT SYSUTCDATETIME(),
    updated_at     DATETIME2(0)   NOT NULL
        CONSTRAINT DF_room_rates_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_room_rates PRIMARY KEY (room_rate_id),
    CONSTRAINT UQ_room_rates_type_date UNIQUE (room_type_id, rate_date),
    CONSTRAINT FK_room_rates_room_type FOREIGN KEY (room_type_id)
        REFERENCES dbo.room_types (room_type_id),
    CONSTRAINT CK_room_rates_price CHECK (nightly_price >= 0)
);
GO

/* ==============================================================
   03. RESERVATIONS, CHECK-IN AND STAY
   ============================================================== */

CREATE TABLE dbo.reservations
(
    reservation_id         BIGINT IDENTITY(1,1) NOT NULL,
    customer_id            BIGINT          NOT NULL,
    created_by_user_id     BIGINT          NULL,
    checked_in_by_user_id  BIGINT          NULL,
    checked_out_by_user_id BIGINT          NULL,
    booking_code           VARCHAR(30)     NOT NULL,
    source_code            VARCHAR(20)     NOT NULL
        CONSTRAINT DF_reservations_source DEFAULT ('ONLINE'),
    status_code            VARCHAR(20)     NOT NULL
        CONSTRAINT DF_reservations_status DEFAULT ('PENDING'),
    booked_at              DATETIME2(0)    NOT NULL
        CONSTRAINT DF_reservations_booked_at DEFAULT SYSUTCDATETIME(),
    check_in_date          DATE            NOT NULL,
    check_out_date         DATE            NOT NULL,
    actual_check_in_at     DATETIME2(0)    NULL,
    actual_check_out_at    DATETIME2(0)    NULL,
    adult_count            SMALLINT        NOT NULL,
    child_count            SMALLINT        NOT NULL
        CONSTRAINT DF_reservations_child_count DEFAULT (0),
    room_subtotal          DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_reservations_room_subtotal DEFAULT (0),
    service_total          DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_reservations_service_total DEFAULT (0),
    tax_amount             DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_reservations_tax_amount DEFAULT (0),
    total_amount           DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_reservations_total_amount DEFAULT (0),
    deposit_required       DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_reservations_deposit_required DEFAULT (0),
    special_requests       NVARCHAR(MAX)   NULL,
    cancellation_reason    NVARCHAR(255)   NULL,
    created_at             DATETIME2(0)    NOT NULL
        CONSTRAINT DF_reservations_created_at DEFAULT SYSUTCDATETIME(),
    updated_at             DATETIME2(0)    NOT NULL
        CONSTRAINT DF_reservations_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_reservations PRIMARY KEY (reservation_id),
    CONSTRAINT UQ_reservations_booking_code UNIQUE (booking_code),
    CONSTRAINT FK_reservations_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers (customer_id),
    CONSTRAINT FK_reservations_created_by FOREIGN KEY (created_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_reservations_checked_in_by FOREIGN KEY (checked_in_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_reservations_checked_out_by FOREIGN KEY (checked_out_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_reservations_source CHECK
        (source_code IN ('ONLINE', 'RECEPTIONIST', 'WALK_IN')),
    CONSTRAINT CK_reservations_status CHECK
        (status_code IN
            ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT',
             'CANCELLED', 'NO_SHOW')),
    CONSTRAINT CK_reservations_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT CK_reservations_actual_times CHECK
    (
        actual_check_out_at IS NULL
        OR actual_check_in_at IS NULL
        OR actual_check_out_at >= actual_check_in_at
    ),
    CONSTRAINT CK_reservations_guests CHECK
        (adult_count > 0 AND child_count >= 0),
    CONSTRAINT CK_reservations_amounts CHECK
    (
        room_subtotal >= 0 AND service_total >= 0 AND tax_amount >= 0
        AND total_amount >= 0 AND deposit_required >= 0
    )
);
GO

CREATE INDEX IX_reservations_availability
    ON dbo.reservations (status_code, check_in_date, check_out_date);
GO

CREATE INDEX IX_reservations_customer
    ON dbo.reservations (customer_id, booked_at DESC);
GO

CREATE TABLE dbo.reservation_rooms
(
    reservation_room_id     BIGINT IDENTITY(1,1) NOT NULL,
    reservation_id          BIGINT          NOT NULL,
    room_type_id            BIGINT          NOT NULL,
    quantity                SMALLINT        NOT NULL,
    adult_count             SMALLINT        NOT NULL,
    child_count             SMALLINT        NOT NULL
        CONSTRAINT DF_reservation_rooms_child_count DEFAULT (0),
    nightly_price_snapshot  DECIMAL(12,2)   NOT NULL,
    number_of_nights        SMALLINT        NOT NULL,
    line_total              DECIMAL(12,2)   NOT NULL,
    notes                   NVARCHAR(500)   NULL,

    CONSTRAINT PK_reservation_rooms PRIMARY KEY (reservation_room_id),
    CONSTRAINT FK_reservation_rooms_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_reservation_rooms_room_type FOREIGN KEY (room_type_id)
        REFERENCES dbo.room_types (room_type_id),
    CONSTRAINT CK_reservation_rooms_quantity CHECK (quantity > 0),
    CONSTRAINT CK_reservation_rooms_guests CHECK
        (adult_count > 0 AND child_count >= 0),
    CONSTRAINT CK_reservation_rooms_nights CHECK (number_of_nights > 0),
    CONSTRAINT CK_reservation_rooms_amounts CHECK
        (nightly_price_snapshot >= 0 AND line_total >= 0)
);
GO

CREATE INDEX IX_reservation_rooms_reservation
    ON dbo.reservation_rooms (reservation_id, room_type_id);
GO

CREATE TABLE dbo.reservation_guests
(
    reservation_guest_id  BIGINT IDENTITY(1,1) NOT NULL,
    reservation_id        BIGINT         NOT NULL,
    customer_id           BIGINT         NULL,
    full_name             NVARCHAR(150)  NOT NULL,
    date_of_birth         DATE           NULL,
    id_document_type      VARCHAR(30)    NULL,
    id_document_number    VARCHAR(50)    NULL,
    nationality           NVARCHAR(80)   NULL,
    is_primary_guest      BIT            NOT NULL
        CONSTRAINT DF_reservation_guests_primary DEFAULT (0),

    CONSTRAINT PK_reservation_guests PRIMARY KEY (reservation_guest_id),
    CONSTRAINT FK_reservation_guests_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_reservation_guests_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers (customer_id),
    CONSTRAINT CK_reservation_guests_document CHECK
    (
        (id_document_type IS NULL AND id_document_number IS NULL)
        OR
        (id_document_type IS NOT NULL AND id_document_number IS NOT NULL)
    )
);
GO

CREATE INDEX IX_reservation_guests_reservation
    ON dbo.reservation_guests (reservation_id);
GO

CREATE UNIQUE INDEX UX_reservation_guests_primary
    ON dbo.reservation_guests (reservation_id)
    WHERE is_primary_guest = 1;
GO

CREATE TABLE dbo.room_assignments
(
    room_assignment_id    BIGINT IDENTITY(1,1) NOT NULL,
    reservation_room_id   BIGINT         NOT NULL,
    room_id               BIGINT         NOT NULL,
    assigned_by_user_id   BIGINT         NULL,
    assigned_at           DATETIME2(0)   NOT NULL
        CONSTRAINT DF_room_assignments_assigned_at DEFAULT SYSUTCDATETIME(),
    unassigned_at         DATETIME2(0)   NULL,
    unassigned_reason     NVARCHAR(255)  NULL,
    is_current            BIT            NOT NULL
        CONSTRAINT DF_room_assignments_current DEFAULT (1),

    CONSTRAINT PK_room_assignments PRIMARY KEY (room_assignment_id),
    CONSTRAINT FK_room_assignments_reservation_room FOREIGN KEY (reservation_room_id)
        REFERENCES dbo.reservation_rooms (reservation_room_id),
    CONSTRAINT FK_room_assignments_room FOREIGN KEY (room_id)
        REFERENCES dbo.rooms (room_id),
    CONSTRAINT FK_room_assignments_assigned_by FOREIGN KEY (assigned_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_room_assignments_times CHECK
        (unassigned_at IS NULL OR unassigned_at >= assigned_at),
    CONSTRAINT CK_room_assignments_current CHECK
    (
        (is_current = 1 AND unassigned_at IS NULL)
        OR
        (is_current = 0 AND unassigned_at IS NOT NULL)
    )
);
GO

-- A physical room can belong to only one active assignment at a time.
CREATE UNIQUE INDEX UX_room_assignments_current_room
    ON dbo.room_assignments (room_id)
    WHERE is_current = 1;
GO

CREATE INDEX IX_room_assignments_reservation_room
    ON dbo.room_assignments (reservation_room_id, is_current);
GO

/* ==============================================================
   04. BILLING, SERVICES AND PAYMENT
   ============================================================== */

CREATE TABLE dbo.invoices
(
    invoice_id         BIGINT IDENTITY(1,1) NOT NULL,
    reservation_id     BIGINT          NOT NULL,
    customer_id        BIGINT          NOT NULL,
    issued_by_user_id  BIGINT          NULL,
    invoice_number     VARCHAR(30)     NOT NULL,
    issued_at          DATETIME2(0)    NULL,
    currency_code      CHAR(3)         NOT NULL
        CONSTRAINT DF_invoices_currency DEFAULT ('VND'),
    subtotal           DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_invoices_subtotal DEFAULT (0),
    tax_amount         DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_invoices_tax DEFAULT (0),
    total_amount       DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_invoices_total DEFAULT (0),
    paid_amount        DECIMAL(12,2)   NOT NULL
        CONSTRAINT DF_invoices_paid DEFAULT (0),
    status_code        VARCHAR(20)     NOT NULL
        CONSTRAINT DF_invoices_status DEFAULT ('DRAFT'),
    created_at         DATETIME2(0)    NOT NULL
        CONSTRAINT DF_invoices_created_at DEFAULT SYSUTCDATETIME(),
    updated_at         DATETIME2(0)    NOT NULL
        CONSTRAINT DF_invoices_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_invoices PRIMARY KEY (invoice_id),
    CONSTRAINT UQ_invoices_reservation UNIQUE (reservation_id),
    CONSTRAINT UQ_invoices_number UNIQUE (invoice_number),
    CONSTRAINT FK_invoices_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_invoices_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers (customer_id),
    CONSTRAINT FK_invoices_issued_by FOREIGN KEY (issued_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_invoices_currency CHECK
        (currency_code LIKE '[A-Z][A-Z][A-Z]'),
    CONSTRAINT CK_invoices_amounts CHECK
    (
        subtotal >= 0 AND tax_amount >= 0 AND total_amount >= 0
        AND paid_amount >= 0 AND paid_amount <= total_amount
    ),
    CONSTRAINT CK_invoices_status CHECK
        (status_code IN ('DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED'))
);
GO

CREATE TABLE dbo.hotel_services
(
    hotel_service_id  BIGINT IDENTITY(1,1) NOT NULL,
    service_code      VARCHAR(20)     NOT NULL,
    service_name      NVARCHAR(100)   NOT NULL,
    [description]     NVARCHAR(MAX)   NULL,
    unit_name         NVARCHAR(30)    NOT NULL,
    unit_price        DECIMAL(12,2)   NOT NULL,
    image_url         VARCHAR(255)    NULL,
    is_active         BIT             NOT NULL
        CONSTRAINT DF_hotel_services_active DEFAULT (1),
    created_at        DATETIME2(0)    NOT NULL
        CONSTRAINT DF_hotel_services_created_at DEFAULT SYSUTCDATETIME(),
    updated_at        DATETIME2(0)    NOT NULL
        CONSTRAINT DF_hotel_services_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_hotel_services PRIMARY KEY (hotel_service_id),
    CONSTRAINT UQ_hotel_services_code UNIQUE (service_code),
    CONSTRAINT CK_hotel_services_price CHECK (unit_price >= 0)
);
GO

CREATE TABLE dbo.service_requests
(
    service_request_id      BIGINT IDENTITY(1,1) NOT NULL,
    reservation_id         BIGINT          NOT NULL,
    customer_id            BIGINT          NOT NULL,
    hotel_service_id       BIGINT          NOT NULL,
    assigned_staff_user_id BIGINT          NULL,
    quantity               DECIMAL(10,2)   NOT NULL,
    unit_price_snapshot    DECIMAL(12,2)   NOT NULL,
    total_amount           DECIMAL(12,2)   NOT NULL,
    status_code            VARCHAR(20)     NOT NULL
        CONSTRAINT DF_service_requests_status DEFAULT ('PENDING'),
    requested_at           DATETIME2(0)    NOT NULL
        CONSTRAINT DF_service_requests_requested_at DEFAULT SYSUTCDATETIME(),
    requested_for_at       DATETIME2(0)    NOT NULL,
    assigned_at            DATETIME2(0)    NULL,
    started_at             DATETIME2(0)    NULL,
    completed_at           DATETIME2(0)    NULL,
    notes                  NVARCHAR(500)   NULL,

    CONSTRAINT PK_service_requests PRIMARY KEY (service_request_id),
    CONSTRAINT FK_service_requests_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_service_requests_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers (customer_id),
    CONSTRAINT FK_service_requests_service FOREIGN KEY (hotel_service_id)
        REFERENCES dbo.hotel_services (hotel_service_id),
    CONSTRAINT FK_service_requests_staff FOREIGN KEY (assigned_staff_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_service_requests_quantity CHECK (quantity > 0),
    CONSTRAINT CK_service_requests_amounts CHECK
        (unit_price_snapshot >= 0 AND total_amount >= 0),
    CONSTRAINT CK_service_requests_status CHECK
        (status_code IN
            ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_service_requests_times CHECK
    (
        (assigned_at IS NULL OR assigned_at >= requested_at)
        AND (started_at IS NULL OR started_at >= requested_at)
        AND (completed_at IS NULL OR completed_at >= requested_at)
        AND (started_at IS NULL OR completed_at IS NULL OR completed_at >= started_at)
    ),
    CONSTRAINT CK_service_requests_requested_for CHECK
        (requested_for_at >= requested_at)
);
GO

CREATE INDEX IX_service_requests_work_queue
    ON dbo.service_requests (status_code, assigned_staff_user_id, requested_at);
GO

CREATE TABLE dbo.invoice_items
(
    invoice_item_id     BIGINT IDENTITY(1,1) NOT NULL,
    invoice_id          BIGINT          NOT NULL,
    service_request_id  BIGINT          NULL,
    posted_by_user_id   BIGINT          NULL,
    item_type           VARCHAR(20)     NOT NULL,
    [description]       NVARCHAR(255)   NOT NULL,
    quantity            DECIMAL(10,2)   NOT NULL,
    unit_price          DECIMAL(12,2)   NOT NULL,
    amount              DECIMAL(12,2)   NOT NULL,
    posted_at           DATETIME2(0)    NOT NULL
        CONSTRAINT DF_invoice_items_posted_at DEFAULT SYSUTCDATETIME(),
    is_voided           BIT             NOT NULL
        CONSTRAINT DF_invoice_items_voided DEFAULT (0),

    CONSTRAINT PK_invoice_items PRIMARY KEY (invoice_item_id),
    CONSTRAINT FK_invoice_items_invoice FOREIGN KEY (invoice_id)
        REFERENCES dbo.invoices (invoice_id),
    CONSTRAINT FK_invoice_items_service_request FOREIGN KEY (service_request_id)
        REFERENCES dbo.service_requests (service_request_id),
    CONSTRAINT FK_invoice_items_posted_by FOREIGN KEY (posted_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_invoice_items_type CHECK
        (item_type IN ('ROOM', 'SERVICE', 'EXTRA')),
    CONSTRAINT CK_invoice_items_amounts CHECK
        (quantity > 0 AND unit_price >= 0 AND amount >= 0)
);
GO

CREATE INDEX IX_invoice_items_invoice
    ON dbo.invoice_items (invoice_id, is_voided);
GO

CREATE UNIQUE INDEX UX_invoice_items_service_request
    ON dbo.invoice_items (service_request_id)
    WHERE service_request_id IS NOT NULL;
GO

CREATE TABLE dbo.payments
(
    payment_id          BIGINT IDENTITY(1,1) NOT NULL,
    reservation_id      BIGINT          NOT NULL,
    invoice_id          BIGINT          NULL,
    recorded_by_user_id BIGINT          NULL,
    payment_type        VARCHAR(20)     NOT NULL,
    method_code         VARCHAR(20)     NOT NULL,
    amount              DECIMAL(12,2)   NOT NULL,
    currency_code       CHAR(3)         NOT NULL
        CONSTRAINT DF_payments_currency DEFAULT ('VND'),
    status_code         VARCHAR(20)     NOT NULL
        CONSTRAINT DF_payments_status DEFAULT ('PENDING'),
    provider_name       VARCHAR(50)     NULL,
    provider_reference  VARCHAR(100)    NULL,
    failure_reason      NVARCHAR(255)   NULL,
    paid_at             DATETIME2(0)    NULL,
    created_at          DATETIME2(0)    NOT NULL
        CONSTRAINT DF_payments_created_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_payments PRIMARY KEY (payment_id),
    CONSTRAINT FK_payments_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_payments_invoice FOREIGN KEY (invoice_id)
        REFERENCES dbo.invoices (invoice_id),
    CONSTRAINT FK_payments_recorded_by FOREIGN KEY (recorded_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_payments_type CHECK
        (payment_type IN ('DEPOSIT', 'FINAL_PAYMENT')),
    CONSTRAINT CK_payments_method CHECK
        (method_code IN ('CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE')),
    CONSTRAINT CK_payments_status CHECK
        (status_code IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    CONSTRAINT CK_payments_amount CHECK (amount > 0),
    CONSTRAINT CK_payments_currency CHECK
        (currency_code LIKE '[A-Z][A-Z][A-Z]')
);
GO

CREATE INDEX IX_payments_reservation
    ON dbo.payments (reservation_id, status_code, created_at DESC);
GO

CREATE INDEX IX_payments_invoice
    ON dbo.payments (invoice_id, status_code)
    WHERE invoice_id IS NOT NULL;
GO

CREATE UNIQUE INDEX UX_payments_provider_reference
    ON dbo.payments (provider_name, provider_reference)
    WHERE provider_reference IS NOT NULL;
GO

/* ==============================================================
   05. HOUSEKEEPING AND MAINTENANCE
   ============================================================== */

CREATE TABLE dbo.housekeeping_tasks
(
    housekeeping_task_id   BIGINT IDENTITY(1,1) NOT NULL,
    room_id                BIGINT         NOT NULL,
    reservation_id         BIGINT         NULL,
    assigned_staff_user_id BIGINT         NULL,
    created_by_user_id     BIGINT         NULL,
    task_type              VARCHAR(30)    NOT NULL,
    priority_code          VARCHAR(20)    NOT NULL
        CONSTRAINT DF_housekeeping_priority DEFAULT ('NORMAL'),
    status_code            VARCHAR(20)    NOT NULL
        CONSTRAINT DF_housekeeping_status DEFAULT ('PENDING'),
    scheduled_at           DATETIME2(0)   NULL,
    started_at             DATETIME2(0)   NULL,
    completed_at           DATETIME2(0)   NULL,
    notes                  NVARCHAR(500)  NULL,
    created_at             DATETIME2(0)   NOT NULL
        CONSTRAINT DF_housekeeping_created_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_housekeeping_tasks PRIMARY KEY (housekeeping_task_id),
    CONSTRAINT FK_housekeeping_room FOREIGN KEY (room_id)
        REFERENCES dbo.rooms (room_id),
    CONSTRAINT FK_housekeeping_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_housekeeping_assigned_staff FOREIGN KEY (assigned_staff_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_housekeeping_created_by FOREIGN KEY (created_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_housekeeping_type CHECK
        (task_type IN
            ('CHECKOUT_CLEANING', 'STAYOVER_CLEANING',
             'DEEP_CLEANING', 'INSPECTION')),
    CONSTRAINT CK_housekeeping_priority CHECK
        (priority_code IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT CK_housekeeping_status CHECK
        (status_code IN
            ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_housekeeping_times CHECK
    (
        (started_at IS NULL OR completed_at IS NULL OR completed_at >= started_at)
        AND (scheduled_at IS NULL OR started_at IS NULL OR started_at >= scheduled_at)
    )
);
GO

CREATE INDEX IX_housekeeping_work_queue
    ON dbo.housekeeping_tasks
        (status_code, assigned_staff_user_id, priority_code, scheduled_at);
GO

CREATE INDEX IX_housekeeping_room
    ON dbo.housekeeping_tasks (room_id, status_code);
GO

CREATE TABLE dbo.maintenance_tickets
(
    maintenance_ticket_id  BIGINT IDENTITY(1,1) NOT NULL,
    room_id                BIGINT         NOT NULL,
    reported_by_user_id    BIGINT         NOT NULL,
    assigned_staff_user_id BIGINT         NULL,
    ticket_code            VARCHAR(30)    NOT NULL,
    title                  NVARCHAR(150)  NOT NULL,
    [description]          NVARCHAR(MAX)  NOT NULL,
    priority_code          VARCHAR(20)    NOT NULL
        CONSTRAINT DF_maintenance_priority DEFAULT ('NORMAL'),
    status_code            VARCHAR(20)    NOT NULL
        CONSTRAINT DF_maintenance_status DEFAULT ('OPEN'),
    reported_at            DATETIME2(0)   NOT NULL
        CONSTRAINT DF_maintenance_reported_at DEFAULT SYSUTCDATETIME(),
    started_at             DATETIME2(0)   NULL,
    resolved_at            DATETIME2(0)   NULL,
    resolution_note        NVARCHAR(500)  NULL,

    CONSTRAINT PK_maintenance_tickets PRIMARY KEY (maintenance_ticket_id),
    CONSTRAINT UQ_maintenance_ticket_code UNIQUE (ticket_code),
    CONSTRAINT FK_maintenance_room FOREIGN KEY (room_id)
        REFERENCES dbo.rooms (room_id),
    CONSTRAINT FK_maintenance_reported_by FOREIGN KEY (reported_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_maintenance_assigned_staff FOREIGN KEY (assigned_staff_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_maintenance_priority CHECK
        (priority_code IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT CK_maintenance_status CHECK
        (status_code IN
            ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    CONSTRAINT CK_maintenance_times CHECK
    (
        (started_at IS NULL OR started_at >= reported_at)
        AND (resolved_at IS NULL OR resolved_at >= reported_at)
        AND (started_at IS NULL OR resolved_at IS NULL OR resolved_at >= started_at)
    )
);
GO

CREATE INDEX IX_maintenance_work_queue
    ON dbo.maintenance_tickets
        (status_code, assigned_staff_user_id, priority_code, reported_at);
GO

CREATE INDEX IX_maintenance_room
    ON dbo.maintenance_tickets (room_id, status_code);
GO

/* ==============================================================
   06. EMAIL NOTIFICATIONS
   ============================================================== */

CREATE TABLE dbo.email_templates
(
    email_template_id  BIGINT IDENTITY(1,1) NOT NULL,
    template_code      VARCHAR(50)     NOT NULL,
    template_name      NVARCHAR(100)   NOT NULL,
    event_code         VARCHAR(50)     NOT NULL,
    subject_template   NVARCHAR(255)   NOT NULL,
    body_html          NVARCHAR(MAX)   NOT NULL,
    body_text          NVARCHAR(MAX)   NULL,
    is_active          BIT             NOT NULL
        CONSTRAINT DF_email_templates_active DEFAULT (1),
    created_by_user_id BIGINT          NULL,
    updated_by_user_id BIGINT          NULL,
    created_at         DATETIME2(0)    NOT NULL
        CONSTRAINT DF_email_templates_created_at DEFAULT SYSUTCDATETIME(),
    updated_at         DATETIME2(0)    NOT NULL
        CONSTRAINT DF_email_templates_updated_at DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_email_templates PRIMARY KEY (email_template_id),
    CONSTRAINT UQ_email_templates_code UNIQUE (template_code),
    CONSTRAINT UQ_email_templates_event UNIQUE (event_code),
    CONSTRAINT FK_email_templates_created_by FOREIGN KEY (created_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_email_templates_updated_by FOREIGN KEY (updated_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_email_templates_event CHECK
    (
        event_code IN
        (
            'ACCOUNT_VERIFICATION',
            'PASSWORD_RESET',
            'RESERVATION_CONFIRMED',
            'RESERVATION_UPDATED',
            'RESERVATION_CANCELLED',
            'DEPOSIT_RECEIPT',
            'INVOICE_AND_RECEIPT',
            'ACCOUNT_CHANGED'
        )
    )
);
GO

CREATE INDEX IX_email_templates_active_event
    ON dbo.email_templates (is_active, event_code);
GO

CREATE TABLE dbo.email_logs
(
    email_log_id          BIGINT IDENTITY(1,1) NOT NULL,
    email_template_id     BIGINT          NOT NULL,
    recipient_user_id     BIGINT          NULL,
    reservation_id        BIGINT          NULL,
    payment_id            BIGINT          NULL,
    invoice_id            BIGINT          NULL,
    triggered_by_user_id  BIGINT          NULL,
    recipient_email       VARCHAR(255)    NOT NULL,
    subject_snapshot      NVARCHAR(255)   NOT NULL,
    body_snapshot         NVARCHAR(MAX)   NOT NULL,
    payload_json          NVARCHAR(MAX)   NULL,
    status_code           VARCHAR(20)     NOT NULL
        CONSTRAINT DF_email_logs_status DEFAULT ('QUEUED'),
    provider_name         VARCHAR(50)     NULL,
    provider_message_id   VARCHAR(100)    NULL,
    retry_count           SMALLINT        NOT NULL
        CONSTRAINT DF_email_logs_retry_count DEFAULT (0),
    queued_at             DATETIME2(0)    NOT NULL
        CONSTRAINT DF_email_logs_queued_at DEFAULT SYSUTCDATETIME(),
    sent_at               DATETIME2(0)    NULL,
    failed_at             DATETIME2(0)    NULL,
    last_error            NVARCHAR(500)   NULL,

    CONSTRAINT PK_email_logs PRIMARY KEY (email_log_id),
    CONSTRAINT FK_email_logs_template FOREIGN KEY (email_template_id)
        REFERENCES dbo.email_templates (email_template_id),
    CONSTRAINT FK_email_logs_recipient_user FOREIGN KEY (recipient_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT FK_email_logs_reservation FOREIGN KEY (reservation_id)
        REFERENCES dbo.reservations (reservation_id),
    CONSTRAINT FK_email_logs_payment FOREIGN KEY (payment_id)
        REFERENCES dbo.payments (payment_id),
    CONSTRAINT FK_email_logs_invoice FOREIGN KEY (invoice_id)
        REFERENCES dbo.invoices (invoice_id),
    CONSTRAINT FK_email_logs_triggered_by FOREIGN KEY (triggered_by_user_id)
        REFERENCES dbo.[users] (user_id),
    CONSTRAINT CK_email_logs_payload_json CHECK
        (payload_json IS NULL OR ISJSON(payload_json) = 1),
    CONSTRAINT CK_email_logs_status CHECK
        (status_code IN ('QUEUED', 'SENDING', 'SENT', 'FAILED', 'CANCELLED')),
    CONSTRAINT CK_email_logs_retry_count CHECK (retry_count >= 0),
    CONSTRAINT CK_email_logs_delivery_time CHECK
    (
        (status_code = 'SENT' AND sent_at IS NOT NULL AND failed_at IS NULL)
        OR
        (status_code = 'FAILED' AND sent_at IS NULL AND failed_at IS NOT NULL)
        OR
        (status_code IN ('QUEUED', 'SENDING', 'CANCELLED')
            AND sent_at IS NULL AND failed_at IS NULL)
    )
);
GO

CREATE UNIQUE INDEX UX_email_logs_provider_message
    ON dbo.email_logs (provider_name, provider_message_id)
    WHERE provider_message_id IS NOT NULL;
GO

CREATE INDEX IX_email_logs_delivery_queue
    ON dbo.email_logs (status_code, queued_at, retry_count);
GO

CREATE INDEX IX_email_logs_recipient
    ON dbo.email_logs (recipient_email, queued_at DESC);
GO

CREATE INDEX IX_email_logs_reservation
    ON dbo.email_logs (reservation_id, queued_at DESC)
    WHERE reservation_id IS NOT NULL;
GO

/* ==============================================================
   INITIAL DATA FOR THE SINGLE HOTEL
   ============================================================== */

INSERT INTO dbo.hotel_profile
(
    profile_id,
    hotel_name,
    [description],
    [address],
    phone,
    email,
    check_in_time,
    check_out_time,
    currency_code
)
VALUES
(
    1,
    N'Your Hotel Name',
    N'Update this description in the Manager screen.',
    N'Update hotel address',
    '0000000000',
    'hotel@example.com',
    '14:00:00',
    '12:00:00',
    'VND'
);
GO

/*
  No default ADMIN account is inserted because a real BCrypt password hash
  must be generated by Spring Security. Create the first System Administrator
  through a controlled bootstrap process in the Java application. The ADMIN
  can then create Receptionist, Service Staff and Manager accounts.

  Dashboard feature (F23) and Reports feature (F24) use SELECT queries over
  existing tables; they do not require separate dashboard or report tables.

  Email Service credentials must be configured through Spring Boot environment
  variables or application configuration. Do not store SMTP/API secrets in
  email_templates or email_logs.
*/

PRINT N'SingleHotelManagementDB created successfully: 19 tables.';
GO
