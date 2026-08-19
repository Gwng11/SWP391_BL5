package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.User;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F07 - Xem / sửa ngày / hủy đơn đặt phòng */
@WebServlet(urlPatterns = {"/my-reservations", "/reservation"})
public class ReservationController extends BaseController {

    private final ReservationService reservationService = new ReservationService();
    private final PaymentService paymentService = new PaymentService();
    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        if ("/my-reservations".equals(req.getServletPath())) {
            Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
            req.setAttribute("reservations",
                    c == null ? java.util.List.of() : reservationService.getByCustomer(c.getCustomerId()));
            req.getRequestDispatcher("/WEB-INF/views/my-reservations.jsp").forward(req, resp);
            return;
        }
        long id = longParam(req, "id");
        Reservation r = reservationService.getById(id);
        if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        // Khách chỉ được xem đơn của chính mình
        if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
            Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
            if (c == null || c.getCustomerId() != r.getCustomerId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN); return;
            }
        }
        req.setAttribute("r", r);
        req.setAttribute("rooms", reservationService.getRooms(id));
        req.setAttribute("guests", reservationService.getGuests(id));
        req.setAttribute("payments", paymentService.getByReservation(id));
        req.setAttribute("depositPaid", paymentService.getDepositPaid(id));
        req.setAttribute("serviceRequests", serviceRequestService.getByReservation(id));
        req.getRequestDispatcher("/WEB-INF/views/reservation-detail.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "id");
        try {
            String action = req.getParameter("action");
            if ("cancel".equals(action)) {
                reservationService.cancel(id, req.getParameter("reason"), me.getUserId());
            } else if ("updateDates".equals(action)) {
                reservationService.updateDates(id, dateParam(req, "checkIn"), dateParam(req, "checkOut"), me.getUserId());
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
            resp.sendRedirect(req.getContextPath() + "/reservation?id=" + id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reservation?id=" + id + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
