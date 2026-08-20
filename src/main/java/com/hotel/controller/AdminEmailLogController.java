package com.hotel.controller;

import com.hotel.entity.EmailLog;
import com.hotel.service.AdminService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/admin/email-logs" })
public class AdminEmailLogController extends BaseController {

    private final AdminService adminService;

    public AdminEmailLogController() {
        this(new AdminService());
    }

    public AdminEmailLogController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String search = req.getParameter("q");
        String status = req.getParameter("status");

        List<EmailLog> logs = adminService.listEmailLogs(search, status);
        req.setAttribute("logs", logs);
        req.setAttribute("statuses", List.of("QUEUED", "SENT", "FAILED"));

        req.getRequestDispatcher("/WEB-INF/views/admin-email-logs.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("retry".equals(action)) {
                long emailLogId = longParam(req, "emailLogId");
                boolean ok = adminService.retryEmailLog(emailLogId);
                if (ok) {
                    redirect(req, resp, "/admin/email-logs", "msg", "Gửi lại email thành công!");
                } else {
                    redirect(req, resp, "/admin/email-logs", "err", "Gửi lại email thất bại. Vui lòng kiểm tra nhật ký lỗi!");
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("err", e.getMessage());
            doGet(req, resp);
        } catch (Exception e) {
            req.setAttribute("err", Constants.MSG_SYSTEM_ERROR);
            doGet(req, resp);
        }
    }
}
