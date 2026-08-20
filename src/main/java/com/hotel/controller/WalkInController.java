package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.User;
import com.hotel.service.RoomService;
import com.hotel.service.WalkInService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Walk-in: khách đến quầy đặt trực tiếp - wizard 1 trang cho lễ tân.
 * GET  /reception/walkin                     → form (kèm tra cứu khách theo giấy tờ)
 * GET  /reception/walkin?docType=&docNo=     → tra khách cũ, prefill form
 * POST /reception/walkin                     → tạo đơn + thu tiền + check-in + gán phòng
 */
@WebServlet(urlPatterns = {"/reception/walkin"})
public class WalkInController extends BaseController {

    private final WalkInService walkInService = new WalkInService();
    private final RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Tra cứu khách cũ theo giấy tờ (bước 1 của wizard)
        String docType = req.getParameter("docType");
        String docNo = req.getParameter("docNo");
        if (docType != null && docNo != null && !docNo.isBlank()) {
            Customer found = walkInService.lookupByDocument(docType, docNo);
            req.setAttribute("found", found);
            req.setAttribute("lookedUp", true);
        }
        req.setAttribute("readyRooms", walkInService.getReadyRooms());
        req.setAttribute("roomTypes", roomService.getAllActiveTypes());
        req.getRequestDispatcher("/WEB-INF/views/walkin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        try {
            WalkInService.WalkInForm f = new WalkInService.WalkInForm();
            f.idDocumentType = req.getParameter("idDocumentType");
            f.idDocumentNumber = req.getParameter("idDocumentNumber");
            f.fullName = req.getParameter("fullName");
            f.phone = req.getParameter("phone");
            f.email = req.getParameter("email");
            f.nationality = req.getParameter("nationality");
            f.roomId = longParam(req, "roomId");
            f.nights = intParam(req, "nights", 1);
            f.adults = intParam(req, "adults", 1);
            f.children = intParam(req, "children", 0);
            f.methodCode = req.getParameter("method");
            f.amount = decimalParam(req, "amount");
            f.notes = req.getParameter("notes");

            WalkInService.WalkInResult result = walkInService.processWalkIn(f, me.getUserId());

            String url = req.getContextPath() + "/reservation?id="
                    + result.reservation.getReservationId() + "&walkin=1";
            if (result.warning != null)
                url += "&err=" + URLEncoder.encode(result.warning, StandardCharsets.UTF_8);
            resp.sendRedirect(url);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/walkin?err="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
