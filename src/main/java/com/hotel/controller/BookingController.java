package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationGuest;
import com.hotel.entity.User;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * F06 - Đặt phòng (khách online hoặc lễ tân đặt hộ).
 * GET  /booking?roomTypeId=&checkIn=&checkOut=&adults=&children=  → form
 * POST /booking → tạo đơn
 */
@WebServlet(urlPatterns = {"/booking"})
public class BookingController extends BaseController {

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long roomTypeId = longParamOrNull(req, "roomTypeId");
        var roomType = roomTypeId == null ? null : roomService.getTypeDetail(roomTypeId);
        if (roomType == null) { resp.sendRedirect(req.getContextPath() + "/rooms"); return; }
        req.setAttribute("roomType", roomType);
        req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        try {
            LocalDate checkIn = dateParam(req, "checkIn");
            LocalDate checkOut = dateParam(req, "checkOut");
            long roomTypeId = longParam(req, "roomTypeId");
            int quantity = intParam(req, "quantity", 1);
            int adults = intParam(req, "adults", 1);
            int children = intParam(req, "children", 0);

            // Xác định khách hàng: CUSTOMER tự đặt / lễ tân đặt hộ (truyền customerId)
            long customerId;
            Long createdBy = null;
            String source;
            if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
                Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
                if (c == null)
                    throw new IllegalStateException("Tài khoản chưa có hồ sơ khách hàng, vui lòng liên hệ lễ tân");
                customerId = c.getCustomerId();
                source = "ONLINE";
            } else {
                customerId = longParam(req, "customerId");
                createdBy = me.getUserId();
                source = "RECEPTIONIST";
            }

            // Khách ở chính
            List<ReservationGuest> guests = new ArrayList<>();
            String primaryName = req.getParameter("primaryGuestName");
            if (primaryName != null && !primaryName.isBlank()) {
                ReservationGuest g = new ReservationGuest();
                g.setFullName(primaryName.trim());
                g.setPrimaryGuest(true);
                guests.add(g);
            }

            List<ReservationService.RoomRequest> rooms = List.of(
                    new ReservationService.RoomRequest(roomTypeId, quantity, adults, children));
            Reservation r = reservationService.createReservation(customerId, createdBy, source,
                    checkIn, checkOut, rooms, guests, req.getParameter("specialRequests"));

            resp.sendRedirect(req.getContextPath() + "/reservation?id=" + r.getReservationId() + "&created=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("err", e.getMessage());
            Long rtId = longParamOrNull(req, "roomTypeId");
            if (rtId != null) req.setAttribute("roomType", roomService.getTypeDetail(rtId));
            req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, resp);
        }
    }
}
