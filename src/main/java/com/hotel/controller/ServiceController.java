package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** F15 - Khách xem danh mục dịch vụ + tạo yêu cầu (lễ tân cũng tạo hộ được) */
@WebServlet(urlPatterns = {"/services"})
public class ServiceController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("catalog", serviceRequestService.getCatalog());
        // Đơn CHECKED_IN của khách hiện tại để chọn khi yêu cầu
        Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
        if (c != null) {
            List<Reservation> active = reservationService.getByCustomer(c.getCustomerId()).stream()
                    .filter(r -> Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
                    .collect(java.util.stream.Collectors.toList());
            req.setAttribute("activeReservations", active);
        }
        req.getRequestDispatcher("/WEB-INF/views/services.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long reservationId = longParam(req, "reservationId");
            // Chống IDOR: khách chỉ yêu cầu dịch vụ cho đơn của chính mình
            Reservation target = reservationService.getById(reservationId);
            if (target == null) throw new IllegalArgumentException("Đơn không tồn tại");
            com.hotel.entity.User me = currentUser(req);
            if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
                Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
                if (c == null || c.getCustomerId() != target.getCustomerId())
                    throw new IllegalArgumentException("Bạn chỉ được yêu cầu dịch vụ cho đơn của chính mình");
            }
            String scheduled = req.getParameter("scheduledAt"); // datetime-local: yyyy-MM-ddTHH:mm
            LocalDateTime scheduledAt = null;
            if (scheduled != null && !scheduled.isEmpty()) {
                try {
                    scheduledAt = LocalDateTime.parse(scheduled);
                } catch (java.time.format.DateTimeParseException e) {
                    throw new IllegalArgumentException("Thời gian hẹn không hợp lệ");
                }
            }
            serviceRequestService.createRequest(
                    reservationId,
                    longParam(req, "hotelServiceId"),
                    decimalParam(req, "quantity"),
                    scheduledAt,
                    req.getParameter("notes"));
            resp.sendRedirect(req.getContextPath() + "/services?ok=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/services?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
