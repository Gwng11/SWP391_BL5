package com.hotel.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.entity.Payment;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** MoMo Collection Link adapter (API v2 create, HMAC-SHA256 callback verification). */
public class MomoPaymentGateway implements PaymentGateway {
    static final String DEFAULT_CREATE_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String REQUEST_TYPE = "payWithMethod";

    private final String partnerCode;
    private final String accessKey;
    private final String secretKey;
    private final String createUrl;
    private final String configuredRedirectUrl;
    private final String configuredIpnUrl;
    private final String partnerName;
    private final String storeId;
    private final JsonPoster poster;

    public MomoPaymentGateway(String partnerCode, String accessKey, String secretKey,
                              String createUrl, String configuredRedirectUrl, String configuredIpnUrl,
                              String partnerName, String storeId) {
        this(partnerCode, accessKey, secretKey, createUrl, configuredRedirectUrl, configuredIpnUrl,
                partnerName, storeId, MomoPaymentGateway::postJson);
    }

    MomoPaymentGateway(String partnerCode, String accessKey, String secretKey,
                       String createUrl, String configuredRedirectUrl, String configuredIpnUrl,
                       String partnerName, String storeId, JsonPoster poster) {
        if (!hasText(partnerCode) || !hasText(accessKey) || !hasText(secretKey)) {
            throw new IllegalStateException("Thiếu HMS_MOMO_PARTNER_CODE, HMS_MOMO_ACCESS_KEY hoặc HMS_MOMO_SECRET_KEY");
        }
        this.partnerCode = partnerCode.trim();
        this.accessKey = accessKey.trim();
        this.secretKey = secretKey.trim();
        this.createUrl = hasText(createUrl) ? createUrl.trim() : DEFAULT_CREATE_URL;
        this.configuredRedirectUrl = trimToNull(configuredRedirectUrl);
        this.configuredIpnUrl = trimToNull(configuredIpnUrl);
        this.partnerName = hasText(partnerName) ? partnerName.trim() : "Hotel Management System";
        this.storeId = hasText(storeId) ? storeId.trim() : "HMS";
        this.poster = poster;
    }

    @Override public String providerName() { return "MOMO"; }
    @Override public String displayName() { return "MoMo"; }
    @Override public boolean requiresRedirect() { return true; }

    @Override
    public GatewayResult authorize(Payment payment) {
        return GatewayResult.failed("MoMo yêu cầu chuyển hướng người dùng");
    }

    @Override
    public String buildPaymentUrl(Payment payment, String returnUrl, String clientIp) {
        String redirectUrl = configuredRedirectUrl != null ? configuredRedirectUrl : trimToNull(returnUrl);
        if (redirectUrl == null) throw new IllegalStateException("Chưa cấu hình MoMo redirectUrl");
        String ipnUrl = configuredIpnUrl != null ? configuredIpnUrl : deriveIpnUrl(redirectUrl);
        long amount = payment.getAmount().setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        if (amount < 1000) throw new IllegalArgumentException("MoMo yêu cầu số tiền tối thiểu 1.000 VND");

        String orderId = payment.getProviderReference();
        String requestId = orderId;
        String orderInfo = "Thanh toan " + payment.getPaymentType() + " don " + payment.getReservationId();
        String extraData = "";
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + REQUEST_TYPE;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("partnerName", partnerName);
        body.put("storeId", storeId);
        body.put("requestType", REQUEST_TYPE);
        body.put("ipnUrl", ipnUrl);
        body.put("redirectUrl", redirectUrl);
        body.put("orderId", orderId);
        body.put("amount", amount);
        body.put("lang", "vi");
        body.put("orderInfo", orderInfo);
        body.put("requestId", requestId);
        body.put("extraData", extraData);
        body.put("autoCapture", true);
        body.put("signature", hmac(rawSignature));

        try {
            JsonNode response = JSON.readTree(poster.post(createUrl, JSON.writeValueAsString(body)));
            int resultCode = response.path("resultCode").asInt(-1);
            String payUrl = response.path("payUrl").asText(null);
            if (resultCode != 0 || !hasText(payUrl)) {
                String message = response.path("message").asText("Không tạo được liên kết thanh toán");
                throw new IllegalStateException("MoMo từ chối tạo giao dịch (" + resultCode + "): " + message);
            }
            return payUrl;
        } catch (IOException ex) {
            throw new IllegalStateException("Phản hồi tạo giao dịch MoMo không hợp lệ", ex);
        }
    }

    @Override
    public GatewayResult verifyCallback(Map<String, String> parameters) {
        if (parameters == null) return GatewayResult.failed("Thiếu dữ liệu MoMo");
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + value(parameters, "amount")
                + "&extraData=" + value(parameters, "extraData")
                + "&message=" + value(parameters, "message")
                + "&orderId=" + value(parameters, "orderId")
                + "&orderInfo=" + value(parameters, "orderInfo")
                + "&orderType=" + value(parameters, "orderType")
                + "&partnerCode=" + value(parameters, "partnerCode")
                + "&payType=" + value(parameters, "payType")
                + "&requestId=" + value(parameters, "requestId")
                + "&responseTime=" + value(parameters, "responseTime")
                + "&resultCode=" + value(parameters, "resultCode")
                + "&transId=" + value(parameters, "transId");
        String expected = hmac(rawSignature);
        String supplied = value(parameters, "signature").toLowerCase(Locale.ROOT);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                supplied.getBytes(StandardCharsets.US_ASCII))) {
            throw new SecurityException("Chữ ký MoMo không hợp lệ");
        }
        if (!partnerCode.equals(parameters.get("partnerCode"))) {
            throw new SecurityException("partnerCode MoMo không hợp lệ");
        }
        if ("0".equals(parameters.get("resultCode"))) {
            return GatewayResult.success(parameters.get("transId"));
        }
        return GatewayResult.failed("MoMo trả mã " + value(parameters, "resultCode")
                + ": " + value(parameters, "message"));
    }

    @Override
    public String callbackMerchantReference(Map<String, String> parameters) {
        return parameters == null ? null : parameters.get("orderId");
    }

    @Override
    public BigDecimal callbackAmount(Map<String, String> parameters) {
        try {
            return new BigDecimal(parameters.get("amount"));
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Số tiền MoMo không hợp lệ", ex);
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký dữ liệu MoMo", ex);
        }
    }

    private static String deriveIpnUrl(String redirectUrl) {
        String suffix = "/payment/momo-return";
        if (!redirectUrl.endsWith(suffix)) {
            throw new IllegalStateException("Cần cấu hình HMS_MOMO_IPN_URL khi redirectUrl không kết thúc bằng " + suffix);
        }
        return redirectUrl.substring(0, redirectUrl.length() - suffix.length()) + "/payment/momo-ipn";
    }

    private static String postJson(String url, String body) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("MoMo HTTP " + response.statusCode() + " khi tạo giao dịch");
            }
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bị gián đoạn khi kết nối MoMo", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Không kết nối được MoMo", ex);
        }
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
    private static String trimToNull(String value) { return hasText(value) ? value.trim() : null; }
    private static String value(Map<String, String> values, String key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }

    @FunctionalInterface
    interface JsonPoster {
        String post(String url, String body);
    }
}
