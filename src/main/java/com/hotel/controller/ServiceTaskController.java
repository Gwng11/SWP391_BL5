package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F16 - Xử lý yêu cầu dịch vụ: phân công / bắt đầu / hoàn tất / hủy */
@WebServlet(urlPatterns = {"/reception/service-requests", "/staff/service-requests"})
public class ServiceTaskController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        boolean dispatcher = isDispatcher(me);
        String status = req.getParameter("status");
        var requests = serviceRequestService.getRequestsFor(me, status == null || status.isEmpty() ? null : status);
        req.setAttribute("requests", requests);
        req.setAttribute("isDispatcher", dispatcher);
        if (dispatcher) req.setAttribute("staffList", serviceRequestService.getAssignableStaff());
        req.setAttribute("statusFilter", status);
        req.setAttribute("taskUrl", taskUrl(me));
        req.getRequestDispatcher("/WEB-INF/views/service-tasks.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "id");
        try {
            String action = req.getParameter("action");
            if ("assign".equals(action)) {
                requireDispatcher(me);
                serviceRequestService.assign(id, longParam(req, "staffUserId"));
            } else if ("assignAuto".equals(action)) {
                requireDispatcher(me);
                serviceRequestService.assignAuto(id);
            } else if ("start".equals(action)) {
                serviceRequestService.start(id, me);
            } else if ("complete".equals(action)) {
                serviceRequestService.complete(id, me);
            } else if ("unable".equals(action)) {
                serviceRequestService.reportUnable(id, me, req.getParameter("reason"));
            } else if ("reschedule".equals(action)) {
                requireDispatcher(me);
                String value = req.getParameter("requestedForAt");
                try {
                    if (value == null || value.isBlank()) throw new java.time.DateTimeException("missing");
                    serviceRequestService.reschedule(id, java.time.LocalDateTime.parse(value), req.getParameter("note"));
                } catch (java.time.DateTimeException e) {
                    throw new IllegalArgumentException("Thời gian phục vụ mới không hợp lệ");
                }
            } else if ("cancel".equals(action)) {
                if (!isDispatcher(me))
                    throw new IllegalStateException("Chỉ lễ tân/quản lý được hủy yêu cầu");
                serviceRequestService.cancel(id, req.getParameter("note"));
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
            resp.sendRedirect(req.getContextPath() + taskUrl(me));
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + taskUrl(me) + "?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private boolean isDispatcher(User user) {
        return Constants.ROLE_RECEPTIONIST.equals(user.getRoleCode());
    }

    private void requireDispatcher(User user) {
        if (!isDispatcher(user))
            throw new IllegalStateException("Chỉ lễ tân/quản lý được phân công yêu cầu");
    }

    private String taskUrl(User user) {
        return Constants.ROLE_RECEPTIONIST.equals(user.getRoleCode())
                ? "/reception/service-requests" : "/staff/service-requests";
    }
}
