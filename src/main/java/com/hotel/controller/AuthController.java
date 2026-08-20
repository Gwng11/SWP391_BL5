package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.repository.CustomerRepository;
import com.hotel.service.AuthService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * F04 - Đăng ký, đăng nhập, đăng xuất, xác thực email, quên/đặt lại mật khẩu
 */
@WebServlet(urlPatterns = { "/login", "/logout", "/register", "/verify", "/forgot-password", "/reset-password" })
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController() { this(new AuthService()); }

    AuthController(AuthService authService) { this.authService = authService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            var session = req.getSession(false);
            if (session != null) session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
        } else if ("/verify".equals(path)) {
            boolean ok = authService.verifyEmail(req.getParameter("token"));
            req.setAttribute(ok ? "msg" : "err",
                    ok ? "Xác thực email thành công! Bạn có thể đăng nhập."
                            : "Link xác thực không hợp lệ hoặc đã hết hạn.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } else if ("/register".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        } else if ("/forgot-password".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
        } else if ("/reset-password".equals(path)) {
            req.setAttribute("token", req.getParameter("token"));
            req.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            if ("/login".equals(path)) {
                User u = authService.login(req.getParameter("email"), req.getParameter("password"));
                HttpSession session = req.getSession();
                req.changeSessionId();
                session.setAttribute(Constants.SESSION_USER, u);
                String target = (String) session.getAttribute("redirectAfterLogin");
                session.removeAttribute("redirectAfterLogin");
                if (Constants.ROLE_CUSTOMER.equals(u.getRoleCode())) {
                    session.setAttribute(Constants.SESSION_CUSTOMER,
                            new CustomerRepository().findByUserId(u.getUserId()));
                }

                String redirect = req.getParameter("redirect");
                String destination = isSafeLocalPath(redirect) ? redirect
                        : (isSafeLocalPath(target) ? target : null);
                if (destination != null) {
                    resp.sendRedirect(req.getContextPath() + destination);
                } else if (Constants.ROLE_CUSTOMER.equals(u.getRoleCode())) {
                    resp.sendRedirect(req.getContextPath() + "/home");
                } else {
                    resp.sendRedirect(req.getContextPath() + destinationFor(u));
                }
            } else if ("/register".equals(path)) {
                authService.register(req.getParameter("email"), req.getParameter("password"),
                        req.getParameter("fullName"), req.getParameter("phone"), baseUrl(req));
                req.setAttribute("msg", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
                req.setAttribute("redirect", req.getParameter("redirect"));
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            } else if ("/forgot-password".equals(path)) {
                authService.forgotPassword(req.getParameter("email"), baseUrl(req));
                req.setAttribute("msg", "Nếu email tồn tại, link đặt lại mật khẩu đã được gửi.");
                req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
            } else if ("/reset-password".equals(path)) {
                boolean ok = authService.resetPassword(req.getParameter("token"), req.getParameter("password"));
                req.setAttribute(ok ? "msg" : "err",
                        ok ? "Đặt lại mật khẩu thành công! Hãy đăng nhập." : "Link không hợp lệ hoặc đã hết hạn.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("err", e.getMessage());
            req.setAttribute("redirect", req.getParameter("redirect"));
            String view = "/WEB-INF/views/login.jsp";
            if ("/register".equals(path))
                view = "/WEB-INF/views/register.jsp";
            else if ("/forgot-password".equals(path))
                view = "/WEB-INF/views/forgot-password.jsp";
            else if ("/reset-password".equals(path))
                view = "/WEB-INF/views/reset-password.jsp";
            req.getRequestDispatcher(view).forward(req, resp);
        } catch (RuntimeException e) {
            req.setAttribute("err", Constants.MSG_SYSTEM_ERROR);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    static String destinationFor(User user) {
        return switch (user.getRoleCode()) {
            case Constants.ROLE_MANAGER -> "/manager/dashboard";
            case Constants.ROLE_SERVICE_STAFF -> "/staff/service-requests";
            case Constants.ROLE_RECEPTIONIST -> "/reception/checkin";
            case Constants.ROLE_ADMIN -> "/reception/checkin";
            default -> "/home";
        };
    }

    private boolean isSafeLocalPath(String path) {
        return path != null && !path.isBlank() && path.startsWith("/")
                && !path.startsWith("//") && !path.contains("://");
    }
}
