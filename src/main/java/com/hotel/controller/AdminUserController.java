package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.AdminService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/admin/users" })
public class AdminUserController extends BaseController {

    private final AdminService adminService;

    public AdminUserController() {
        this(new AdminService());
    }

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String search = req.getParameter("q");
        String roleCode = req.getParameter("roleCode");
        String statusCode = req.getParameter("statusCode");

        List<User> users = adminService.listUsers(search, roleCode, statusCode);
        req.setAttribute("users", users);

        String editIdStr = req.getParameter("edit");
        if (editIdStr != null && !editIdStr.isEmpty()) {
            try {
                long editId = Long.parseLong(editIdStr);
                User editUser = adminService.getUser(editId);
                req.setAttribute("editUser", editUser);
            } catch (Exception ignored) {}
        }

        req.setAttribute("roles", List.of(
            Constants.ROLE_ADMIN,
            Constants.ROLE_MANAGER,
            Constants.ROLE_RECEPTIONIST,
            Constants.ROLE_SERVICE_STAFF,
            Constants.ROLE_CUSTOMER
        ));
        req.setAttribute("departments", List.of("GENERAL_SERVICE", "HOUSEKEEPING", "MAINTENANCE", "FRONT_DESK"));
        req.setAttribute("statuses", List.of("ACTIVE", "LOCKED", "INACTIVE"));

        req.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");
                String address = req.getParameter("address");
                String identificationNumber = req.getParameter("identificationNumber");
                String roleCode = req.getParameter("roleCode");
                String departmentCode = req.getParameter("departmentCode");

                adminService.createEmployee(email, password, fullName, phone, address, identificationNumber, roleCode, departmentCode, baseUrl(req));
                redirect(req, resp, "/admin/users", "msg", "Tạo tài khoản nhân viên thành công!");
            } else if ("update".equals(action)) {
                long userId = longParam(req, "userId");
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");
                String address = req.getParameter("address");
                String identificationNumber = req.getParameter("identificationNumber");
                String roleCode = req.getParameter("roleCode");
                String departmentCode = req.getParameter("departmentCode");
                String statusCode = req.getParameter("statusCode");
                String lockedUntil = req.getParameter("lockedUntil");

                adminService.updateUser(userId, fullName, phone, address, identificationNumber, roleCode, departmentCode, statusCode, lockedUntil);
                redirect(req, resp, "/admin/users", "msg", "Cập nhật tài khoản thành công!");
            } else if ("resetPassword".equals(action)) {
                long userId = longParam(req, "userId");
                String newPassword = req.getParameter("newPassword");

                adminService.resetUserPassword(userId, newPassword);
                redirect(req, resp, "/admin/users?edit=" + userId, "msg", "Đặt lại mật khẩu thành công!");
            } else if ("sendResetLink".equals(action)) {
                long userId = longParam(req, "userId");

                adminService.resetAndSendPasswordByEmail(userId);
                redirect(req, resp, "/admin/users?edit=" + userId, "msg", "Đã cấp mật khẩu mới và gửi thông tin qua email của nhân viên!");
            } else if ("delete".equals(action)) {
                long userId = longParam(req, "userId");
                User current = currentUser(req);
                long currentUserId = current != null ? current.getUserId() : -1;
                adminService.deleteUser(userId, currentUserId);
                redirect(req, resp, "/admin/users", "msg", "Xóa tài khoản thành công!");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("err", e.getMessage());
            doGet(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("err", e.getMessage() != null ? e.getMessage() : e.toString());
            doGet(req, resp);
        }
    }
}
