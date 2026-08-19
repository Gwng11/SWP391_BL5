package com.hotel.controller;

import com.hotel.entity.Invoice;
import com.hotel.entity.User;
import com.hotel.service.InvoiceService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/** F14 - Phát hành hóa đơn & thu phần thanh toán còn lại */
@WebServlet(urlPatterns = {"/reception/invoice"})
public class InvoiceController extends BaseController {

    private final InvoiceService invoiceService = new InvoiceService();
    private final ReservationService reservationService = new ReservationService();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long reservationId = longParam(req, "reservationId");
        req.setAttribute("r", reservationService.getById(reservationId));
        Invoice inv = invoiceService.getByReservation(reservationId);
        req.setAttribute("invoice", inv);
        if (inv != null) {
            req.setAttribute("items", invoiceService.getItems(inv.getInvoiceId()));
            req.setAttribute("outstanding", invoiceService.getOutstanding(inv));
        }
        req.setAttribute("totalPaid", paymentService.getTotalPaid(reservationId));
        req.getRequestDispatcher("/WEB-INF/views/invoice.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long reservationId = longParam(req, "reservationId");
        try {
            String action = req.getParameter("action");
            if ("generate".equals(action)) {
                invoiceService.generateFinalInvoice(reservationId, me.getUserId());
            } else if ("pay".equals(action)) {
                invoiceService.processFinalPayment(reservationId,
                        req.getParameter("method") == null ? "CASH" : req.getParameter("method"), me.getUserId());
            } else if ("voidItem".equals(action)) {
                invoiceService.voidExtraItem(reservationId, longParam(req, "itemId"), me.getUserId());
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
            resp.sendRedirect(req.getContextPath() + "/reception/invoice?reservationId=" + reservationId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/invoice?reservationId=" + reservationId + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
