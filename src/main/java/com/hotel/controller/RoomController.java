package com.hotel.controller;

import com.hotel.service.RoomService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/** F02 - Tìm phòng trống, F03 - Chi tiết loại phòng */
@WebServlet(urlPatterns = {"/rooms", "/rooms/detail"})
public class RoomController extends BaseController {

    private final RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/rooms/detail".equals(path)) {
            req.setAttribute("roomType", roomService.getTypeDetail(longParam(req, "id")));
            req.getRequestDispatcher("/WEB-INF/views/room-detail.jsp").forward(req, resp);
            return;
        }
        // F02: form tìm kiếm + kết quả
        LocalDate checkIn = dateParam(req, "checkIn");
        LocalDate checkOut = dateParam(req, "checkOut");
        int adults = intParam(req, "adults", 1);
        int children = intParam(req, "children", 0);
        if (checkIn != null && checkOut != null) {
            try {
                req.setAttribute("results", roomService.searchAvailability(checkIn, checkOut, adults, children));
            } catch (IllegalArgumentException e) {
                req.setAttribute("err", e.getMessage());
            }
        }
        req.setAttribute("roomTypes", roomService.getAllActiveTypes());
        req.getRequestDispatcher("/WEB-INF/views/rooms.jsp").forward(req, resp);
    }
}
