package com.hotel.controller;

import com.hotel.entity.Reservation;
import com.hotel.entity.User;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/** F08 - Đặt cọc: khách thanh toán ONLINE (giả lập) hoặc lễ tân thu CASH */
@WebServlet(urlPatterns = {"/deposit"})
public class DepositController extends BaseController {

    private final PaymentService paymentService = new PaymentService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id = longParam(req, "reservationId");
        Reservation r = reservationService.getById(id);
        if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        req.setAttribute("r", r);
        req.setAttribute("depositPaid", paymentService.getDepositPaid(id));
        req.setAttribute("outstanding", paymentService.getDepositOutstanding(r));
        req.getRequestDispatcher("/WEB-INF/views/deposit.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "reservationId");
        try {
            String method = req.getParameter("method"); // CASH | ONLINE
            // Khách hàng chỉ được thanh toán online
            if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) method = "ONLINE";
            BigDecimal amount = decimalParam(req, "amount");
            Long recordedBy = Constants.ROLE_CUSTOMER.equals(me.getRoleCode()) ? null : me.getUserId();
            paymentService.payDeposit(id, amount, method, recordedBy);
            resp.sendRedirect(req.getContextPath() + "/reservation?id=" + id + "&paid=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/deposit?reservationId=" + id + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
