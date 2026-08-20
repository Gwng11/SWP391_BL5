package com.hotel.ultis;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Sinh mã nghiệp vụ: booking_code, customer_code, invoice_number, ticket_code */
public final class CodeGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyMMdd");

    private CodeGenerator() {}

    private static String random(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }

    public static String bookingCode()  { return "BK" + LocalDateTime.now().format(FMT) + random(6); }   // varchar(30)
    public static String customerCode() { return "CUS" + LocalDateTime.now().format(FMT) + random(5); }  // varchar(20)
    public static String invoiceNumber(){ return "INV" + LocalDateTime.now().format(FMT) + random(6); }  // varchar(30)
    public static String maintenanceTicketCode(){ return "MT" + LocalDateTime.now().format(FMT) + random(6); }
}
