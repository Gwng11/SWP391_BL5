package com.hotel.payment;

import com.hotel.entity.Payment;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VnPayPaymentGatewayTest {
    private static final String SECRET = "TEST_SECRET_123";

    @Test
    void createsPayUrlWithVndAmountMultipliedByOneHundred() {
        VnPayPaymentGateway gateway = gateway();
        Payment payment = payment();

        String url = gateway.buildPaymentUrl(payment,
                "https://hotel.test/HotelManagement/payment/vnpay-return", "127.0.0.1");
        Map<String, String> params = query(url);

        assertEquals("2.1.0", params.get("vnp_Version"));
        assertEquals("15000000", params.get("vnp_Amount"));
        assertEquals("HMS-TEST-1", params.get("vnp_TxnRef"));
        assertNotNull(params.get("vnp_SecureHash"));
    }

    @Test
    void acceptsOnlyCorrectlySignedSuccessfulCallback() {
        VnPayPaymentGateway gateway = gateway();
        Map<String, String> callback = new LinkedHashMap<>();
        callback.put("vnp_TmnCode", "TESTCODE");
        callback.put("vnp_TxnRef", "HMS-TEST-1");
        callback.put("vnp_Amount", "15000000");
        callback.put("vnp_ResponseCode", "00");
        callback.put("vnp_TransactionStatus", "00");
        callback.put("vnp_TransactionNo", "90001");
        callback.put("vnp_SecureHash", sign(callback));

        GatewayResult result = gateway.verifyCallback(callback);

        assertTrue(result.successful());
        assertEquals("90001", result.providerReference());
        callback.put("vnp_Amount", "100");
        assertThrows(SecurityException.class, () -> gateway.verifyCallback(callback));
    }

    private VnPayPaymentGateway gateway() {
        return new VnPayPaymentGateway("TESTCODE", SECRET, "https://sandbox.vnpayment.vn/pay", null);
    }

    private Payment payment() {
        Payment payment = new Payment();
        payment.setReservationId(12);
        payment.setPaymentType("DEPOSIT");
        payment.setAmount(new BigDecimal("150000"));
        payment.setProviderReference("HMS-TEST-1");
        return payment;
    }

    private Map<String, String> query(String url) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : URI.create(url).getRawQuery().split("&")) {
            String[] entry = pair.split("=", 2);
            values.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
        }
        return values;
    }

    private String sign(Map<String, String> values) {
        try {
            StringBuilder canonical = new StringBuilder();
            new TreeMap<>(values).forEach((key, value) -> {
                if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)
                        || value == null || value.isEmpty()) return;
                if (canonical.length() > 0) canonical.append('&');
                canonical.append(URLEncoder.encode(key, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            });
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            StringBuilder result = new StringBuilder();
            for (byte b : mac.doFinal(canonical.toString().getBytes(StandardCharsets.UTF_8)))
                result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
