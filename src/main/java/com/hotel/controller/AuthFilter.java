package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.ultis.Constants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * - Ép UTF-8 cho mọi request
 * - /manager/*   : MANAGER only
 * - /reception/* : RECEPTIONIST only
 * - /staff/*     : SERVICE_STAFF (service requests are also available to RECEPTIONIST)
 * - /profile, /my-reservations, /booking, /services : phải đăng nhập
 */
@WebFilter(urlPatterns = { "/*" })
public class AuthFilter implements Filter {

    private static final Set<String> LOGIN_REQUIRED_PREFIX = Set.of(
            "/profile", "/my-reservations", "/reservation", "/booking", "/services", "/deposit");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI().substring(req.getContextPath().length());
        User user = (User) req.getSession().getAttribute(Constants.SESSION_USER);

        if (path.startsWith("/admin/")) {
            if (!authenticated(req, res, user, path)) return;
            if (!hasRole(user, Constants.ROLE_ADMIN)) { deny(req, res); return; }
        } else if (path.startsWith("/manager/")) {
            if (!authenticated(req, res, user, path)) return;
            if (!hasRole(user, Constants.ROLE_MANAGER)) { deny(req, res); return; }
        } else if (path.startsWith("/reception/")) {
            if (!authenticated(req, res, user, path)) return;
            if (!hasRole(user, Constants.ROLE_RECEPTIONIST, Constants.ROLE_ADMIN)) { deny(req, res); return; }
        } else if (path.startsWith("/staff/")) {
            if (!authenticated(req, res, user, path)) return;
            boolean serviceRequest = path.startsWith("/staff/service-requests");
            boolean allowed = serviceRequest
                    ? hasRole(user, Constants.ROLE_SERVICE_STAFF, Constants.ROLE_RECEPTIONIST, Constants.ROLE_ADMIN)
                    : hasRole(user, Constants.ROLE_SERVICE_STAFF);
            if (!allowed) { deny(req, res); return; }
        } else if (user == null && LOGIN_REQUIRED_PREFIX.stream().anyMatch(path::startsWith)) {
            redirectToLogin(req, res, path);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean authenticated(HttpServletRequest req, HttpServletResponse res, User user, String path) throws IOException {
        if (user != null) return true;
        redirectToLogin(req, res, path);
        return false;
    }

    private void deny(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("err", Constants.MSG_NO_PERMISSION);
        req.getRequestDispatcher("/WEB-INF/views/forbidden.jsp").forward(req, res);
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
        String target = path + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            req.getSession().setAttribute("redirectAfterLogin", target);
        }
        String encoded = java.net.URLEncoder.encode(target, java.nio.charset.StandardCharsets.UTF_8);
        res.sendRedirect(req.getContextPath() + "/login?redirect=" + encoded);
    }

    private boolean hasRole(User u, String... roles) {
        for (String r : roles)
            if (r.equals(u.getRoleCode()))
                return true;
        return false;
    }
}
