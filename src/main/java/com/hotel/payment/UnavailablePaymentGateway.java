package com.hotel.payment;

import com.hotel.entity.Payment;

/** Trạng thái an toàn khi chưa cấu hình cổng thanh toán online. */
public final class UnavailablePaymentGateway implements PaymentGateway {
    @Override
    public String providerName() { return "UNCONFIGURED"; }

    @Override
    public String displayName() { return "Chưa cấu hình"; }

    @Override
    public boolean isAvailable() { return false; }

    @Override
    public GatewayResult authorize(Payment payment) {
        return GatewayResult.failed("Thanh toán online chưa được cấu hình");
    }
}
