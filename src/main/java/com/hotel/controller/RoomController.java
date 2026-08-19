package com.hotel.controller;

import com.hotel.service.RoomService;
import com.hotel.entity.RoomAvailability;
import com.hotel.ultis.PageSlice;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** F02 - Tìm phòng trống, F03 - Chi tiết loại phòng */
@WebServlet(urlPatterns = {"/rooms", "/rooms/detail"})
public class RoomController extends BaseController {
    private static final int PAGE_SIZE=25;

    private final RoomService roomService;

    public RoomController() { this(new RoomService()); }
    RoomController(RoomService roomService) { this.roomService = roomService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/rooms/detail".equals(path)) {
            try{req.setAttribute("roomType", roomService.getTypeDetail(longParam(req, "id")));}
            catch(IllegalArgumentException e){resp.sendError(HttpServletResponse.SC_NOT_FOUND,e.getMessage());return;}
            req.getRequestDispatcher("/WEB-INF/views/room-detail.jsp").forward(req, resp);
            return;
        }
        // F02: form tìm kiếm + kết quả
        LocalDate checkIn = dateParam(req, "checkIn");
        LocalDate checkOut = dateParam(req, "checkOut");
        if(checkIn==null)checkIn=LocalDate.now().plusDays(1);
        if(checkOut==null)checkOut=checkIn.plusDays(1);
        int adults = intParam(req, "adults", 1);
        int children = intParam(req, "children", 0);
        req.setAttribute("checkIn",checkIn);req.setAttribute("checkOut",checkOut);
        req.setAttribute("adults",adults);req.setAttribute("children",children);
        try {
            List<RoomAvailability> allResults=roomService.searchAvailability(checkIn,checkOut,adults,children);
            PageSlice<RoomAvailability> page=PageSlice.of(allResults,intParam(req,"page",1),PAGE_SIZE);
            req.setAttribute("results",page.items());req.setAttribute("currentPage",page.currentPage());
            req.setAttribute("totalPages",page.totalPages());req.setAttribute("totalResults",page.totalItems());
            req.setAttribute("nights",ChronoUnit.DAYS.between(checkIn,checkOut));
        } catch (IllegalArgumentException e) {
            req.setAttribute("err", e.getMessage());req.setAttribute("results",List.of());
            req.setAttribute("currentPage",1);req.setAttribute("totalPages",1);req.setAttribute("totalResults",0);
        }
        req.setAttribute("roomTypes", roomService.getAllActiveTypes());
        req.getRequestDispatcher("/WEB-INF/views/rooms.jsp").forward(req, resp);
    }
}
