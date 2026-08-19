package com.hotel.controller;

import com.hotel.entity.ReservationRoom;
import com.hotel.entity.User;
import com.hotel.service.FrontDeskService;
import com.hotel.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** F11 - Gán / đổi phòng vật lý */
@WebServlet(urlPatterns = {"/reception/assign"})
public class AssignRoomController extends BaseController {

    private final ReservationService reservationService = new ReservationService();
    private final FrontDeskService frontDeskService = new FrontDeskService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long reservationId = longParam(req, "reservationId");
        req.setAttribute("r", reservationService.getById(reservationId));
        List<ReservationRoom> rooms = reservationService.getRooms(reservationId);
        req.setAttribute("rooms", rooms);
        req.setAttribute("assignments", frontDeskService.getCurrentAssignments(reservationId));
        req.setAttribute("history", frontDeskService.getAssignmentHistory(reservationId));
        Map<Long, Object> assignable = new HashMap<>();
        for (ReservationRoom rr : rooms)
            assignable.put(rr.getReservationRoomId(), frontDeskService.getAssignableRooms(rr.getReservationRoomId()));
        req.setAttribute("assignableMap", assignable);
        req.getRequestDispatcher("/WEB-INF/views/assign-room.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long reservationId = longParam(req, "reservationId");
        try {
            if ("change".equals(req.getParameter("action"))) {
                frontDeskService.changeRoom(longParam(req, "assignmentId"), longParam(req, "newRoomId"),
                        me.getUserId(), req.getParameter("reason"));
            } else {
                frontDeskService.assignRoom(longParam(req, "reservationRoomId"), longParam(req, "roomId"), me.getUserId());
            }
            resp.sendRedirect(req.getContextPath() + "/reception/assign?reservationId=" + reservationId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/assign?reservationId=" + reservationId + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
