package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

/** F16 - Xử lý yêu cầu dịch vụ: phân công / bắt đầu / hoàn tất / hủy */
@WebServlet(urlPatterns = {"/staff/service-requests"})
public class ServiceTaskController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        boolean dispatcher = isDispatcher(me);
        String status = req.getParameter("status");
        var requests = serviceRequestService.getWorkQueue(status == null || status.isEmpty() ? null : status);
        if (Constants.ROLE_SERVICE_STAFF.equals(me.getRoleCode())) {
            requests = requests.stream()
                    .filter(s -> Constants.SR_PENDING.equals(s.getStatusCode())
                            || (s.getAssignedStaffUserId() != null
                            && s.getAssignedStaffUserId() == me.getUserId()))
                    .collect(Collectors.toList());
        }
        req.setAttribute("requests", requests);
        req.setAttribute("isDispatcher", dispatcher);
        if (dispatcher) req.setAttribute("staffList", serviceRequestService.getAssignableStaff());
        req.setAttribute("statusFilter", status);
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
            } else if ("claim".equals(action)) {
                serviceRequestService.selfClaim(id, me);
            } else if ("start".equals(action)) {
                serviceRequestService.start(id, me);
            } else if ("complete".equals(action)) {
                serviceRequestService.complete(id, me);
            } else if ("cancel".equals(action)) {
                if (!isDispatcher(me))
                    throw new IllegalStateException("Chỉ lễ tân/quản lý được hủy yêu cầu");
                serviceRequestService.cancel(id, req.getParameter("note"));
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
            resp.sendRedirect(req.getContextPath() + "/staff/service-requests");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/staff/service-requests?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private boolean isDispatcher(User user) {
        return Constants.ROLE_RECEPTIONIST.equals(user.getRoleCode())
                || Constants.ROLE_MANAGER.equals(user.getRoleCode())
                || Constants.ROLE_ADMIN.equals(user.getRoleCode());
    }

    private void requireDispatcher(User user) {
        if (!isDispatcher(user))
            throw new IllegalStateException("Chỉ lễ tân/quản lý được phân công yêu cầu");
    }
}
