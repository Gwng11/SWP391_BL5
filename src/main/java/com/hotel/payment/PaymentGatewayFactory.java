package com.hotel.payment;

public final class PaymentGatewayFactory {
    private PaymentGatewayFactory() {}

    public static PaymentGateway create() {
        String provider = env("HMS_PAYMENT_PROVIDER", "SANDBOX").trim().toUpperCase();
        if ("VNPAY".equals(provider)) return VnPayPaymentGateway.fromEnvironment();
        if ("SANDBOX".equals(provider)) return new SandboxPaymentGateway();
        throw new IllegalStateException("HMS_PAYMENT_PROVIDER chỉ hỗ trợ SANDBOX hoặc VNPAY");
    }

    static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
