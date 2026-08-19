package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.FrontDeskService;
import com.hotel.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F12 - Quản lý kỳ ở: xem khách đang ở, gia hạn/rút ngắn, phụ thu, ghi chú */
@WebServlet(urlPatterns = {"/reception/stays"})
public class StayController extends BaseController {

    private final FrontDeskService frontDeskService = new FrontDeskService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        java.util.List<com.hotel.entity.Reservation> stays = frontDeskService.getActiveStays();
        // Cảnh báo các đơn đang ở nhưng chưa gán đủ phòng (lưới an toàn cho lỗ hổng check-in)
        java.util.Map<Long, String> roomProgress = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> roomMissing = new java.util.HashMap<>();
        for (com.hotel.entity.Reservation rv : stays) {
            int[] p = frontDeskService.getAssignmentProgress(rv.getReservationId());
            roomProgress.put(rv.getReservationId(), p[0] + "/" + p[1]);
            roomMissing.put(rv.getReservationId(), p[0] < p[1]);
        }
        req.setAttribute("stays", stays);
        req.setAttribute("roomProgress", roomProgress);
        req.setAttribute("roomMissing", roomMissing);
        req.getRequestDispatcher("/WEB-INF/views/stays.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "reservationId");
        try {
            String action = req.getParameter("action");
            if ("extend".equals(action)) {
                // Gia hạn / rút ngắn: đổi ngày trả phòng (tính lại tiền, kiểm tra tồn phòng)
                com.hotel.entity.Reservation r = reservationService.getById(id);
                reservationService.updateDatesForStay(id, r.getCheckInDate(), dateParam(req, "newCheckOut"), me.getUserId());
            } else if ("extra".equals(action)) {
                frontDeskService.addExtraCharge(id, req.getParameter("description"),
                        decimalParam(req, "quantity"), decimalParam(req, "unitPrice"), me.getUserId());
            } else if ("note".equals(action)) {
                frontDeskService.addStayNote(id, req.getParameter("note"));
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
            resp.sendRedirect(req.getContextPath() + "/reception/stays");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/stays?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
