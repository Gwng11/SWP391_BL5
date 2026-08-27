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
import java.time.format.DateTimeFormatter;

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
        if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode()) && reservationId == null) {
            req.setAttribute("browseOnly", true);
            req.getRequestDispatcher("/WEB-INF/views/service-detail.jsp").forward(req, resp);
            return;
        }
        try {
            Customer customer = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
            var currentStay = serviceRequestService.resolveCurrentStay(me, customer, reservationId);
            req.setAttribute("currentStay", currentStay);
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            req.setAttribute("serviceWindowStart",
                    serviceRequestService.serviceWindowStart(currentStay).format(inputFormat));
            req.setAttribute("serviceWindowEnd",
                    serviceRequestService.serviceWindowEnd(currentStay).minusMinutes(1).format(inputFormat));
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
            String successPath = Constants.ROLE_CUSTOMER.equals(me.getRoleCode())
                    ? "/my-service-requests?created=1" : "/services?ok=1";
            resp.sendRedirect(req.getContextPath() + successPath);
        } catch (IllegalArgumentException | IllegalStateException e) {
            long serviceId = longParam(req, "hotelServiceId");
            Long reservationId = longParamOrNull(req, "reservationId");
            String context = reservationId == null ? "" : "&reservationId=" + reservationId;
            resp.sendRedirect(req.getContextPath() + "/service-detail?id=" + serviceId + context + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
