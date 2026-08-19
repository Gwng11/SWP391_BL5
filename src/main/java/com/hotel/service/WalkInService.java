package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationGuest;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IRoomRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.ultis.Constants;
import com.hotel.ultis.ValidationUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Luồng WALK-IN: khách đến quầy đặt trực tiếp với lễ tân.
 * Gói toàn bộ chuỗi thành 1 nghiệp vụ:
 *   1. Nhận diện khách theo giấy tờ (có sẵn thì dùng lại, chưa có thì tạo hồ sơ walk-in)
 *   2. Tạo đơn source=WALK_IN, check-in = hôm nay
 *   3. Thu tiền tại quầy (tối thiểu = tiền cọc, khuyến nghị 100%) → đơn CONFIRMED
 *   4. Check-in ngay
 *   5. Gán luôn phòng cụ thể khách đã chọn
 * Bước 4-5 là best-effort: nếu lỗi (vd phòng vừa bị chiếm) thì đơn + tiền vẫn hợp lệ,
 * lễ tân xử lý tiếp ở màn Check-in / Gán phòng, không rollback cả chuỗi.
 */
public class WalkInService {

    private final ICustomerRepository customerRepo = new CustomerRepository();
    private final IRoomRepository roomRepo = new RoomRepository();
    private final CustomerService customerService = new CustomerService();
    private final ReservationService reservationService = new ReservationService();
    private final PaymentService paymentService = new PaymentService();
    private final FrontDeskService frontDeskService = new FrontDeskService();
    private final RoomService roomService = new RoomService();

    /** Dữ liệu form walk-in */
    public static class WalkInForm {
        public String idDocumentType;
        public String idDocumentNumber;
        public String fullName;
        public String phone;
        public String email;
        public String nationality;
        public long roomId;        // phòng vật lý khách chọn
        public int nights;
        public int adults;
        public int children;
        public String methodCode;  // CASH | CARD
        public BigDecimal amount;  // số tiền thu tại quầy
        public String notes;
    }

    public static class WalkInResult {
        public Reservation reservation;
        public Customer customer;
        public String warning; // bước check-in/gán phòng lỗi (đơn vẫn hợp lệ)
    }

    /** Danh sách phòng sẵn sàng ở NGAY (sạch + trống + chưa gán) - khác tồn kho online */
    public List<Room> getReadyRooms() {
        return roomRepo.findAllAssignableRooms();
    }

    /** Bước 1: tra khách cũ theo giấy tờ để không tạo hồ sơ trùng */
    public Customer lookupByDocument(String docType, String docNumber) {
        if (ValidationUtil.isBlank(docType) || ValidationUtil.isBlank(docNumber)) return null;
        return customerRepo.findByDocument(docType.trim(), docNumber.trim());
    }

    public WalkInResult processWalkIn(WalkInForm f, long receptionistUserId) {
        // ==== Validate: walk-in bắt buộc có giấy tờ (khai báo lưu trú) ====
        if (ValidationUtil.isBlank(f.idDocumentType) || ValidationUtil.isBlank(f.idDocumentNumber))
            throw new IllegalArgumentException("Khách walk-in bắt buộc phải có giấy tờ tùy thân");
        if (ValidationUtil.isBlank(f.fullName))
            throw new IllegalArgumentException("Họ tên không được để trống");
        if (f.nights < 1) throw new IllegalArgumentException("Số đêm tối thiểu là 1");
        Room room = roomRepo.findById(f.roomId);
        if (room == null || !room.isActive())
            throw new IllegalArgumentException("Phòng không tồn tại");
        boolean readyNow = roomRepo.findAssignableRooms(room.getRoomTypeId()).stream()
                .anyMatch(candidate -> candidate.getRoomId() == room.getRoomId());
        if (!readyNow)
            throw new IllegalStateException("Phòng vừa chọn không còn sẵn sàng; vui lòng chọn phòng khác");

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(f.nights);
        RoomType roomType = roomService.getTypeDetail(room.getRoomTypeId());
        BigDecimal subtotal = roomService.calcStayPrice(roomType, checkIn, checkOut);
        BigDecimal total = subtotal.add(subtotal.multiply(Constants.TAX_RATE))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal minimumDeposit = total.multiply(Constants.DEPOSIT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        if (f.amount == null || f.amount.compareTo(minimumDeposit) < 0)
            throw new IllegalArgumentException("Walk-in phải thu tối thiểu tiền cọc "
                    + minimumDeposit.toPlainString() + " đ");

        // ==== 1. Nhận diện khách: có sẵn theo giấy tờ thì dùng lại ====
        Customer c = customerRepo.findByDocument(f.idDocumentType.trim(), f.idDocumentNumber.trim());
        if (c == null) {
            c = new Customer();
            c.setFullName(f.fullName.trim());
            c.setPhone(ValidationUtil.isBlank(f.phone) ? null : f.phone.trim());
            c.setEmail(ValidationUtil.isBlank(f.email) ? null : f.email.trim());
            c.setIdDocumentType(f.idDocumentType.trim());
            c.setIdDocumentNumber(f.idDocumentNumber.trim());
            c.setNationality(ValidationUtil.isBlank(f.nationality) ? null : f.nationality.trim());
            long customerId = customerService.createWalkIn(c, receptionistUserId);
            c.setCustomerId(customerId);
        }

        // ==== 2. Tạo đơn WALK_IN, nhận phòng hôm nay ====
        List<ReservationGuest> guests = new ArrayList<>();
        ReservationGuest g = new ReservationGuest();
        g.setCustomerId(c.getCustomerId());
        g.setFullName(c.getFullName());
        g.setIdDocumentType(c.getIdDocumentType());
        g.setIdDocumentNumber(c.getIdDocumentNumber());
        g.setNationality(c.getNationality());
        g.setPrimaryGuest(true);
        guests.add(g);

        Reservation r = reservationService.createReservation(
                c.getCustomerId(), receptionistUserId, "WALK_IN",
                checkIn, checkOut,
                List.of(new ReservationService.RoomRequest(room.getRoomTypeId(), 1, f.adults, f.children)),
                guests, f.notes);

        // ==== 3. Thu tiền tại quầy: tối thiểu = tiền cọc, tối đa = tổng đơn ====
        BigDecimal amount = f.amount;
        if (amount == null || amount.compareTo(r.getDepositRequired()) < 0)
            throw new IllegalArgumentException("Walk-in phải thu tối thiểu tiền cọc "
                    + r.getDepositRequired().toPlainString() + " đ");
        if (amount.compareTo(r.getTotalAmount()) > 0) amount = r.getTotalAmount();
        paymentService.payDeposit(r.getReservationId(), amount,
                ValidationUtil.isBlank(f.methodCode) ? "CASH" : f.methodCode, receptionistUserId);
        // đủ cọc → PaymentService đã tự chuyển đơn sang CONFIRMED

        WalkInResult result = new WalkInResult();
        result.customer = c;

        // ==== 4-5. Check-in + gán phòng ngay (best-effort) ====
        try {
            frontDeskService.checkIn(r.getReservationId(), receptionistUserId);
            long reservationRoomId = reservationService.getRooms(r.getReservationId())
                    .get(0).getReservationRoomId();
            frontDeskService.assignRoom(reservationRoomId, f.roomId, receptionistUserId);
        } catch (RuntimeException e) {
            result.warning = "Đơn đã tạo và thu tiền thành công, nhưng bước check-in/gán phòng gặp lỗi: "
                    + e.getMessage() + ". Vui lòng xử lý tiếp ở màn Check-in / Gán phòng.";
        }
        result.reservation = reservationService.getById(r.getReservationId());
        return result;
    }
}
