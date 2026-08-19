package com.hotel.ultis;

import java.math.BigDecimal;

/** Các hằng số dùng chung, khớp với CHECK constraint trong DB */
public final class Constants {
    private Constants() {}

    // Thuế & tỉ lệ đặt cọc
    public static final BigDecimal TAX_RATE = new BigDecimal("0.10");     // 10% VAT
    public static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30"); // cọc 30%

    // users.role_code
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_RECEPTIONIST = "RECEPTIONIST";
    public static final String ROLE_SERVICE_STAFF = "SERVICE_STAFF";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_ADMIN = "ADMIN";

    // SRS system messages used by authentication/RBAC.
    public static final String MSG_INVALID_LOGIN = "Incorrect email or password. Please try again.";
    public static final String MSG_LOGIN_REQUIRED = "Email and password are required.";
    public static final String MSG_ACCOUNT_INACTIVE = "Your account is inactive or blocked.";
    public static final String MSG_ACCOUNT_LOCKED = "Your account has been locked for 30 minutes.";
    public static final String MSG_NO_PERMISSION = "You do not have permission to access this function.";
    public static final String MSG_SYSTEM_ERROR = "An unexpected error has occurred. Please try again later.";

    public static final String ROOM_AVAILABLE = "AVAILABLE";
    public static final String ROOM_OCCUPIED = "OCCUPIED";
    public static final String ROOM_MAINTENANCE = "MAINTENANCE";
    public static final String ROOM_OUT_OF_SERVICE = "OUT_OF_SERVICE";
    public static final String ROOM_BLOCKED = "BLOCKED";
    public static final String ROOM_INACTIVE = "INACTIVE";

    public static final String CLEAN_DIRTY = "DIRTY";
    public static final String CLEAN_CLEANING = "CLEANING";
    public static final String CLEAN_CLEAN = "CLEAN";
    public static final String CLEAN_INSPECTED = "INSPECTED";
    public static final String CLEAN_READY = "READY";

    // reservations.status_code
    public static final String RES_PENDING = "PENDING";
    public static final String RES_CONFIRMED = "CONFIRMED";
    public static final String RES_CHECKED_IN = "CHECKED_IN";
    public static final String RES_CHECKED_OUT = "CHECKED_OUT";
    public static final String RES_CANCELLED = "CANCELLED";
    public static final String RES_NO_SHOW = "NO_SHOW";

    // payments
    public static final String PAY_DEPOSIT = "DEPOSIT";
    public static final String PAY_FINAL = "FINAL_PAYMENT";
    public static final String PAY_PENDING = "PENDING";
    public static final String PAY_SUCCESS = "SUCCESS";
    public static final String PAY_FAILED = "FAILED";

    // invoices.status_code
    public static final String INV_DRAFT = "DRAFT";
    public static final String INV_ISSUED = "ISSUED";
    public static final String INV_PARTIALLY_PAID = "PARTIALLY_PAID";
    public static final String INV_PAID = "PAID";

    // service_requests.status_code
    public static final String SR_PENDING = "PENDING";
    public static final String SR_ASSIGNED = "ASSIGNED";
    public static final String SR_IN_PROGRESS = "IN_PROGRESS";
    public static final String SR_COMPLETED = "COMPLETED";
    public static final String SR_CANCELLED = "CANCELLED";

    // email event_code
    public static final String EV_ACCOUNT_VERIFICATION = "ACCOUNT_VERIFICATION";
    public static final String EV_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String EV_RESERVATION_CONFIRMED = "RESERVATION_CONFIRMED";
    public static final String EV_RESERVATION_UPDATED = "RESERVATION_UPDATED";
    public static final String EV_RESERVATION_CANCELLED = "RESERVATION_CANCELLED";
    public static final String EV_DEPOSIT_RECEIPT = "DEPOSIT_RECEIPT";
    public static final String EV_INVOICE_AND_RECEIPT = "INVOICE_AND_RECEIPT";

    // user_tokens.token_type
    public static final String TK_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    public static final String TK_PASSWORD_RESET = "PASSWORD_RESET";

    // session attribute
    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_CUSTOMER = "currentCustomer";
}
