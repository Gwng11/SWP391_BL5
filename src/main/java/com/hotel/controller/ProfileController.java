package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.User;
import com.hotel.service.AuthService;
import com.hotel.service.UserService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F05 - Hồ sơ cá nhân: xem/cập nhật thông tin, đổi mật khẩu */
@WebServlet(urlPatterns = {"/profile"})
public class ProfileController extends BaseController {

    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        show(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        try {
            if ("changePassword".equals(req.getParameter("action"))) {
                if (Constants.ROLE_MANAGER.equals(me.getRoleCode()))
                    throw new IllegalStateException(Constants.MSG_NO_PERMISSION);
                authService.changePassword(me.getUserId(),
                        req.getParameter("oldPassword"), req.getParameter("newPassword"));
                req.setAttribute("msg", "Đổi mật khẩu thành công");
            } else {
                Customer extra = null;
                if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
                    extra = new Customer();
                    extra.setDateOfBirth(dateParam(req, "dateOfBirth"));
                    String docType = req.getParameter("idDocumentType");
                    String docNo = req.getParameter("idDocumentNumber");
                    extra.setIdDocumentType(docType == null || docType.isEmpty() ? null : docType);
                    extra.setIdDocumentNumber(docNo == null || docNo.isEmpty() ? null : docNo);
                    extra.setNationality(req.getParameter("nationality"));
                    extra.setAddress(req.getParameter("address"));
                }
                userService.updateProfile(me.getUserId(), req.getParameter("fullName"), req.getParameter("phone"),
                        req.getParameter("address"), req.getParameter("identificationNumber"), extra);
                // refresh session
                req.getSession().setAttribute(Constants.SESSION_USER, userService.getUser(me.getUserId()));
                req.getSession().setAttribute(Constants.SESSION_CUSTOMER, userService.getCustomerProfile(me.getUserId()));
                req.setAttribute("msg", "Cập nhật hồ sơ thành công");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("err", e.getMessage());
        }
        show(req, resp);
    }

    private void show(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        req.setAttribute("user", userService.getUser(me.getUserId()));
        req.setAttribute("customer", userService.getCustomerProfile(me.getUserId()));
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }
}
