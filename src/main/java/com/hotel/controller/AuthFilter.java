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
 * - /reception/* : RECEPTIONIST, MANAGER, ADMIN
 * - /staff/*     : SERVICE_STAFF, RECEPTIONIST, MANAGER, ADMIN
 * - /profile, /my-reservations, /booking, /services : phải đăng nhập
 */
@WebFilter(urlPatterns = {"/*"})
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

        if (path.startsWith("/reception/")) {
            if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }
            if (!hasRole(user, Constants.ROLE_RECEPTIONIST, Constants.ROLE_MANAGER, Constants.ROLE_ADMIN)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN); return;
            }
        } else if (path.startsWith("/staff/")) {
            if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }
            if (!hasRole(user, Constants.ROLE_SERVICE_STAFF, Constants.ROLE_RECEPTIONIST,
                    Constants.ROLE_MANAGER, Constants.ROLE_ADMIN)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN); return;
            }
        } else if (user == null && LOGIN_REQUIRED_PREFIX.stream().anyMatch(path::startsWith)) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean hasRole(User u, String... roles) {
        for (String r : roles) if (r.equals(u.getRoleCode())) return true;
        return false;
    }
}
