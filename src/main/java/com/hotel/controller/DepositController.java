package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.User;
import com.hotel.service.PaymentService;
import com.hotel.payment.PaymentStartResult;
import com.hotel.service.ReservationService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/** F08 - Đặt cọc: khách thanh toán ONLINE hoặc lễ tân ghi nhận CASH/BANK_TRANSFER. */
@WebServlet(urlPatterns = {"/deposit"})
public class DepositController extends BaseController {

    private final PaymentService paymentService = new PaymentService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        long id = longParam(req, "reservationId");
        Reservation r = reservationService.getById(id);
        if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        if (!checkOwnership(req, resp, r)) return; // chống IDOR
        req.setAttribute("r", r);
        req.setAttribute("depositPaid", paymentService.getDepositPaid(id));
        req.setAttribute("outstanding", paymentService.getDepositOutstanding(r));
        exposeOnlinePayment(req);
        req.getRequestDispatcher("/WEB-INF/views/deposit.jsp").forward(req, resp);
    }

    /** Khách CUSTOMER chỉ được xem/thanh toán cọc đơn của chính mình */
    private boolean checkOwnership(HttpServletRequest req, HttpServletResponse resp, Reservation r) throws IOException {
        User me = currentUser(req);
        if (!Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) return true;
        Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
        if (c == null || c.getCustomerId() != r.getCustomerId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        User me = currentUser(req);
        long id = longParam(req, "reservationId");
        Reservation r = reservationService.getById(id);
        if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        if (!checkOwnership(req, resp, r)) return; // chống IDOR
        try {
            String method = req.getParameter("method"); // CASH | ONLINE
            BigDecimal amount = decimalParam(req, "amount");
            if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
                method = "ONLINE";                              // khách chỉ thanh toán online
                amount = paymentService.getDepositOutstanding(r); // khách KHÔNG tự quyết số tiền cọc
            }
            Long recordedBy = Constants.ROLE_CUSTOMER.equals(me.getRoleCode()) ? null : me.getUserId();
            PaymentStartResult started = paymentService.startDeposit(id, amount, method, recordedBy,
                    PaymentRequestUtil.gatewayReturnUrl(req, paymentService.getOnlinePaymentProviderName()),
                    PaymentRequestUtil.clientIp(req));
            if (started.requiresRedirect()) {
                resp.sendRedirect(started.redirectUrl());
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/reservation?id=" + id + "&paid=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/deposit?reservationId=" + id + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private boolean checkRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User me = currentUser(req);
        if (me != null && (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())
                || Constants.ROLE_RECEPTIONIST.equals(me.getRoleCode()))) return true;
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private void exposeOnlinePayment(HttpServletRequest req) {
        req.setAttribute("onlinePaymentAvailable", paymentService.isOnlinePaymentAvailable());
        req.setAttribute("onlinePaymentSimulation", paymentService.isOnlinePaymentSimulation());
        req.setAttribute("onlinePaymentDisplayName", paymentService.getOnlinePaymentDisplayName());
    }
}
