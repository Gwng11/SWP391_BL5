package com.hotel.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.entity.Payment;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MomoPaymentGatewayTest {
    private static final String PARTNER = "TEST_PARTNER";
    private static final String ACCESS = "test-access";
    private static final String SECRET = "test-secret";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void createsCollectionLinkRequestWithVndAmountAndTwoPublicCallbacks() throws Exception {
        AtomicReference<String> postedUrl = new AtomicReference<>();
        AtomicReference<String> postedBody = new AtomicReference<>();
        MomoPaymentGateway gateway = gateway((url, body) -> {
            postedUrl.set(url);
            postedBody.set(body);
            return "{\"resultCode\":0,\"payUrl\":\"https://test-payment.momo.vn/pay/store/abc\"}";
        });

        String payUrl = gateway.buildPaymentUrl(payment(),
                "https://hotel.test/HotelManagement/payment/momo-return", "127.0.0.1");

        assertEquals("https://test-payment.momo.vn/pay/store/abc", payUrl);
        assertEquals(MomoPaymentGateway.DEFAULT_CREATE_URL, postedUrl.get());
        JsonNode body = JSON.readTree(postedBody.get());
        assertEquals(180000L, body.path("amount").asLong());
        assertEquals("HMS-MOMO-1", body.path("orderId").asText());
        assertEquals("payWithMethod", body.path("requestType").asText());
        assertEquals("https://hotel.test/HotelManagement/payment/momo-return",
                body.path("redirectUrl").asText());
        assertEquals("https://hotel.test/HotelManagement/payment/momo-ipn",
                body.path("ipnUrl").asText());

        String raw = "accessKey=" + ACCESS
                + "&amount=180000&extraData="
                + "&ipnUrl=https://hotel.test/HotelManagement/payment/momo-ipn"
                + "&orderId=HMS-MOMO-1"
                + "&orderInfo=Thanh toan DEPOSIT don 26"
                + "&partnerCode=" + PARTNER
                + "&redirectUrl=https://hotel.test/HotelManagement/payment/momo-return"
                + "&requestId=HMS-MOMO-1&requestType=payWithMethod";
        assertEquals(sign(raw), body.path("signature").asText());
    }

    @Test
    void verifiesSignedSuccessCallbackAndExposesProviderNeutralFields() {
        MomoPaymentGateway gateway = gateway((url, body) -> "{}");
        Map<String, String> callback = callback("0", "Thành công", "180000");

        GatewayResult result = gateway.verifyCallback(callback);

        assertTrue(result.successful());
        assertEquals("99112233", result.providerReference());
        assertEquals("HMS-MOMO-1", gateway.callbackMerchantReference(callback));
        assertEquals(0, new BigDecimal("180000").compareTo(gateway.callbackAmount(callback)));
    }

    @Test
    void rejectsAnyCallbackTampering() {
        MomoPaymentGateway gateway = gateway((url, body) -> "{}");
        Map<String, String> callback = callback("0", "Thành công", "180000");
        callback.put("amount", "1");

        assertThrows(SecurityException.class, () -> gateway.verifyCallback(callback));
    }

    @Test
    void createApiErrorDoesNotProducePaymentUrl() {
        MomoPaymentGateway gateway = gateway((url, body) ->
                "{\"resultCode\":1004,\"message\":\"Invalid amount\"}");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> gateway.buildPaymentUrl(payment(),
                        "https://hotel.test/HotelManagement/payment/momo-return", "127.0.0.1"));

        assertTrue(error.getMessage().contains("1004"));
    }

    private MomoPaymentGateway gateway(MomoPaymentGateway.JsonPoster poster) {
        return new MomoPaymentGateway(PARTNER, ACCESS, SECRET,
                MomoPaymentGateway.DEFAULT_CREATE_URL, null, null,
                "Hotel Management System", "HMS", poster);
    }

    private Payment payment() {
        Payment payment = new Payment();
        payment.setReservationId(26);
        payment.setPaymentType("DEPOSIT");
        payment.setAmount(new BigDecimal("180000"));
        payment.setProviderReference("HMS-MOMO-1");
        return payment;
    }

    private Map<String, String> callback(String resultCode, String message, String amount) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("amount", amount);
        values.put("extraData", "");
        values.put("message", message);
        values.put("orderId", "HMS-MOMO-1");
        values.put("orderInfo", "Thanh toan DEPOSIT don 26");
        values.put("orderType", "momo_wallet");
        values.put("partnerCode", PARTNER);
        values.put("payType", "qr");
        values.put("requestId", "HMS-MOMO-1");
        values.put("responseTime", "1787904000000");
        values.put("resultCode", resultCode);
        values.put("transId", "99112233");
        String raw = "accessKey=" + ACCESS
                + "&amount=" + amount
                + "&extraData=&message=" + message
                + "&orderId=HMS-MOMO-1"
                + "&orderInfo=Thanh toan DEPOSIT don 26"
                + "&orderType=momo_wallet"
                + "&partnerCode=" + PARTNER
                + "&payType=qr&requestId=HMS-MOMO-1"
                + "&responseTime=1787904000000"
                + "&resultCode=" + resultCode
                + "&transId=99112233";
        values.put("signature", sign(raw));
        return values;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            StringBuilder hex = new StringBuilder();
            for (byte b : mac.doFinal(data.getBytes(StandardCharsets.UTF_8))) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
