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
        String momoPartnerCode = value(environment, "HMS_MOMO_PARTNER_CODE", null);
        String momoAccessKey = value(environment, "HMS_MOMO_ACCESS_KEY", null);
        String momoSecretKey = value(environment, "HMS_MOMO_SECRET_KEY", null);
        String provider = configuredProvider;

        // Hai credentials là đủ để tự bật VNPay; không còn mặc định sandbox tự thành công.
        if (provider == null || provider.isBlank()) {
            boolean hasVnPay = hasText(tmnCode) || hasText(hashSecret);
            boolean hasMomo = hasText(momoPartnerCode) || hasText(momoAccessKey) || hasText(momoSecretKey);
            if (hasVnPay && hasMomo) {
                throw new IllegalStateException("Có cả VNPay và MoMo credentials; hãy đặt HMS_PAYMENT_PROVIDER");
            }
            provider = hasMomo ? "MOMO" : hasVnPay ? "VNPAY" : "UNCONFIGURED";
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
        if ("MOMO".equals(provider)) {
            return new MomoPaymentGateway(
                    momoPartnerCode,
                    momoAccessKey,
                    momoSecretKey,
                    value(environment, "HMS_MOMO_CREATE_URL", MomoPaymentGateway.DEFAULT_CREATE_URL),
                    value(environment, "HMS_MOMO_REDIRECT_URL", null),
                    value(environment, "HMS_MOMO_IPN_URL", null),
                    value(environment, "HMS_MOMO_PARTNER_NAME", "Hotel Management System"),
                    value(environment, "HMS_MOMO_STORE_ID", "HMS"));
        }
        if ("SANDBOX".equals(provider)) return new SandboxPaymentGateway();
        if ("UNCONFIGURED".equals(provider) && !hasText(configuredProvider))
            return new UnavailablePaymentGateway();
        throw new IllegalStateException("HMS_PAYMENT_PROVIDER chỉ hỗ trợ SANDBOX, VNPAY hoặc MOMO");
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
