package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Invoice;
import com.hotel.entity.Reservation;
import com.hotel.entity.User;
import com.hotel.service.InvoiceService;
import com.hotel.service.PaymentService;
import com.hotel.payment.PaymentStartResult;
import com.hotel.service.ReservationService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Customer xem hóa đơn của mình và thanh toán online phần còn lại. */
@WebServlet(urlPatterns = {"/my-invoice"})
public class CustomerInvoiceController extends BaseController {
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public CustomerInvoiceController() {
        this(new ReservationService(), new InvoiceService(), new PaymentService());
    }

    CustomerInvoiceController(ReservationService reservationService, InvoiceService invoiceService,
                              PaymentService paymentService) {
        this.reservationService = reservationService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Reservation reservation = ownedReservation(req, resp);
        if (reservation == null) return;
        Invoice invoice = invoiceService.getByReservation(reservation.getReservationId());
        req.setAttribute("r", reservation);
        req.setAttribute("invoice", invoice);
        req.setAttribute("payments", paymentService.getByReservation(reservation.getReservationId()));
        req.setAttribute("onlinePaymentAvailable", paymentService.isOnlinePaymentAvailable());
        req.setAttribute("onlinePaymentSimulation", paymentService.isOnlinePaymentSimulation());
        req.setAttribute("onlinePaymentDisplayName", paymentService.getOnlinePaymentDisplayName());
        if (invoice != null && !Constants.INV_DRAFT.equals(invoice.getStatusCode())) {
            req.setAttribute("items", invoiceService.getItems(invoice.getInvoiceId()));
            req.setAttribute("outstanding", invoiceService.getOutstanding(invoice));
        }
        req.getRequestDispatcher("/WEB-INF/views/customer-invoice.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Reservation reservation = ownedReservation(req, resp);
        if (reservation == null) return;
        long reservationId = reservation.getReservationId();
        try {
            // Customer không được sửa phương thức hoặc số tiền bằng request thủ công.
            PaymentStartResult started = paymentService.startFinalInvoice(reservationId, "ONLINE", null,
                    PaymentRequestUtil.vnPayReturnUrl(req), PaymentRequestUtil.clientIp(req));
            if (started.requiresRedirect()) {
                resp.sendRedirect(started.redirectUrl());
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/my-invoice?reservationId=" + reservationId
                    + "&msg=" + encode("Thanh toán hóa đơn thành công"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/my-invoice?reservationId=" + reservationId
                    + "&err=" + encode(e.getMessage()));
        }
    }

    private Reservation ownedReservation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        if (user == null || !Constants.ROLE_CUSTOMER.equals(user.getRoleCode())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        long reservationId;
        try {
            reservationId = longParam(req, "reservationId");
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Customer customer = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
        if (customer == null || customer.getCustomerId() != reservation.getCustomerId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return reservation;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
