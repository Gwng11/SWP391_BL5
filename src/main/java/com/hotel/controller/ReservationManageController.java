package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Màn QUẢN LÝ ĐƠN ĐẶT PHÒNG cho lễ tân - mắt xích nối các luồng:
 * - Tìm đơn theo mã / tên / SĐT, lọc theo trạng thái (kể cả PENDING mà màn check-in không hiện)
 * - Hành động theo ngữ cảnh: PENDING → Thu cọc; CONFIRMED → Check-in / Không đến (NO_SHOW);
 *   CHECKED_IN → Phòng / Hóa đơn; mọi đơn → Xem chi tiết
 */
@WebServlet(urlPatterns = {"/reception/reservations"})
public class ReservationManageController extends BaseController {

    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        String status = req.getParameter("status");
        if (status != null && status.isEmpty()) status = null;
        req.setAttribute("reservations", reservationService.search(q, status));
        req.setAttribute("q", q);
        req.setAttribute("statusFilter", status);
        req.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "id");
        try {
            if ("noshow".equals(req.getParameter("action"))) {
                reservationService.markNoShow(id, me.getUserId());
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
            resp.sendRedirect(req.getContextPath() + "/reception/reservations");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/reservations?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
