package com.hotel.controller;

import com.hotel.entity.EmailLog;
import com.hotel.entity.EmailTemplate;
import com.hotel.entity.User;
import com.hotel.service.AdminService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/admin/dashboard" })
public class AdminDashboardController extends BaseController {

    private final AdminService adminService;

    public AdminDashboardController() {
        this(new AdminService());
    }

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = adminService.listUsers(null, null, null);
        List<EmailTemplate> templates = adminService.listTemplates();
        List<EmailLog> logs = adminService.listEmailLogs(null, null);

        long totalUsers = users.size();
        long staffCount = users.stream().filter(u -> !Constants.ROLE_CUSTOMER.equals(u.getRoleCode())).count();
        long customerCount = users.stream().filter(u -> Constants.ROLE_CUSTOMER.equals(u.getRoleCode())).count();
        long lockedCount = users.stream().filter(u -> "LOCKED".equals(u.getStatusCode())).count();

        long totalTemplates = templates.size();
        long activeTemplates = templates.stream().filter(EmailTemplate::isActive).count();

        long sentLogs = logs.stream().filter(l -> "SENT".equals(l.getStatusCode())).count();
        long failedLogs = logs.stream().filter(l -> "FAILED".equals(l.getStatusCode())).count();

        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("staffCount", staffCount);
        req.setAttribute("customerCount", customerCount);
        req.setAttribute("lockedCount", lockedCount);
        req.setAttribute("totalTemplates", totalTemplates);
        req.setAttribute("activeTemplates", activeTemplates);
        req.setAttribute("sentLogs", sentLogs);
        req.setAttribute("failedLogs", failedLogs);

        // Pass top 5 recent users and logs for dashboard quick tables
        req.setAttribute("recentUsers", users.stream().limit(5).toList());
        req.setAttribute("recentLogs", logs.stream().limit(5).toList());

        req.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(req, resp);
    }
}
