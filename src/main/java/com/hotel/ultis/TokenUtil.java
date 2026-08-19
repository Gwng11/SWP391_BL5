package com.hotel.ultis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Sinh token ngẫu nhiên cho verify email / reset password (F04). DB chỉ lưu SHA-256 của token. */
public final class TokenUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenUtil() {}

    /** Token thô gửi cho user qua email */
    public static String newRawToken() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** Hash SHA-256 để lưu vào user_tokens.token_hash */
    public static String sha256(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
