package com.hotel.controller;

import jakarta.servlet.http.HttpServletRequest;

/** HTTP data needed when starting a redirect-based payment. */
final class PaymentRequestUtil {
    private PaymentRequestUtil() {}

    static String vnPayReturnUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getScheme()).append("://")
                .append(request.getServerName());
        int port = request.getServerPort();
        if (("http".equalsIgnoreCase(request.getScheme()) && port != 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && port != 443)) {
            url.append(':').append(port);
        }
        return url.append(request.getContextPath()).append("/payment/vnpay-return").toString();
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",", 2)[0].trim();
        return request.getRemoteAddr();
    }
}
