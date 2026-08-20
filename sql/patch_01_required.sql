USE [SingleHotelManagementDB]
GO

-- 1. Bổ sung các cột cho bảng users (chống dò pass & xác thực email)
IF COL_LENGTH('dbo.users', 'failed_login_attempts') IS NULL
    ALTER TABLE dbo.users ADD failed_login_attempts int NOT NULL CONSTRAINT DF_users_failed_attempts DEFAULT 0;
GO

IF COL_LENGTH('dbo.users', 'locked_until') IS NULL
    ALTER TABLE dbo.users ADD locked_until datetime2(0) NULL;
GO

IF COL_LENGTH('dbo.users', 'email_verified_at') IS NULL
    ALTER TABLE dbo.users ADD email_verified_at datetime2(0) NULL;
GO

-- 2. Tạo bảng user_tokens (quản lý token verify email & reset password)
IF OBJECT_ID('dbo.user_tokens') IS NULL
BEGIN
    CREATE TABLE dbo.user_tokens (
        user_token_id bigint IDENTITY(1,1) NOT NULL,
        user_id       bigint NOT NULL,
        token_hash    varchar(255) NOT NULL,
        token_type    varchar(30) NOT NULL,
        expires_at    datetime2(0) NOT NULL,
        used_at       datetime2(0) NULL,
        created_at    datetime2(0) NOT NULL CONSTRAINT DF_user_tokens_created DEFAULT sysutcdatetime(),
        CONSTRAINT PK_user_tokens PRIMARY KEY (user_token_id),
        CONSTRAINT FK_user_tokens_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
        CONSTRAINT CK_user_tokens_type CHECK (token_type IN ('EMAIL_VERIFICATION','PASSWORD_RESET'))
    );
    CREATE INDEX IX_user_tokens_lookup ON dbo.user_tokens (token_hash, token_type)
        INCLUDE (user_id, expires_at, used_at);
END
GO

-- 3. Bổ sung cột scheduled_at cho bảng service_requests (giờ hẹn làm dịch vụ)
IF COL_LENGTH('dbo.service_requests', 'scheduled_at') IS NULL
    ALTER TABLE dbo.service_requests ADD scheduled_at datetime2(0) NULL;
GO

-- Khuyến nghị cho web app: tránh SQL Server đóng/mở DB liên tục gây chậm
ALTER DATABASE SingleHotelManagementDB SET AUTO_CLOSE OFF;
GO

SELECT * from  dbo.user_tokens ;
