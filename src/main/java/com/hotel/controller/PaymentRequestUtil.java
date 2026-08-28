package com.hotel.controller;

import jakarta.servlet.http.HttpServletRequest;

/** HTTP data needed when starting a redirect-based payment. */
final class PaymentRequestUtil {
    private PaymentRequestUtil() {}

    static String gatewayReturnUrl(HttpServletRequest request, String providerName) {
        String forwardedProto = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
        String scheme = isHttpScheme(forwardedProto) ? forwardedProto.toLowerCase() : request.getScheme();
        String forwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
        String host = isValidPublicHost(forwardedHost) ? forwardedHost : request.getServerName();
        Integer forwardedPort = validPort(firstHeaderValue(request.getHeader("X-Forwarded-Port")));
        boolean behindProxy = isHttpScheme(forwardedProto) || isValidPublicHost(forwardedHost);
        int port = forwardedPort != null ? forwardedPort
                : behindProxy ? defaultPort(scheme) : request.getServerPort();

        StringBuilder url = new StringBuilder(scheme).append("://").append(host);
        if (!hostContainsPort(host) && port != defaultPort(scheme)) {
            url.append(':').append(port);
        }
        String callbackPath = "MOMO".equalsIgnoreCase(providerName)
                ? "/payment/momo-return" : "/payment/vnpay-return";
        return url.append(request.getContextPath()).append(callbackPath).toString();
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",", 2)[0].trim();
        return request.getRemoteAddr();
    }

    private static String firstHeaderValue(String value) {
        if (value == null || value.isBlank()) return null;
        String first = value.split(",", 2)[0].trim();
        return first.isEmpty() ? null : first;
    }

    private static boolean isHttpScheme(String value) {
        return "http".equalsIgnoreCase(value) || "https".equalsIgnoreCase(value);
    }

    private static boolean isValidPublicHost(String value) {
        return value != null && value.matches("[A-Za-z0-9.-]+(?::[0-9]{1,5})?");
    }

    private static Integer validPort(String value) {
        if (value == null || !value.matches("[0-9]{1,5}")) return null;
        int port = Integer.parseInt(value);
        return port >= 1 && port <= 65535 ? port : null;
    }

    private static int defaultPort(String scheme) {
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    private static boolean hostContainsPort(String host) {
        return host != null && host.matches(".*:[0-9]{1,5}$");
    }
}
