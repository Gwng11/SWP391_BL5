package com.hotel.controller;

import com.hotel.service.HotelInfoService;
import com.hotel.service.RoomService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F01 - Trang chủ: thông tin khách sạn, chính sách, giờ nhận/trả phòng */
@WebServlet(urlPatterns = {"/home", ""})
public class HomeController extends BaseController {

    private final HotelInfoService hotelInfoService = new HotelInfoService();
    private final RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("hotel", hotelInfoService.getProfile());
        req.setAttribute("roomTypes", roomService.getAllActiveTypes());
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
