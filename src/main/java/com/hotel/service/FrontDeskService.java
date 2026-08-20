package com.hotel.service;

import com.hotel.entity.Invoice;
import com.hotel.entity.InvoiceItem;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationRoom;
import com.hotel.entity.Room;
import com.hotel.entity.RoomAssignment;
import com.hotel.interfaces.IInvoiceItemRepository;
import com.hotel.interfaces.IInvoiceRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IReservationRoomRepository;
import com.hotel.interfaces.IRoomAssignmentRepository;
import com.hotel.interfaces.IRoomRepository;
import com.hotel.repository.InvoiceItemRepository;
import com.hotel.repository.InvoiceRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ReservationRoomRepository;
import com.hotel.repository.RoomAssignmentRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.ultis.CodeGenerator;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** F10 Check-in, F11 Gán/đổi phòng, F12 Quản lý kỳ ở, F13 Check-out */
public class FrontDeskService {

    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final IReservationRoomRepository resRoomRepo = new ReservationRoomRepository();
    private final IRoomAssignmentRepository assignmentRepo = new RoomAssignmentRepository();
    private final IRoomRepository roomRepo = new RoomRepository();
    private final IInvoiceRepository invoiceRepo = new InvoiceRepository();
    private final IInvoiceItemRepository invoiceItemRepo = new InvoiceItemRepository();
    private final PaymentService paymentService = new PaymentService();
    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    /** F10: check-in - yêu cầu đơn CONFIRMED và đã nộp đủ cọc */
    public void checkIn(long reservationId, long byUserId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_CONFIRMED.equals(r.getStatusCode()))
            throw new IllegalStateException("Đơn phải ở trạng thái CONFIRMED mới check-in được");
        if (paymentService.getDepositPaid(reservationId).compareTo(r.getDepositRequired()) < 0)
            throw new IllegalStateException("Khách chưa nộp đủ tiền cọc");
        reservationRepo.checkIn(reservationId, byUserId);
    }

    /** F11: danh sách phòng có thể gán cho 1 dòng đặt phòng */
    public List<Room> getAssignableRooms(long reservationRoomId) {
        ReservationRoom rr = resRoomRepo.findById(reservationRoomId);
        if (rr == null) throw new IllegalArgumentException("Dòng đặt phòng không tồn tại");
        return roomRepo.findAssignableRooms(rr.getRoomTypeId());
    }

    /** F11: gán phòng vật lý - kiểm tra đúng loại, phòng sạch & trống */
    public void assignRoom(long reservationRoomId, long roomId, long byUserId) {
        ReservationRoom rr = resRoomRepo.findById(reservationRoomId);
        if (rr == null) throw new IllegalArgumentException("Dòng đặt phòng không tồn tại");
        Reservation r = reservationRepo.findById(rr.getReservationId());
        if (!Constants.RES_CHECKED_IN.equals(r.getStatusCode()) && !Constants.RES_CONFIRMED.equals(r.getStatusCode()))
            throw new IllegalStateException("Chỉ gán phòng cho đơn CONFIRMED/CHECKED_IN");
        if (assignmentRepo.countCurrentByReservationRoom(reservationRoomId) >= rr.getQuantity())
            throw new IllegalStateException("Dòng này đã được gán đủ " + rr.getQuantity() + " phòng");
        Room room = roomRepo.findById(roomId);
        if (room == null || room.getRoomTypeId() != rr.getRoomTypeId())
            throw new IllegalArgumentException("Phòng không thuộc loại phòng đã đặt");
        if (!"AVAILABLE".equals(room.getOperationalStatus())
                || !("CLEAN".equals(room.getCleaningStatus()) || "INSPECTED".equals(room.getCleaningStatus())))
            throw new IllegalStateException("Phòng chưa sẵn sàng (bẩn/bảo trì/đang dùng)");
        // Unique index UX_room_assignments_current_room trong DB chặn double-assign lần cuối
        assignmentRepo.assign(reservationRoomId, roomId, byUserId);
    }

    /** F11: đổi phòng */
    public void changeRoom(long roomAssignmentId, long newRoomId, long byUserId, String reason) {
        Room room = roomRepo.findById(newRoomId);
        if (room == null) throw new IllegalArgumentException("Phòng mới không tồn tại");
        if (!"AVAILABLE".equals(room.getOperationalStatus())
                || !("CLEAN".equals(room.getCleaningStatus()) || "INSPECTED".equals(room.getCleaningStatus())))
            throw new IllegalStateException("Phòng mới chưa sẵn sàng");
        assignmentRepo.changeRoom(roomAssignmentId, newRoomId, byUserId, reason);
    }

    public List<RoomAssignment> getCurrentAssignments(long reservationId) {
        return assignmentRepo.findCurrentByReservation(reservationId);
    }

    public List<RoomAssignment> getAssignmentHistory(long reservationId) {
        return assignmentRepo.historyByReservation(reservationId);
    }

    /** F12: danh sách khách đang ở */
    public List<Reservation> getActiveStays() {
        return reservationRepo.findByStatus(Constants.RES_CHECKED_IN);
    }

    /** F12: thêm phụ thu (EXTRA) - tạo hóa đơn DRAFT nếu chưa có */
    public void addExtraCharge(long reservationId, String description, BigDecimal quantity,
                               BigDecimal unitPrice, long byUserId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
            throw new IllegalStateException("Chỉ thêm phụ thu cho khách đang ở");
        Invoice inv = getOrCreateDraftInvoice(r, byUserId);
        InvoiceItem item = new InvoiceItem();
        item.setInvoiceId(inv.getInvoiceId());
        item.setPostedByUserId(byUserId);
        item.setItemType("EXTRA");
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setAmount(quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP));
        invoiceItemRepo.insert(item);
    }

    /** F12: ghi chú kỳ ở */
    public void addStayNote(long reservationId, String note) {
        reservationRepo.appendSpecialRequest(reservationId, "[Ghi chú] " + note);
    }

    /**
     * F13: check-out - yêu cầu hóa đơn cuối đã thanh toán đủ (F14 thực hiện trước).
     * Trả phòng: assignment đóng lại, phòng AVAILABLE + DIRTY (chờ housekeeping F17).
     */
    public void checkOut(long reservationId, long byUserId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
            throw new IllegalStateException("Đơn không ở trạng thái CHECKED_IN");

        Invoice inv = invoiceRepo.findByReservation(reservationId);
        if (inv == null || !Constants.INV_PAID.equals(inv.getStatusCode()))
            throw new IllegalStateException("Chưa phát hành/thanh toán đủ hóa đơn cuối trước khi check-out");

        // 1. Lấy danh sách các phòng đang được gán cho đơn này
        List<RoomAssignment> currentAssignments = assignmentRepo.findCurrentByReservation(reservationId);

        // 2. Thực hiện check-out & giải phóng phòng (Phòng sẽ tự động sang AVAILABLE + DIRTY trong DB)
        reservationRepo.checkOut(reservationId, byUserId);
        assignmentRepo.releaseAllForReservation(reservationId, "Checked out");

        // 3. Tự động sinh Task dọn phòng cho đội Buồng phòng (Housekeeping)
        for (RoomAssignment ra : currentAssignments) {
            serviceRequestService.createHousekeepingTask(reservationId, r.getCustomerId(), ra.getRoomNumber());
        }
    }


    Invoice getOrCreateDraftInvoice(Reservation r, long byUserId) {
        Invoice inv = invoiceRepo.findByReservation(r.getReservationId());
        if (inv != null) {
            if (Constants.INV_PAID.equals(inv.getStatusCode()))
                throw new IllegalStateException("Hóa đơn đã thanh toán, không thêm được chi phí");
            return inv;
        }
        Invoice ni = new Invoice();
        ni.setReservationId(r.getReservationId());
        ni.setCustomerId(r.getCustomerId());
        ni.setInvoiceNumber(CodeGenerator.invoiceNumber());
        ni.setSubtotal(BigDecimal.ZERO);
        ni.setTaxAmount(BigDecimal.ZERO);
        ni.setTotalAmount(BigDecimal.ZERO);
        ni.setPaidAmount(BigDecimal.ZERO);
        ni.setStatusCode(Constants.INV_DRAFT);
        long id = invoiceRepo.insert(ni);
        ni.setInvoiceId(id);
        return ni;
    }
}
