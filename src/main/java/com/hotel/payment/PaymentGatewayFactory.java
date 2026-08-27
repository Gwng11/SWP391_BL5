package com.hotel.payment;

import java.util.Map;

public final class PaymentGatewayFactory {
    private PaymentGatewayFactory() {}

    public static PaymentGateway create() {
        return create(System.getenv());
    }

    static PaymentGateway create(Map<String, String> environment) {
        String configuredProvider = value(environment, "HMS_PAYMENT_PROVIDER", null);
        String tmnCode = value(environment, "HMS_VNPAY_TMN_CODE", null);
        String hashSecret = value(environment, "HMS_VNPAY_HASH_SECRET", null);
        String provider = configuredProvider;

        // Hai credentials là đủ để tự bật VNPay; không còn mặc định sandbox tự thành công.
        if (provider == null || provider.isBlank()) {
            provider = (hasText(tmnCode) || hasText(hashSecret)) ? "VNPAY" : "UNCONFIGURED";
        }
        provider = provider.trim().toUpperCase();
        if ("VNPAY".equals(provider)) {
            return new VnPayPaymentGateway(
                    tmnCode,
                    hashSecret,
                    value(environment, "HMS_VNPAY_PAY_URL",
                            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"),
                    value(environment, "HMS_VNPAY_RETURN_URL", null));
        }
        if ("SANDBOX".equals(provider)) return new SandboxPaymentGateway();
        if ("UNCONFIGURED".equals(provider) && !hasText(configuredProvider))
            return new UnavailablePaymentGateway();
        throw new IllegalStateException("HMS_PAYMENT_PROVIDER chỉ hỗ trợ SANDBOX hoặc VNPAY");
    }

    static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String value(Map<String, String> environment, String name, String fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
