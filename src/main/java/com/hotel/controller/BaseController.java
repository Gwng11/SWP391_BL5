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
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + name);
        }
    }

    protected Long longParamOrNull(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isEmpty()) return null;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return null; // tham số tùy chọn hỏng định dạng → coi như không truyền
        }
    }

    protected int intParam(HttpServletRequest req, String name, int defaultValue) {
        String v = req.getParameter(name);
        try { return Integer.parseInt(v); } catch (Exception e) { return defaultValue; }
    }

    protected LocalDate dateParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isEmpty()) return null;
        try {
            return LocalDate.parse(v.trim()); // yyyy-MM-dd từ <input type=date>
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (định dạng yyyy-MM-dd): " + v);
        }
    }

    protected BigDecimal decimalParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isEmpty()) return null;
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số tiền/số lượng không hợp lệ: " + v);
        }
    }

    /** Base URL để tạo link trong email (verify, reset password) */
    protected String baseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        int port = req.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        return scheme + "://" + req.getServerName() + (defaultPort ? "" : ":" + port) + req.getContextPath();
    }
}
