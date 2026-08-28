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
    void threeMomoCredentialsAutomaticallySelectMomo() {
        PaymentGateway gateway = PaymentGatewayFactory.create(Map.of(
                "HMS_MOMO_PARTNER_CODE", "PARTNER",
                "HMS_MOMO_ACCESS_KEY", "access",
                "HMS_MOMO_SECRET_KEY", "secret"));

        assertInstanceOf(MomoPaymentGateway.class, gateway);
        assertEquals("MOMO", gateway.providerName());
        assertTrue(gateway.requiresRedirect());
    }

    @Test
    void partialMomoConfigurationFailsFast() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> PaymentGatewayFactory.create(Map.of(
                        "HMS_MOMO_PARTNER_CODE", "PARTNER",
                        "HMS_MOMO_ACCESS_KEY", "access")));

        assertTrue(error.getMessage().contains("HMS_MOMO_SECRET_KEY"));
    }

    @Test
    void mixedCredentialsRequireExplicitProviderSelection() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> PaymentGatewayFactory.create(Map.of(
                        "HMS_VNPAY_TMN_CODE", "DEMO1",
                        "HMS_VNPAY_HASH_SECRET", "vnp-secret",
                        "HMS_MOMO_PARTNER_CODE", "PARTNER",
                        "HMS_MOMO_ACCESS_KEY", "access",
                        "HMS_MOMO_SECRET_KEY", "momo-secret")));

        assertTrue(error.getMessage().contains("HMS_PAYMENT_PROVIDER"));
    }

    @Test
    void sandboxMustBeExplicitAndIsMarkedAsSimulation() {
        PaymentGateway gateway = PaymentGatewayFactory.create(Map.of("HMS_PAYMENT_PROVIDER", "SANDBOX"));

        assertInstanceOf(SandboxPaymentGateway.class, gateway);
        assertTrue(gateway.isSimulation());
    }
}
