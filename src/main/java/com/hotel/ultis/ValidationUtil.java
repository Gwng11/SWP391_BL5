package com.hotel.ultis;

import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{8,15}$");

    private ValidationUtil() {}

    public static boolean isEmail(String s) { return s != null && EMAIL.matcher(s).matches(); }
    public static boolean isPhone(String s) { return s != null && PHONE.matcher(s).matches(); }
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** Chặn chuỗi vượt độ dài cột DB - tránh lỗi SQL truncation 500 */
    public static void requireMaxLen(String value, int max, String label) {
        if (value != null && value.length() > max)
            throw new IllegalArgumentException(label + " tối đa " + max + " ký tự");
    }

    /** Mật khẩu >= 8 ký tự, có chữ và số */
    public static boolean isStrongPassword(String s) {
        return s != null && s.length() >= 8 && s.matches(".*[A-Za-z].*") && s.matches(".*[0-9].*");
    }
}
