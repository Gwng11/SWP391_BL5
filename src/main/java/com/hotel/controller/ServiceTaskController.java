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
@WebServlet(urlPatterns = {"/staff/service-requests"})
public class ServiceTaskController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String status = req.getParameter("status");
        req.setAttribute("requests",
                serviceRequestService.getWorkQueue(status == null || status.isEmpty() ? null : status));
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
                // Lễ tân phân công cho nhân viên khác; nhân viên tự nhận việc cho mình
                Long staffId = longParamOrNull(req, "staffUserId");
                serviceRequestService.assign(id, staffId != null ? staffId : me.getUserId());
            } else if ("start".equals(action)) {
                serviceRequestService.start(id);
            } else if ("complete".equals(action)) {
                serviceRequestService.complete(id);
            } else if ("cancel".equals(action)) {
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
}
