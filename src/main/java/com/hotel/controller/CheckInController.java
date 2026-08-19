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
            // Cảnh báo khách đến muộn (đơn vẫn CONFIRMED = chưa bị đánh NO_SHOW)
            com.hotel.entity.Reservation detail = reservationService.getById(detailId);
            if (detail != null && detail.getCheckInDate().isBefore(java.time.LocalDate.now()))
                req.setAttribute("lateDays", java.time.temporal.ChronoUnit.DAYS.between(
                        detail.getCheckInDate(), java.time.LocalDate.now()));
        }
        req.getRequestDispatcher("/WEB-INF/views/checkin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "id");
        try {
            if ("saveGuests".equals(req.getParameter("action"))) {
                saveGuests(req, id, me.getUserId());
                resp.sendRedirect(req.getContextPath() + "/reception/checkin?id=" + id);
                return;
            }
            frontDeskService.checkIn(id, me.getUserId());
            resp.sendRedirect(req.getContextPath() + "/reception/assign?reservationId=" + id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/checkin?id=" + id + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /** F10.2: lễ tân bổ sung/sửa danh sách khách ở + giấy tờ ngay tại quầy check-in */
    private void saveGuests(HttpServletRequest req, long reservationId, long byUserId) {
        com.hotel.entity.Reservation r = reservationService.getById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        String[] names = req.getParameterValues("gName");
        String[] docTypes = req.getParameterValues("gDocType");
        String[] docNos = req.getParameterValues("gDocNo");
        int primaryIdx = intParam(req, "primaryIdx", 0);
        java.util.List<com.hotel.entity.ReservationGuest> guests = new java.util.ArrayList<>();
        for (int i = 0; names != null && i < names.length; i++) {
            if (names[i] == null || names[i].isBlank()) continue; // bỏ dòng trống
            String dt = docTypes != null && i < docTypes.length ? docTypes[i].trim() : "";
            String dn = docNos != null && i < docNos.length ? docNos[i].trim() : "";
            if (dt.isEmpty() != dn.isEmpty())
                throw new IllegalArgumentException("Loại và số giấy tờ phải nhập cùng nhau (dòng: " + names[i] + ")");
            if (!dt.isEmpty() && !com.hotel.ultis.ValidationUtil.isValidDocument(dt, dn))
                throw new IllegalArgumentException("Số giấy tờ không hợp lệ ở dòng '" + names[i]
                        + "' (CCCD: 12 chữ số; Hộ chiếu: 6-9 ký tự chữ/số)");
            com.hotel.entity.ReservationGuest g = new com.hotel.entity.ReservationGuest();
            g.setFullName(names[i].trim());
            g.setIdDocumentType(dt.isEmpty() ? null : dt);
            g.setIdDocumentNumber(dn.isEmpty() ? null : dn);
            g.setPrimaryGuest(i == primaryIdx);
            guests.add(g);
        }
        if (guests.isEmpty()) throw new IllegalArgumentException("Danh sách khách ở không được để trống");
        if (guests.stream().noneMatch(com.hotel.entity.ReservationGuest::isPrimaryGuest))
            guests.get(0).setPrimaryGuest(true); // dòng được chọn 'chính' bị bỏ trống → lấy dòng đầu
        if (guests.size() > r.getAdultCount() + r.getChildCount())
            throw new IllegalArgumentException("Vượt số khách đã khai của đơn (tối đa "
                    + (r.getAdultCount() + r.getChildCount()) + " người)");
        reservationService.updateGuests(reservationId, guests, byUserId);
    }
}
