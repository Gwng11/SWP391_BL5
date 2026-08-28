package com.hotel.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.entity.Payment;
import com.hotel.entity.User;
import com.hotel.payment.PaymentCallbackOutcome;
import com.hotel.service.PaymentService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Public browser-return and server-to-server IPN endpoints for MoMo. */
@WebServlet(urlPatterns = {"/payment/momo-return", "/payment/momo-ipn"})
public class MomoCallbackController extends BaseController {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final PaymentService paymentService;

    public MomoCallbackController() {
        this(new PaymentService());
    }

    MomoCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().endsWith("/momo-ipn")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            PaymentCallbackOutcome outcome = paymentService.handleGatewayCallback(queryParameters(req));
            String message = outcome.successful()
                    ? (outcome.alreadyProcessed() ? "Giao dịch đã được xác nhận trước đó"
                    : "Thanh toán MoMo thành công")
                    : "Thanh toán MoMo không thành công";
            resp.sendRedirect(req.getContextPath() + returnTarget(req, outcome.payment())
                    + (outcome.successful() ? "&msg=" : "&err=") + encode(message));
        } catch (SecurityException ex) {
            redirectError(req, resp, "Không thể xác minh chữ ký MoMo");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectError(req, resp, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!req.getServletPath().endsWith("/momo-ipn")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            paymentService.handleGatewayCallback(jsonParameters(req));
            // MoMo requires a successful IPN acknowledgement within 15 seconds.
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature");
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Payment not found or invalid amount");
        } catch (RuntimeException ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payment processing error");
        }
    }

    private Map<String, String> queryParameters(HttpServletRequest req) {
        Map<String, String> values = new LinkedHashMap<>();
        req.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0) values.put(key, value[0]);
        });
        return values;
    }

    private Map<String, String> jsonParameters(HttpServletRequest req) throws IOException {
        Map<String, Object> raw = JSON.readValue(req.getReader(), new TypeReference<>() {});
        Map<String, String> values = new LinkedHashMap<>();
        raw.forEach((key, value) -> values.put(key, value == null ? "" : String.valueOf(value)));
        return values;
    }

    private String returnTarget(HttpServletRequest req, Payment payment) {
        long reservationId = payment.getReservationId();
        if (Constants.PAY_DEPOSIT.equals(payment.getPaymentType()))
            return "/deposit?reservationId=" + reservationId;
        User user = currentUser(req);
        if (user != null && Constants.ROLE_CUSTOMER.equals(user.getRoleCode()))
            return "/my-invoice?reservationId=" + reservationId;
        return "/reception/invoice?reservationId=" + reservationId;
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        String value = message == null || message.isBlank() ? "Không thể xử lý kết quả MoMo" : message;
        resp.sendRedirect(req.getContextPath() + "/home?err=" + encode(value));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
