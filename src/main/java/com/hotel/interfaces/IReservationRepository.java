package com.hotel.interfaces;

import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationGuest;
import com.hotel.entity.ReservationRoom;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IReservationRepository {
    /** F06: tạo reservation + reservation_rooms + reservation_guests trong 1 transaction */
    long createFull(Reservation r, List<ReservationRoom> rooms, List<ReservationGuest> guests);
    Reservation findById(long reservationId);
    Reservation findByCode(String bookingCode);
    List<Reservation> findByCustomer(long customerId);
    List<Reservation> findByStatus(String statusCode);
    List<Reservation> searchByCodeOrCustomer(String keyword, String statusCode);
    /** F02/F06: số phòng của 1 loại đã bị giữ trong khoảng ngày (PENDING/CONFIRMED/CHECKED_IN) */
    int countSoldRooms(long roomTypeId, LocalDate checkIn, LocalDate checkOut, Long excludeReservationId);
    void updateStatus(long reservationId, String statusCode);
    /** Đánh dấu NO_SHOW: đơn CONFIRMED đã qua ngày nhận phòng mà khách không đến */
    void markNoShow(long reservationId);
    /** Tự hủy các đơn PENDING quá hạn giữ chỗ mà chưa nộp đủ cọc. @return số đơn bị hủy */
    int cancelExpiredPending(int holdHours);
    void cancel(long reservationId, String reason);
    void checkIn(long reservationId, long byUserId);
    void checkOut(long reservationId, long byUserId);
    /** F07/F12: đổi ngày ở + thay dòng phòng + cập nhật tiền (transaction) */
    void updateDatesAndRooms(Reservation r, List<ReservationRoom> newRooms);
    /** F07/F12: cập nhật ngày + tiền, GIỮ NGUYÊN các dòng reservation_rooms (update tại chỗ,
     *  an toàn khi đã có room_assignments trỏ FK vào) */
    void updateStayTotals(Reservation r, List<ReservationRoom> lines);
    /** F16: cộng tiền dịch vụ vào service_total và total_amount */
    void addServiceTotal(long reservationId, BigDecimal delta);
    void appendSpecialRequest(long reservationId, String note);
}
