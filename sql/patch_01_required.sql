-- =====================================================================
-- PATCH BẮT BUỘC chạy trước khi dùng code (F04, F15)
-- =====================================================================
USE [SingleHotelManagementDB]
GO

-- F04: cờ xác thực email
IF COL_LENGTH('dbo.users', 'email_verified_at') IS NULL
    ALTER TABLE dbo.users ADD email_verified_at datetime2(0) NULL;
GO

-- F04: bảng token xác thực email / reset mật khẩu
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

-- F15: giờ hẹn thực hiện dịch vụ
IF COL_LENGTH('dbo.service_requests', 'scheduled_at') IS NULL
    ALTER TABLE dbo.service_requests ADD scheduled_at datetime2(0) NULL;
GO

-- Khuyến nghị cho web app
ALTER DATABASE SingleHotelManagementDB SET AUTO_CLOSE OFF;
GO
