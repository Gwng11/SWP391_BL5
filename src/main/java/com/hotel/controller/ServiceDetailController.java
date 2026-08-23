package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.HotelService;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/** F15 - Trang chi tiết dịch vụ & Gửi yêu cầu đặt dịch vụ */
@WebServlet(urlPatterns = {"/service-detail"})
public class ServiceDetailController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long serviceId = longParam(req, "id");
        var me = currentUser(req);
        if (!(Constants.ROLE_CUSTOMER.equals(me.getRoleCode())
                || Constants.ROLE_RECEPTIONIST.equals(me.getRoleCode()))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        HotelService service = serviceRequestService.getActiveService(serviceId);
        if (service == null) {
            resp.sendRedirect(req.getContextPath() + "/services?err="
                    + java.net.URLEncoder.encode("Dịch vụ không tồn tại hoặc đã ngưng phục vụ", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }
        req.setAttribute("service", service);

        Long reservationId = longParamOrNull(req, "reservationId");
        try {
            Customer customer = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
            req.setAttribute("currentStay", serviceRequestService.resolveCurrentStay(me, customer, reservationId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("stayError", e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/service-detail.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var me = currentUser(req);
            Customer customer = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
            String requestedFor = req.getParameter("requestedForAt");
            LocalDateTime requestedForAt;
            try {
                requestedForAt = requestedFor == null || requestedFor.isBlank()
                        ? null : LocalDateTime.parse(requestedFor);
            } catch (java.time.DateTimeException e) {
                throw new IllegalArgumentException("Thời gian mong muốn không hợp lệ");
            }

            serviceRequestService.createRequest(
                    me,
                    customer,
                    longParamOrNull(req, "reservationId"),
                    longParam(req, "hotelServiceId"),
                    decimalParam(req, "quantity"),
                    requestedForAt,
                    req.getParameter("notes"));
            resp.sendRedirect(req.getContextPath() + "/services?ok=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            long serviceId = longParam(req, "hotelServiceId");
            Long reservationId = longParamOrNull(req, "reservationId");
            String context = reservationId == null ? "" : "&reservationId=" + reservationId;
            resp.sendRedirect(req.getContextPath() + "/service-detail?id=" + serviceId + context + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
