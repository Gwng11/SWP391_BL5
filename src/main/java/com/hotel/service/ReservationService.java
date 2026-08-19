package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationGuest;
import com.hotel.entity.ReservationRoom;
import com.hotel.entity.RoomType;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IReservationGuestRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IReservationRoomRepository;
import com.hotel.interfaces.IRoomTypeRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.ReservationGuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ReservationRoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.ultis.CodeGenerator;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** F06 - Đặt phòng, F07 - Quản lý đơn đặt phòng */
public class ReservationService {

    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final IReservationRoomRepository resRoomRepo = new ReservationRoomRepository();
    private final IReservationGuestRepository guestRepo = new ReservationGuestRepository();
    private final IRoomTypeRepository roomTypeRepo = new RoomTypeRepository();
    private final ICustomerRepository customerRepo = new CustomerRepository();
    private final RoomService roomService = new RoomService();
    private final EmailService emailService = new EmailService();

    /** 1 dòng yêu cầu đặt: loại phòng + số lượng + số khách */
    public static class RoomRequest {
        public long roomTypeId;
        public int quantity;
        public int adults;
        public int children;
        public RoomRequest(long roomTypeId, int quantity, int adults, int children) {
            this.roomTypeId = roomTypeId; this.quantity = quantity; this.adults = adults; this.children = children;
        }
    }

