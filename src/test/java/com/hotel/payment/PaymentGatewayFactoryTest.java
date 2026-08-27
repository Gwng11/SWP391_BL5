package com.hotel.payment;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentGatewayFactoryTest {
    @Test
    void missingConfigurationDoesNotFallBackToSuccessfulSandbox() {
        PaymentGateway gateway = PaymentGatewayFactory.create(Map.of());

        assertInstanceOf(UnavailablePaymentGateway.class, gateway);
        assertFalse(gateway.isAvailable());
    }

    @Test
    void twoVnPayCredentialsAutomaticallySelectVnPay() {
        PaymentGateway gateway = PaymentGatewayFactory.create(Map.of(
                "HMS_VNPAY_TMN_CODE", "DEMO1",
                "HMS_VNPAY_HASH_SECRET", "secret"));

        assertInstanceOf(VnPayPaymentGateway.class, gateway);
        assertEquals("VNPAY", gateway.providerName());
        assertTrue(gateway.requiresRedirect());
    }

    @Test
    void partialVnPayConfigurationFailsFast() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> PaymentGatewayFactory.create(Map.of("HMS_VNPAY_TMN_CODE", "DEMO1")));

        assertTrue(error.getMessage().contains("HMS_VNPAY_HASH_SECRET"));
    }

    @Test
    void sandboxMustBeExplicitAndIsMarkedAsSimulation() {
        PaymentGateway gateway = PaymentGatewayFactory.create(Map.of("HMS_PAYMENT_PROVIDER", "SANDBOX"));

        assertInstanceOf(SandboxPaymentGateway.class, gateway);
        assertTrue(gateway.isSimulation());
    }
}
