package com.hotel.controller;

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

/** Public Return URL/IPN endpoints used by VNPAY after the customer leaves the hotel site. */
@WebServlet(urlPatterns = {"/payment/vnpay-return", "/payment/vnpay-ipn"})
public class VnPayCallbackController extends BaseController {
    private final PaymentService paymentService;

    public VnPayCallbackController() {
        this(new PaymentService());
    }

    VnPayCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().endsWith("/vnpay-ipn")) handleIpn(req, resp);
        else handleReturn(req, resp);
    }

    private void handleReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PaymentCallbackOutcome outcome = paymentService.handleGatewayCallback(parameters(req));
            Payment payment = outcome.payment();
            String target = returnTarget(req, payment);
            String message = outcome.successful()
                    ? (outcome.alreadyProcessed() ? "Giao dịch đã được xác nhận trước đó"
                    : "Thanh toán VNPay thành công")
                    : "Thanh toán VNPay không thành công";
            resp.sendRedirect(req.getContextPath() + target
                    + (outcome.successful() ? "&msg=" : "&err=") + encode(message));
        } catch (SecurityException ex) {
            redirectError(req, resp, "Không thể xác minh chữ ký VNPay");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectError(req, resp, ex.getMessage());
        }
    }

    private void handleIpn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            PaymentCallbackOutcome outcome = paymentService.handleGatewayCallback(parameters(req));
            if (outcome.alreadyProcessed()) writeJson(resp, "02", "Order already confirmed");
            else writeJson(resp, "00", "Confirm Success");
        } catch (SecurityException ex) {
            writeJson(resp, "97", "Invalid Checksum");
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage();
            if (message.contains("Số tiền")) writeJson(resp, "04", "Invalid Amount");
            else writeJson(resp, "01", "Order not found");
        } catch (RuntimeException ex) {
            writeJson(resp, "99", "Unknown error");
        }
    }

    private Map<String, String> parameters(HttpServletRequest req) {
        Map<String, String> values = new LinkedHashMap<>();
        req.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0) values.put(key, value[0]);
        });
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
        resp.sendRedirect(req.getContextPath() + "/home?err=" + encode(
                message == null || message.isBlank() ? "Không thể xử lý kết quả VNPay" : message));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void writeJson(HttpServletResponse resp, String code, String message) throws IOException {
        resp.getWriter().write("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}");
    }
}
