package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationGuest;
import com.hotel.entity.User;
import com.hotel.service.CustomerService;
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
    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long roomTypeId = longParamOrNull(req, "roomTypeId");
        var roomType = roomTypeId == null ? null : roomService.getTypeDetail(roomTypeId);
        if (roomType == null) { resp.sendRedirect(req.getContextPath() + "/rooms"); return; }
        req.setAttribute("roomType", roomType);
        // Lễ tân đặt hộ: tìm khách theo SĐT/CCCD/tên thay vì nhập ID tay
        User me = currentUser(req);
        if (me != null && !Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
            String q = req.getParameter("q");
            if (q != null && !q.isBlank()) req.setAttribute("customerResults", customerService.search(q));
            Long selectedId = longParamOrNull(req, "customerId");
            if (selectedId != null) req.setAttribute("selectedCustomer", customerService.getById(selectedId));
        }
        req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, resp);
    }

    /** Chuỗi query giữ ngữ cảnh đặt phòng (ngày, số khách) khi tìm/tạo khách */
    private String bookingContext(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder("roomTypeId=" + req.getParameter("roomTypeId"));
        for (String p : new String[]{"checkIn", "checkOut", "quantity", "adults", "children"}) {
            String v = req.getParameter(p);
            if (v != null && !v.isBlank()) sb.append("&").append(p).append("=").append(v);
        }
        return sb.toString();
    }

    /** Lễ tân tạo nhanh hồ sơ khách ngay trong trang đặt phòng, tạo xong tự chọn luôn */
    private void quickCreateCustomer(HttpServletRequest req, HttpServletResponse resp, User me) throws IOException {
        try {
            Customer c = new Customer();
            c.setFullName(req.getParameter("newFullName"));
            String phone = req.getParameter("newPhone");
            String docType = req.getParameter("newDocType");
            String docNo = req.getParameter("newDocNumber");
            c.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
            c.setIdDocumentType(docType == null || docType.isBlank() ? null : docType.trim());
            c.setIdDocumentNumber(docNo == null || docNo.isBlank() ? null : docNo.trim());
            long newId = customerService.createWalkIn(c, me.getUserId());
            resp.sendRedirect(req.getContextPath() + "/booking?" + bookingContext(req) + "&customerId=" + newId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/booking?" + bookingContext(req)
                    + "&q=&err=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        // Lễ tân tạo nhanh hồ sơ khách ngay trong trang đặt phòng
        if ("createCustomer".equals(req.getParameter("action"))
                && !Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
            quickCreateCustomer(req, resp, me);
            return;
        }
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
                Long picked = longParamOrNull(req, "customerId");
                if (picked == null)
                    throw new IllegalArgumentException("Chưa chọn khách hàng — hãy tìm và chọn khách trước khi đặt phòng");
                customerId = picked;
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
            Long pickedId = longParamOrNull(req, "customerId");
            if (pickedId != null && !Constants.ROLE_CUSTOMER.equals(me.getRoleCode()))
                req.setAttribute("selectedCustomer", customerService.getById(pickedId));
            req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, resp);
        }
    }
}
