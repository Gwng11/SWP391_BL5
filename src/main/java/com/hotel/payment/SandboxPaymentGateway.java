package com.hotel.payment;

import com.hotel.entity.Payment;
import java.util.UUID;

/** Gateway demo. Có thể thay bằng adapter thật khi khách sạn chọn nhà cung cấp. */
public class SandboxPaymentGateway implements PaymentGateway {
    @Override
    public String providerName() { return "SANDBOX_GATEWAY"; }

    @Override
    public GatewayResult authorize(Payment payment) {
        return GatewayResult.success("TXN-" + UUID.randomUUID());
    }
}
