package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.ultis.Constants;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Helper chung cho các servlet */
public abstract class BaseController extends HttpServlet {

    protected User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(Constants.SESSION_USER);
    }

    protected long longParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isEmpty()) throw new IllegalArgumentException("Thiếu tham số " + name);
        return Long.parseLong(v);
    }

    protected Long longParamOrNull(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.isEmpty()) ? null : Long.parseLong(v);
    }

    protected int intParam(HttpServletRequest req, String name, int defaultValue) {
        String v = req.getParameter(name);
        try { return Integer.parseInt(v); } catch (Exception e) { return defaultValue; }
    }

    protected LocalDate dateParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.isEmpty()) ? null : LocalDate.parse(v); // yyyy-MM-dd từ <input type=date>
    }

    protected BigDecimal decimalParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.isEmpty()) ? null : new BigDecimal(v);
    }

    /** Base URL để tạo link trong email (verify, reset password) */
    protected String baseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        int port = req.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        return scheme + "://" + req.getServerName() + (defaultPort ? "" : ":" + port) + req.getContextPath();
    }
}
