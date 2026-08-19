package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.FrontDeskService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F10 - Check-in: tìm đơn CONFIRMED, kiểm tra giấy tờ + cọc, ghi nhận nhận phòng */
@WebServlet(urlPatterns = {"/reception/checkin"})
public class CheckInController extends BaseController {

    private final ReservationService reservationService = new ReservationService();
    private final FrontDeskService frontDeskService = new FrontDeskService();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String kw = req.getParameter("q");
        req.setAttribute("reservations", kw == null || kw.isBlank()
                ? reservationService.getByStatus(Constants.RES_CONFIRMED)
                : reservationService.search(kw, Constants.RES_CONFIRMED));
        Long detailId = longParamOrNull(req, "id");
        if (detailId != null) {
            req.setAttribute("r", reservationService.getById(detailId));
            req.setAttribute("guests", reservationService.getGuests(detailId));
            req.setAttribute("depositPaid", paymentService.getDepositPaid(detailId));
            // Cho lễ tân thấy tình trạng phòng sạch TRƯỚC khi bấm check-in
            java.util.List<com.hotel.entity.ReservationRoom> lines = reservationService.getRooms(detailId);
            java.util.Map<Long, Integer> readyMap = new java.util.HashMap<>();
            for (com.hotel.entity.ReservationRoom rr : lines)
                readyMap.put(rr.getReservationRoomId(),
                        frontDeskService.getAssignableRooms(rr.getReservationRoomId()).size());
            req.setAttribute("lines", lines);
            req.setAttribute("readyMap", readyMap);
        }
        req.getRequestDispatcher("/WEB-INF/views/checkin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "id");
        try {
            frontDeskService.checkIn(id, me.getUserId());
            resp.sendRedirect(req.getContextPath() + "/reception/assign?reservationId=" + id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/checkin?id=" + id + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
