package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.HotelService;
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

/** F15 - Trang chi tiết dịch vụ & Gửi yêu cầu đặt dịch vụ */
@WebServlet(urlPatterns = {"/service-detail"})
public class ServiceDetailController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long serviceId = longParam(req, "id");
        HotelService service = serviceRequestService.getServiceById(serviceId);
        if (service == null || !service.isActive()) {
            resp.sendRedirect(req.getContextPath() + "/services?err="
                    + java.net.URLEncoder.encode("Dịch vụ không tồn tại hoặc đã ngưng phục vụ", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }
        req.setAttribute("service", service);

        Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
        if (c != null) {
            List<Reservation> active = reservationService.getByCustomer(c.getCustomerId()).stream()
                    .filter(r -> Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
                    .collect(java.util.stream.Collectors.toList());
            req.setAttribute("activeReservations", active);
        }
        req.getRequestDispatcher("/WEB-INF/views/service-detail.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String scheduled = req.getParameter("scheduledAt");
            LocalDateTime scheduledAt = (scheduled == null || scheduled.isEmpty()) ? null : LocalDateTime.parse(scheduled);

            serviceRequestService.createRequest(
                    longParam(req, "reservationId"),
                    longParam(req, "hotelServiceId"),
                    decimalParam(req, "quantity"),
                    scheduledAt,
                    req.getParameter("notes"));
            resp.sendRedirect(req.getContextPath() + "/services?ok=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            long serviceId = longParam(req, "hotelServiceId");
            resp.sendRedirect(req.getContextPath() + "/service-detail?id=" + serviceId + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}