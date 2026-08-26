package com.hotel.payment;

import com.hotel.entity.Payment;
import java.util.Map;

/** Abstraction để thay SANDBOX bằng VNPay/MoMo mà không đổi nghiệp vụ thanh toán. */
public interface PaymentGateway {
    String providerName();
    GatewayResult authorize(Payment payment);

    default boolean requiresRedirect() { return false; }

    default String buildPaymentUrl(Payment payment, String returnUrl, String clientIp) {
        throw new UnsupportedOperationException("Gateway không dùng redirect");
    }

    default GatewayResult verifyCallback(Map<String, String> parameters) {
        throw new UnsupportedOperationException("Gateway không hỗ trợ callback");
    }
}
