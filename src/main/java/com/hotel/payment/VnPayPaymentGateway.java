package com.hotel.payment;

import com.hotel.entity.Payment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Adapter VNPAY PAY 2.1.0, dùng HMAC-SHA512 theo tài liệu chính thức. */
public class VnPayPaymentGateway implements PaymentGateway {
    private static final DateTimeFormatter VNP_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private final String tmnCode;
    private final String hashSecret;
    private final String payUrl;
    private final String configuredReturnUrl;

    public VnPayPaymentGateway(String tmnCode, String hashSecret, String payUrl, String configuredReturnUrl) {
        if (tmnCode == null || tmnCode.isBlank() || hashSecret == null || hashSecret.isBlank())
            throw new IllegalStateException("Thiếu HMS_VNPAY_TMN_CODE hoặc HMS_VNPAY_HASH_SECRET");
        this.tmnCode = tmnCode.trim();
        this.hashSecret = hashSecret.trim();
        this.payUrl = payUrl;
        this.configuredReturnUrl = configuredReturnUrl;
    }

    public static VnPayPaymentGateway fromEnvironment() {
        return new VnPayPaymentGateway(
                System.getenv("HMS_VNPAY_TMN_CODE"),
                System.getenv("HMS_VNPAY_HASH_SECRET"),
                PaymentGatewayFactory.env("HMS_VNPAY_PAY_URL", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"),
                PaymentGatewayFactory.env("HMS_VNPAY_RETURN_URL", null));
    }

    @Override public String providerName() { return "VNPAY"; }
    @Override public String displayName() { return "VNPay Sandbox"; }
    @Override public boolean requiresRedirect() { return true; }

    @Override
    public GatewayResult authorize(Payment payment) {
        return GatewayResult.failed("VNPAY yêu cầu chuyển hướng người dùng");
    }

    @Override
    public String buildPaymentUrl(Payment payment, String returnUrl, String clientIp) {
        String callback = configuredReturnUrl == null || configuredReturnUrl.isBlank()
                ? returnUrl : configuredReturnUrl;
        if (callback == null || callback.isBlank())
            throw new IllegalStateException("Chưa cấu hình URL nhận kết quả VNPAY");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", payment.getAmount().movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY).toPlainString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getProviderReference());
        params.put("vnp_OrderInfo", "Thanh toan " + payment.getPaymentType()
                + " cho don " + payment.getReservationId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", callback);
        params.put("vnp_IpAddr", normalizeIp(clientIp));
        LocalDateTime now = LocalDateTime.now(VIETNAM);
        params.put("vnp_CreateDate", now.format(VNP_TIME));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNP_TIME));
        String canonical = canonical(params);
        return payUrl + "?" + canonical + "&vnp_SecureHash=" + hmac(canonical);
    }

    @Override
    public GatewayResult verifyCallback(Map<String, String> parameters) {
        if (parameters == null) return GatewayResult.failed("Thiếu dữ liệu VNPAY");
        String suppliedHash = parameters.get("vnp_SecureHash");
        TreeMap<String, String> signed = new TreeMap<>();
        parameters.forEach((key, value) -> {
            if (key.startsWith("vnp_") && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key) && value != null && !value.isEmpty()) {
                signed.put(key, value);
            }
        });
        String expected = hmac(canonical(signed));
        if (suppliedHash == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII), suppliedHash.getBytes(StandardCharsets.US_ASCII))) {
            throw new SecurityException("Chữ ký VNPAY không hợp lệ");
        }
        if (!tmnCode.equals(parameters.get("vnp_TmnCode")))
            throw new SecurityException("Mã website VNPAY không hợp lệ");
        boolean success = "00".equals(parameters.get("vnp_ResponseCode"))
                && "00".equals(parameters.get("vnp_TransactionStatus"));
        String transactionNo = parameters.get("vnp_TransactionNo");
        return success ? GatewayResult.success(transactionNo)
                : GatewayResult.failed("VNPAY trả mã " + parameters.get("vnp_ResponseCode"));
    }

    @Override
    public String callbackMerchantReference(Map<String, String> parameters) {
        return parameters == null ? null : parameters.get("vnp_TxnRef");
    }

    @Override
    public BigDecimal callbackAmount(Map<String, String> parameters) {
        try {
            return new BigDecimal(parameters.get("vnp_Amount")).movePointLeft(2);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Số tiền VNPAY không hợp lệ", ex);
        }
    }

    private String canonical(Map<String, String> params) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(params).entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
            if (out.length() > 0) out.append('&');
            out.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return out.toString();
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể ký dữ liệu VNPAY", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) return "127.0.0.1";
        String first = ip.split(",", 2)[0].trim();
        return first.isEmpty() ? "127.0.0.1" : first;
    }
}