    /**
     * F06: tạo đơn đặt phòng.
     * - kiểm tra tồn phòng từng loại
     * - snapshot giá theo ngày, tính thuế + tiền cọc
     * - lưu reservation + rooms + guests (1 transaction) rồi gửi email xác nhận
     */
    public Reservation createReservation(long customerId, Long createdByUserId, String sourceCode,
                                         LocalDate checkIn, LocalDate checkOut,
                                         List<RoomRequest> roomRequests, List<ReservationGuest> guests,
                                         String specialRequests) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ");
        if (checkIn.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Ngày nhận phòng không được ở quá khứ");
        if (roomRequests == null || roomRequests.isEmpty())
            throw new IllegalArgumentException("Chưa chọn phòng nào");
        // Chốt chặn: khách hàng phải tồn tại và đang hoạt động (tránh ID rác gây lỗi FK khó hiểu)
        Customer bookingCustomer = customerRepo.findById(customerId);
        if (bookingCustomer == null || !"ACTIVE".equals(bookingCustomer.getStatusCode()))
            throw new IllegalArgumentException("Khách hàng không hợp lệ hoặc đã ngừng hoạt động");

        // Giải phóng tồn phòng đang bị giữ bởi các đơn PENDING quá hạn chưa đặt cọc
        reservationRepo.cancelExpiredPending(Constants.PENDING_HOLD_HOURS);

        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        List<ReservationRoom> lines = new ArrayList<>();
        BigDecimal roomSubtotal = BigDecimal.ZERO;
        int totalAdults = 0, totalChildren = 0;

        for (RoomRequest rq : roomRequests) {
            RoomType type = roomTypeRepo.findById(rq.roomTypeId);
            if (type == null || !type.isActive()) throw new IllegalArgumentException("Loại phòng không tồn tại");
            if (rq.quantity <= 0) throw new IllegalArgumentException("Số phòng phải lớn hơn 0");
            if (rq.adults <= 0) throw new IllegalArgumentException("Phải có ít nhất 1 người lớn");
            if (rq.children < 0) throw new IllegalArgumentException("Số trẻ em không được âm");
            if (rq.adults > type.getMaxAdults() * rq.quantity || rq.children > type.getMaxChildren() * rq.quantity)
                throw new IllegalArgumentException("Vượt sức chứa của loại phòng " + type.getTypeName());
            if (!roomService.isAvailable(rq.roomTypeId, checkIn, checkOut, rq.quantity, null))
                throw new IllegalArgumentException("Loại phòng " + type.getTypeName() + " không còn đủ phòng trống");

            BigDecimal stayPricePerRoom = roomService.calcStayPrice(type, checkIn, checkOut);
            BigDecimal nightlySnapshot = stayPricePerRoom.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = stayPricePerRoom.multiply(BigDecimal.valueOf(rq.quantity)).setScale(2, RoundingMode.HALF_UP);

            ReservationRoom rr = new ReservationRoom();
            rr.setRoomTypeId(rq.roomTypeId);
            rr.setQuantity(rq.quantity);
            rr.setAdultCount(rq.adults);
            rr.setChildCount(rq.children);
            rr.setNightlyPriceSnapshot(nightlySnapshot);
            rr.setNumberOfNights(nights);
            rr.setLineTotal(lineTotal);
            lines.add(rr);

            roomSubtotal = roomSubtotal.add(lineTotal);
            totalAdults += rq.adults;
            totalChildren += rq.children;
        }

        BigDecimal tax = roomSubtotal.multiply(Constants.TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = roomSubtotal.add(tax);
        BigDecimal deposit = total.multiply(Constants.DEPOSIT_RATE).setScale(2, RoundingMode.HALF_UP);

        Reservation r = new Reservation();
        r.setCustomerId(customerId);
        r.setCreatedByUserId(createdByUserId);
        r.setBookingCode(CodeGenerator.bookingCode());
        r.setSourceCode(sourceCode);
        // Không cần cọc → xác nhận luôn; ngược lại chờ đặt cọc (F08) mới CONFIRMED
        r.setStatusCode(deposit.compareTo(BigDecimal.ZERO) == 0 ? Constants.RES_CONFIRMED : Constants.RES_PENDING);
        r.setCheckInDate(checkIn);
        r.setCheckOutDate(checkOut);
        r.setAdultCount(totalAdults);
        r.setChildCount(Math.max(totalChildren, 0));
        r.setRoomSubtotal(roomSubtotal);
        r.setTaxAmount(tax);
        r.setTotalAmount(total);
        r.setDepositRequired(deposit);
        r.setSpecialRequests(specialRequests);

        long id = reservationRepo.createFull(r, lines, guests);
        r.setReservationId(id);

        sendReservationEmail(Constants.EV_RESERVATION_CONFIRMED, r, createdByUserId);
        return r;
    }

    public Reservation getById(long id) { return reservationRepo.findById(id); }
    public Reservation getByCode(String code) { return reservationRepo.findByCode(code); }
    public List<Reservation> getByCustomer(long customerId) { return reservationRepo.findByCustomer(customerId); }
    public List<Reservation> getByStatus(String status) { return reservationRepo.findByStatus(status); }
    public List<Reservation> search(String keyword, String status) { return reservationRepo.searchByCodeOrCustomer(keyword, status); }

    /** Đánh dấu khách không đến (NO_SHOW) - giải phóng tồn phòng, giữ tiền cọc theo chính sách */
    public void markNoShow(long reservationId, Long byUserId) {
        reservationRepo.markNoShow(reservationId);
    }
    public List<ReservationRoom> getRooms(long reservationId) { return resRoomRepo.findByReservation(reservationId); }
    public List<ReservationGuest> getGuests(long reservationId) { return guestRepo.findByReservation(reservationId); }

    /** F07: đổi ngày ở (tính lại giá theo dòng phòng hiện có) */
    public void updateDates(long reservationId, LocalDate newCheckIn, LocalDate newCheckOut, Long byUserId) {
        Reservation r = requireEditable(reservationId);
        if (newCheckIn == null || newCheckOut == null || !newCheckOut.isAfter(newCheckIn))
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ");

        int nights = (int) ChronoUnit.DAYS.between(newCheckIn, newCheckOut);
        List<ReservationRoom> lines = resRoomRepo.findByReservation(reservationId);
        BigDecimal roomSubtotal = BigDecimal.ZERO;
        for (ReservationRoom rr : lines) {
            if (!roomService.isAvailable(rr.getRoomTypeId(), newCheckIn, newCheckOut, rr.getQuantity(), reservationId))
                throw new IllegalArgumentException("Loại phòng " + rr.getTypeName() + " không còn trống trong khoảng ngày mới");
            RoomType type = roomTypeRepo.findById(rr.getRoomTypeId());
            BigDecimal stayPrice = roomService.calcStayPrice(type, newCheckIn, newCheckOut);
            rr.setNightlyPriceSnapshot(stayPrice.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP));
            rr.setNumberOfNights(nights);
            rr.setLineTotal(stayPrice.multiply(BigDecimal.valueOf(rr.getQuantity())).setScale(2, RoundingMode.HALF_UP));
            roomSubtotal = roomSubtotal.add(rr.getLineTotal());
        }
        BigDecimal tax = roomSubtotal.multiply(Constants.TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        r.setCheckInDate(newCheckIn);
        r.setCheckOutDate(newCheckOut);
        r.setRoomSubtotal(roomSubtotal);
        r.setTaxAmount(tax);
        r.setTotalAmount(roomSubtotal.add(tax).add(r.getServiceTotal()));
        r.setDepositRequired(roomSubtotal.add(tax).multiply(Constants.DEPOSIT_RATE).setScale(2, RoundingMode.HALF_UP));
        reservationRepo.updateStayTotals(r, lines);

        sendReservationEmail(Constants.EV_RESERVATION_UPDATED, reservationRepo.findById(reservationId), byUserId);
    }

    /** F12: gia hạn / rút ngắn kỳ ở cho khách ĐANG Ở (CHECKED_IN) - chỉ đổi ngày trả phòng */
    public void updateDatesForStay(long reservationId, LocalDate checkIn, LocalDate newCheckOut, Long byUserId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
            throw new IllegalStateException("Chỉ gia hạn cho khách đang lưu trú");
        if (newCheckOut == null || !newCheckOut.isAfter(r.getCheckInDate()))
            throw new IllegalArgumentException("Ngày trả phòng mới không hợp lệ");

        int nights = (int) ChronoUnit.DAYS.between(r.getCheckInDate(), newCheckOut);
        List<ReservationRoom> lines = resRoomRepo.findByReservation(reservationId);
        BigDecimal roomSubtotal = BigDecimal.ZERO;
        for (ReservationRoom rr : lines) {
            if (newCheckOut.isAfter(r.getCheckOutDate())
                    && !roomService.isAvailable(rr.getRoomTypeId(), r.getCheckOutDate(), newCheckOut, rr.getQuantity(), reservationId))
                throw new IllegalArgumentException("Loại phòng " + rr.getTypeName() + " không còn trống cho phần gia hạn");
            RoomType type = roomTypeRepo.findById(rr.getRoomTypeId());
            BigDecimal stayPrice = roomService.calcStayPrice(type, r.getCheckInDate(), newCheckOut);
            rr.setNightlyPriceSnapshot(stayPrice.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP));
            rr.setNumberOfNights(nights);
            rr.setLineTotal(stayPrice.multiply(BigDecimal.valueOf(rr.getQuantity())).setScale(2, RoundingMode.HALF_UP));
            roomSubtotal = roomSubtotal.add(rr.getLineTotal());
        }
        BigDecimal tax = roomSubtotal.multiply(Constants.TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        r.setCheckOutDate(newCheckOut);
        r.setRoomSubtotal(roomSubtotal);
        r.setTaxAmount(tax);
        r.setTotalAmount(roomSubtotal.add(tax).add(r.getServiceTotal()));
        reservationRepo.updateStayTotals(r, lines);

        sendReservationEmail(Constants.EV_RESERVATION_UPDATED, reservationRepo.findById(reservationId), byUserId);
    }

    /** F07: cập nhật danh sách khách ở */
    public void updateGuests(long reservationId, List<ReservationGuest> guests, Long byUserId) {
        requireEditable(reservationId);
        guestRepo.replaceGuests(reservationId, guests);
    }

    /** F07: hủy đơn + gửi email */
    public void cancel(long reservationId, String reason, Long byUserId) {
        reservationRepo.cancel(reservationId, reason);
        sendReservationEmail(Constants.EV_RESERVATION_CANCELLED, reservationRepo.findById(reservationId), byUserId);
    }

    private Reservation requireEditable(long reservationId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_PENDING.equals(r.getStatusCode()) && !Constants.RES_CONFIRMED.equals(r.getStatusCode()))
            throw new IllegalStateException("Chỉ sửa được đơn ở trạng thái PENDING/CONFIRMED");
        return r;
    }

    private void sendReservationEmail(String eventCode, Reservation r, Long triggeredBy) {
        Customer c = customerRepo.findById(r.getCustomerId());
        if (c == null || c.getEmail() == null || c.getEmail().isEmpty()) return;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<String, String> params = new HashMap<>();
        params.put("full_name", c.getFullName());
        params.put("booking_code", r.getBookingCode());
        params.put("check_in_date", r.getCheckInDate().format(df));
        params.put("check_out_date", r.getCheckOutDate().format(df));
        params.put("total_amount", r.getTotalAmount().toPlainString());
        params.put("deposit_required", r.getDepositRequired().toPlainString());
        params.put("cancellation_reason", r.getCancellationReason() == null ? "" : r.getCancellationReason());
        emailService.send(eventCode, c.getEmail(), params, c.getUserId(), r.getReservationId(), null, null, triggeredBy);
    }
}
