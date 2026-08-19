package com.hotel.repository;

import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationGuest;
import com.hotel.entity.ReservationRoom;
import com.hotel.interfaces.IReservationRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** F06, F07, F10, F12, F13 - Đặt phòng */
public class ReservationRepository extends BaseRepository implements IReservationRepository {

    private static final String BASE_SELECT =
            "SELECT r.*, c.full_name AS customer_name, c.email AS customer_email "
          + "FROM reservations r JOIN customers c ON c.customer_id = r.customer_id ";

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getLong("reservation_id"));
        r.setCustomerId(rs.getLong("customer_id"));
        r.setCreatedByUserId(longOf(rs, "created_by_user_id"));
        r.setCheckedInByUserId(longOf(rs, "checked_in_by_user_id"));
        r.setCheckedOutByUserId(longOf(rs, "checked_out_by_user_id"));
        r.setBookingCode(rs.getString("booking_code"));
        r.setSourceCode(rs.getString("source_code"));
        r.setStatusCode(rs.getString("status_code"));
        r.setBookedAt(tsOf(rs, "booked_at"));
        r.setCheckInDate(dateOf(rs, "check_in_date"));
        r.setCheckOutDate(dateOf(rs, "check_out_date"));
        r.setActualCheckInAt(tsOf(rs, "actual_check_in_at"));
        r.setActualCheckOutAt(tsOf(rs, "actual_check_out_at"));
        r.setAdultCount(rs.getInt("adult_count"));
        r.setChildCount(rs.getInt("child_count"));
        r.setRoomSubtotal(rs.getBigDecimal("room_subtotal"));
        r.setServiceTotal(rs.getBigDecimal("service_total"));
        r.setTaxAmount(rs.getBigDecimal("tax_amount"));
        r.setTotalAmount(rs.getBigDecimal("total_amount"));
        r.setDepositRequired(rs.getBigDecimal("deposit_required"));
        r.setSpecialRequests(rs.getString("special_requests"));
        r.setCancellationReason(rs.getString("cancellation_reason"));
        r.setCreatedAt(tsOf(rs, "created_at"));
        r.setUpdatedAt(tsOf(rs, "updated_at"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setCustomerEmail(rs.getString("customer_email"));
        return r;
    }

    @Override
    public long createFull(Reservation r, List<ReservationRoom> rooms, List<ReservationGuest> guests) {
        String insRes = "INSERT INTO reservations (customer_id, created_by_user_id, booking_code, source_code, "
                + "status_code, check_in_date, check_out_date, adult_count, child_count, room_subtotal, "
                + "tax_amount, total_amount, deposit_required, special_requests) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insRoom = "INSERT INTO reservation_rooms (reservation_id, room_type_id, quantity, adult_count, "
                + "child_count, nightly_price_snapshot, number_of_nights, line_total, notes) VALUES (?,?,?,?,?,?,?,?,?)";
        String insGuest = "INSERT INTO reservation_guests (reservation_id, customer_id, full_name, date_of_birth, "
                + "id_document_type, id_document_number, nationality, is_primary_guest) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                lockAndCheckAvailability(cn, r, rooms);
                long reservationId;
                try (PreparedStatement ps = cn.prepareStatement(insRes, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, r.getCustomerId());
                    bindLong(ps, 2, r.getCreatedByUserId());
                    ps.setString(3, r.getBookingCode());
                    ps.setString(4, r.getSourceCode());
                    ps.setString(5, r.getStatusCode());
                    ps.setDate(6, Date.valueOf(r.getCheckInDate()));
                    ps.setDate(7, Date.valueOf(r.getCheckOutDate()));
                    ps.setInt(8, r.getAdultCount());
                    ps.setInt(9, r.getChildCount());
                    ps.setBigDecimal(10, r.getRoomSubtotal());
                    ps.setBigDecimal(11, r.getTaxAmount());
                    ps.setBigDecimal(12, r.getTotalAmount());
                    ps.setBigDecimal(13, r.getDepositRequired());
                    ps.setString(14, r.getSpecialRequests());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); reservationId = keys.getLong(1); }
                }
                try (PreparedStatement ps = cn.prepareStatement(insRoom)) {
                    for (ReservationRoom rr : rooms) {
                        ps.setLong(1, reservationId);
                        ps.setLong(2, rr.getRoomTypeId());
                        ps.setInt(3, rr.getQuantity());
                        ps.setInt(4, rr.getAdultCount());
                        ps.setInt(5, rr.getChildCount());
                        ps.setBigDecimal(6, rr.getNightlyPriceSnapshot());
                        ps.setInt(7, rr.getNumberOfNights());
                        ps.setBigDecimal(8, rr.getLineTotal());
                        ps.setString(9, rr.getNotes());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                if (guests != null && !guests.isEmpty()) {
                    try (PreparedStatement ps = cn.prepareStatement(insGuest)) {
                        for (ReservationGuest g : guests) {
                            ps.setLong(1, reservationId);
                            bindLong(ps, 2, g.getCustomerId());
                            ps.setString(3, g.getFullName());
                            bindDate(ps, 4, g.getDateOfBirth());
                            ps.setString(5, g.getIdDocumentType());
                            ps.setString(6, g.getIdDocumentNumber());
                            ps.setString(7, g.getNationality());
                            ps.setBoolean(8, g.isPrimaryGuest());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                cn.commit();
                return reservationId;
            } catch (Exception ex) {
                cn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw (RuntimeException) ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    /**
     * Serialize sales per room type and re-check inventory inside the same
     * transaction that inserts the reservation. Lock order is room_type_id to
     * avoid deadlocks when one reservation contains multiple room types.
     */
    private void lockAndCheckAvailability(Connection cn, Reservation reservation,
                                          List<ReservationRoom> rooms) throws SQLException {
        Map<Long, Integer> requestedByType = new TreeMap<>();
        for (ReservationRoom room : rooms) {
            requestedByType.merge(room.getRoomTypeId(), room.getQuantity(), Integer::sum);
        }

        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM rooms p WHERE p.room_type_id = rt.room_type_id "
                + " AND p.is_active = 1 AND p.operational_status <> 'OUT_OF_SERVICE') AS total_rooms, "
                + "(SELECT COALESCE(SUM(rr.quantity), 0) FROM reservation_rooms rr "
                + " JOIN reservations r WITH (HOLDLOCK) ON r.reservation_id = rr.reservation_id "
                + " WHERE rr.room_type_id = rt.room_type_id "
                + " AND r.status_code IN ('PENDING','CONFIRMED','CHECKED_IN') "
                + " AND r.check_in_date < ? AND r.check_out_date > ?) AS sold_rooms, "
                + "(SELECT COUNT(*) FROM room_rates rate WHERE rate.room_type_id = rt.room_type_id "
                + " AND rate.rate_date >= ? AND rate.rate_date < ? AND rate.stop_sell = 1) AS stop_sell "
                + "FROM room_types rt WITH (UPDLOCK, HOLDLOCK) WHERE rt.room_type_id = ? AND rt.is_active = 1";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Map.Entry<Long, Integer> entry : requestedByType.entrySet()) {
                ps.setDate(1, Date.valueOf(reservation.getCheckOutDate()));
                ps.setDate(2, Date.valueOf(reservation.getCheckInDate()));
                ps.setDate(3, Date.valueOf(reservation.getCheckInDate()));
                ps.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
                ps.setLong(5, entry.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Loại phòng không tồn tại hoặc đã ngừng bán");
                    if (rs.getInt("stop_sell") > 0)
                        throw new IllegalStateException("Loại phòng đang tạm ngừng bán trong khoảng ngày đã chọn");
                    int available = rs.getInt("total_rooms") - rs.getInt("sold_rooms");
                    if (available < entry.getValue())
                        throw new IllegalStateException("Không còn đủ phòng trống; vui lòng tải lại danh sách phòng");
                }
            }
        }
    }

    @Override
    public Reservation findById(long reservationId) {
        String sql = BASE_SELECT + "WHERE r.reservation_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Reservation findByCode(String bookingCode) {
        String sql = BASE_SELECT + "WHERE r.booking_code = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, bookingCode);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Reservation> findByCustomer(long customerId) {
        String sql = BASE_SELECT + "WHERE r.customer_id = ? ORDER BY r.booked_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Reservation> findByStatus(String statusCode) {
        String sql = BASE_SELECT + "WHERE r.status_code = ? ORDER BY r.check_in_date";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Reservation> searchByCodeOrCustomer(String keyword, String statusCode) {
        String sql = BASE_SELECT.replace("SELECT r.*", "SELECT TOP (100) r.*")
                   + "WHERE (r.booking_code LIKE ? OR c.full_name LIKE ? OR c.phone LIKE ?) "
                   + (statusCode != null ? "AND r.status_code = ? " : "")
                   + "ORDER BY r.booked_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            if (statusCode != null) ps.setString(4, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public int countSoldRooms(long roomTypeId, LocalDate checkIn, LocalDate checkOut, Long excludeReservationId) {
        String sql = "SELECT COALESCE(SUM(rr.quantity), 0) FROM reservation_rooms rr "
                   + "JOIN reservations r ON r.reservation_id = rr.reservation_id "
                   + "WHERE rr.room_type_id = ? AND r.status_code IN ('PENDING','CONFIRMED','CHECKED_IN') "
                   + "AND r.check_in_date < ? AND r.check_out_date > ? "
                   + (excludeReservationId != null ? "AND r.reservation_id <> ? " : "");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (excludeReservationId != null) ps.setLong(4, excludeReservationId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public int cancelExpiredPending(int holdHours) {
        // Đơn PENDING quá hạn giữ chỗ mà chưa nộp đủ cọc → tự hủy để trả tồn phòng
        String sql = "UPDATE reservations SET status_code = 'CANCELLED', "
                   + "cancellation_reason = N'H\u1ebft h\u1ea1n gi\u1eef ch\u1ed7 - qu\u00e1 h\u1ea1n ch\u01b0a \u0111\u1eb7t c\u1ecdc', "
                   + "updated_at = SYSUTCDATETIME() "
                   + "WHERE status_code = 'PENDING' AND deposit_required > 0 "
                   + "AND booked_at < DATEADD(HOUR, -?, SYSUTCDATETIME()) "
                   + "AND (SELECT COALESCE(SUM(p.amount), 0) FROM payments p "
                   + "     WHERE p.reservation_id = reservations.reservation_id "
                   + "     AND p.status_code = 'SUCCESS' AND p.payment_type = 'DEPOSIT') < deposit_required";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, holdHours);
            return ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markNoShow(long reservationId) {
        // Chỉ đánh dấu không đến cho đơn CONFIRMED đã qua ngày nhận phòng
        String sql = "UPDATE reservations SET status_code = 'NO_SHOW', updated_at = SYSUTCDATETIME() "
                   + "WHERE reservation_id = ? AND status_code = 'CONFIRMED' "
                   + "AND check_in_date < CAST(GETDATE() AS date)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            if (ps.executeUpdate() == 0)
                throw new IllegalStateException(
                        "Chỉ đánh dấu KHÔNG ĐẾN cho đơn CONFIRMED đã qua ngày nhận phòng");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateStatus(long reservationId, String statusCode) {
        String sql = "UPDATE reservations SET status_code = ?, updated_at = SYSUTCDATETIME() WHERE reservation_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, statusCode);
            ps.setLong(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void cancel(long reservationId, String reason) {
        String sql = "UPDATE reservations SET status_code = 'CANCELLED', cancellation_reason = ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE reservation_id = ? AND status_code IN ('PENDING','CONFIRMED')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setLong(2, reservationId);
            if (ps.executeUpdate() == 0)
                throw new IllegalStateException("Chỉ hủy được đơn ở trạng thái PENDING/CONFIRMED");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void checkIn(long reservationId, long byUserId) {
        String sql = "UPDATE reservations SET status_code = 'CHECKED_IN', actual_check_in_at = SYSUTCDATETIME(), "
                   + "checked_in_by_user_id = ?, updated_at = SYSUTCDATETIME() "
                   + "WHERE reservation_id = ? AND status_code = 'CONFIRMED'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, byUserId);
            ps.setLong(2, reservationId);
            if (ps.executeUpdate() == 0)
                throw new IllegalStateException("Đơn không ở trạng thái CONFIRMED, không thể check-in");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void checkOut(long reservationId, long byUserId) {
        String sql = "UPDATE reservations SET status_code = 'CHECKED_OUT', actual_check_out_at = SYSUTCDATETIME(), "
                   + "checked_out_by_user_id = ?, updated_at = SYSUTCDATETIME() "
                   + "WHERE reservation_id = ? AND status_code = 'CHECKED_IN'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, byUserId);
            ps.setLong(2, reservationId);
            if (ps.executeUpdate() == 0)
                throw new IllegalStateException("Đơn không ở trạng thái CHECKED_IN, không thể check-out");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateDatesAndRooms(Reservation r, List<ReservationRoom> newRooms) {
        String updRes = "UPDATE reservations SET check_in_date = ?, check_out_date = ?, adult_count = ?, "
                + "child_count = ?, room_subtotal = ?, tax_amount = ?, total_amount = ?, deposit_required = ?, "
                + "updated_at = SYSUTCDATETIME() WHERE reservation_id = ?";
        String delRooms = "DELETE FROM reservation_rooms WHERE reservation_id = ?";
        String insRoom = "INSERT INTO reservation_rooms (reservation_id, room_type_id, quantity, adult_count, "
                + "child_count, nightly_price_snapshot, number_of_nights, line_total, notes) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(updRes)) {
                    ps.setDate(1, Date.valueOf(r.getCheckInDate()));
                    ps.setDate(2, Date.valueOf(r.getCheckOutDate()));
                    ps.setInt(3, r.getAdultCount());
                    ps.setInt(4, r.getChildCount());
                    ps.setBigDecimal(5, r.getRoomSubtotal());
                    ps.setBigDecimal(6, r.getTaxAmount());
                    ps.setBigDecimal(7, r.getTotalAmount());
                    ps.setBigDecimal(8, r.getDepositRequired());
                    ps.setLong(9, r.getReservationId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(delRooms)) {
                    ps.setLong(1, r.getReservationId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(insRoom)) {
                    for (ReservationRoom rr : newRooms) {
                        ps.setLong(1, r.getReservationId());
                        ps.setLong(2, rr.getRoomTypeId());
                        ps.setInt(3, rr.getQuantity());
                        ps.setInt(4, rr.getAdultCount());
                        ps.setInt(5, rr.getChildCount());
                        ps.setBigDecimal(6, rr.getNightlyPriceSnapshot());
                        ps.setInt(7, rr.getNumberOfNights());
                        ps.setBigDecimal(8, rr.getLineTotal());
                        ps.setString(9, rr.getNotes());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateStayTotals(Reservation r, List<ReservationRoom> lines) {
        String updRes = "UPDATE reservations SET check_in_date = ?, check_out_date = ?, room_subtotal = ?, "
                + "tax_amount = ?, total_amount = ?, deposit_required = ?, updated_at = SYSUTCDATETIME() "
                + "WHERE reservation_id = ?";
        String updLine = "UPDATE reservation_rooms SET nightly_price_snapshot = ?, number_of_nights = ?, "
                + "line_total = ? WHERE reservation_room_id = ?";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(updRes)) {
                    ps.setDate(1, Date.valueOf(r.getCheckInDate()));
                    ps.setDate(2, Date.valueOf(r.getCheckOutDate()));
                    ps.setBigDecimal(3, r.getRoomSubtotal());
                    ps.setBigDecimal(4, r.getTaxAmount());
                    ps.setBigDecimal(5, r.getTotalAmount());
                    ps.setBigDecimal(6, r.getDepositRequired());
                    ps.setLong(7, r.getReservationId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(updLine)) {
                    for (ReservationRoom rr : lines) {
                        ps.setBigDecimal(1, rr.getNightlyPriceSnapshot());
                        ps.setInt(2, rr.getNumberOfNights());
                        ps.setBigDecimal(3, rr.getLineTotal());
                        ps.setLong(4, rr.getReservationRoomId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void addServiceTotal(long reservationId, BigDecimal delta) {
        String sql = "UPDATE reservations SET service_total = service_total + ?, total_amount = total_amount + ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE reservation_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, delta);
            ps.setBigDecimal(2, delta);
            ps.setLong(3, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void appendSpecialRequest(long reservationId, String note) {
        String sql = "UPDATE reservations SET special_requests = "
                   + "COALESCE(special_requests + CHAR(13) + CHAR(10), '') + ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE reservation_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, note);
            ps.setLong(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
