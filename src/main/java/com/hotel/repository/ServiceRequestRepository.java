package com.hotel.repository;

import com.hotel.entity.ServiceRequest;
import com.hotel.interfaces.IServiceRequestRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F15 - Yêu cầu dịch vụ, F16 - Điều phối và xử lý yêu cầu. */
public class ServiceRequestRepository extends BaseRepository implements IServiceRequestRepository {
    private static final String JOIN_SQL =
            "SELECT sr.*, hs.service_name, hs.unit_name, r.booking_code, u.full_name AS staff_name "
                    + "FROM service_requests sr "
                    + "JOIN hotel_services hs ON hs.hotel_service_id = sr.hotel_service_id "
                    + "JOIN reservations r ON r.reservation_id = sr.reservation_id "
                    + "LEFT JOIN users u ON u.user_id = sr.assigned_staff_user_id ";

    private ServiceRequest map(ResultSet rs) throws SQLException {
        ServiceRequest s = new ServiceRequest();
        s.setServiceRequestId(rs.getLong("service_request_id"));
        s.setReservationId(rs.getLong("reservation_id"));
        s.setCustomerId(rs.getLong("customer_id"));
        s.setHotelServiceId(rs.getLong("hotel_service_id"));
        s.setAssignedStaffUserId(longOf(rs, "assigned_staff_user_id"));
        s.setQuantity(rs.getBigDecimal("quantity"));
        s.setUnitPriceSnapshot(rs.getBigDecimal("unit_price_snapshot"));
        s.setTotalAmount(rs.getBigDecimal("total_amount"));
        s.setStatusCode(rs.getString("status_code"));
        s.setRequestedAt(tsOf(rs, "requested_at"));
        s.setRequestedForAt(tsOf(rs, "requested_for_at"));
        s.setAssignedAt(tsOf(rs, "assigned_at"));
        s.setStartedAt(tsOf(rs, "started_at"));
        s.setCompletedAt(tsOf(rs, "completed_at"));
        s.setNotes(rs.getString("notes"));
        s.setServiceName(rs.getString("service_name"));
        s.setUnitName(rs.getString("unit_name"));
        s.setBookingCode(rs.getString("booking_code"));
        s.setStaffName(rs.getString("staff_name"));
        return s;
    }

    @Override
    public long insert(ServiceRequest sr) {
        String sql = "INSERT INTO service_requests (reservation_id, customer_id, hotel_service_id, quantity, "
                + "unit_price_snapshot, total_amount, status_code, requested_for_at, notes) "
                + "VALUES (?,?,?,?,?,?,'PENDING',?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, sr.getReservationId());
            ps.setLong(2, sr.getCustomerId());
            ps.setLong(3, sr.getHotelServiceId());
            ps.setBigDecimal(4, sr.getQuantity());
            ps.setBigDecimal(5, sr.getUnitPriceSnapshot());
            ps.setBigDecimal(6, sr.getTotalAmount());
            bindTs(ps, 7, sr.getRequestedForAt());
            ps.setString(8, sr.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Không lấy được mã yêu cầu dịch vụ");
                return rs.getLong(1);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public ServiceRequest findById(long serviceRequestId) {
        String sql = JOIN_SQL + "WHERE sr.service_request_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<ServiceRequest> findByReservation(long reservationId) {
        return queryList(JOIN_SQL + "WHERE sr.reservation_id = ? ORDER BY sr.requested_at DESC",
                ps -> ps.setLong(1, reservationId));
    }

    @Override
    public List<ServiceRequest> findWorkQueue(String statusCode) {
        String sql = JOIN_SQL
                + (statusCode == null || statusCode.isBlank() ? "" : "WHERE sr.status_code = ? ")
                + "ORDER BY CASE sr.status_code WHEN 'PENDING' THEN 0 WHEN 'ASSIGNED' THEN 1 "
                + "WHEN 'IN_PROGRESS' THEN 2 ELSE 3 END, sr.requested_for_at, sr.requested_at";
        return queryList(sql, ps -> {
            if (statusCode != null && !statusCode.isBlank()) ps.setString(1, statusCode);
        });
    }

    @Override
    public List<ServiceRequest> findAssignedToStaff(long staffUserId, String statusCode) {
        String sql = JOIN_SQL + "WHERE sr.assigned_staff_user_id = ? "
                + (statusCode == null || statusCode.isBlank() ? "" : "AND sr.status_code = ? ")
                + "ORDER BY sr.requested_for_at, sr.requested_at";
        return queryList(sql, ps -> {
            ps.setLong(1, staffUserId);
            if (statusCode != null && !statusCode.isBlank()) ps.setString(2, statusCode);
        });
    }

    @Override
    public void assign(long serviceRequestId, long staffUserId) {
        String sql = "UPDATE service_requests SET assigned_staff_user_id = ?, status_code = 'ASSIGNED', "
                + "assigned_at = SYSUTCDATETIME(), started_at = NULL "
                + "WHERE service_request_id = ? AND status_code IN ('PENDING','ASSIGNED')";
        updateState(sql, "Yêu cầu không ở trạng thái có thể phân công", staffUserId, serviceRequestId);
    }

    @Override
    public void start(long serviceRequestId, long staffUserId) {
        String sql = "UPDATE service_requests SET status_code = 'IN_PROGRESS', started_at = SYSUTCDATETIME() "
                + "WHERE service_request_id = ? AND assigned_staff_user_id = ? AND status_code = 'ASSIGNED'";
        updateState(sql, "Bạn không được bắt đầu yêu cầu này", serviceRequestId, staffUserId);
    }

    @Override
    public void completeAndAddCharge(long serviceRequestId, long staffUserId) {
        String select = "SELECT sr.reservation_id, sr.total_amount, i.status_code AS invoice_status "
                + "FROM service_requests sr WITH (UPDLOCK, HOLDLOCK) "
                + "LEFT JOIN invoices i ON i.reservation_id=sr.reservation_id "
                + "WHERE sr.service_request_id = ? AND sr.assigned_staff_user_id = ? "
                + "AND sr.status_code IN ('ASSIGNED','IN_PROGRESS')";
        String complete = "UPDATE service_requests SET status_code='COMPLETED', "
                + "started_at=COALESCE(started_at,SYSUTCDATETIME()), completed_at=SYSUTCDATETIME() "
                + "WHERE service_request_id=?";
        String charge = "UPDATE reservations SET service_total=service_total+?, total_amount=total_amount+?, "
                + "updated_at=SYSUTCDATETIME() WHERE reservation_id=?";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                long reservationId;
                java.math.BigDecimal amount;
                try (PreparedStatement ps = cn.prepareStatement(select)) {
                    ps.setLong(1, serviceRequestId);
                    ps.setLong(2, staffUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalStateException("Bạn không được hoàn tất yêu cầu này");
                        reservationId = rs.getLong("reservation_id");
                        amount = rs.getBigDecimal("total_amount");
                        String invoiceStatus = rs.getString("invoice_status");
                        if (invoiceStatus != null && !"DRAFT".equals(invoiceStatus)) {
                            throw new IllegalStateException(
                                    "Hóa đơn cuối đã phát hành; không thể hoàn tất thêm dịch vụ");
                        }
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(complete)) {
                    ps.setLong(1, serviceRequestId);
                    ps.executeUpdate();
                }
                if (amount != null && amount.signum() > 0) {
                    try (PreparedStatement ps = cn.prepareStatement(charge)) {
                        ps.setBigDecimal(1, amount);
                        ps.setBigDecimal(2, amount);
                        ps.setLong(3, reservationId);
                        if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy đơn lưu trú để cộng phí");
                    }
                }
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void reportUnable(long serviceRequestId, long staffUserId, String reason) {
        String sql = "UPDATE service_requests SET status_code='PENDING', assigned_staff_user_id=NULL, "
                + "assigned_at=NULL, started_at=NULL, notes=COALESCE(notes + CHAR(13)+CHAR(10), '') + ? "
                + "WHERE service_request_id=? AND assigned_staff_user_id=? "
                + "AND status_code IN ('ASSIGNED','IN_PROGRESS')";
        updateState(sql, "Bạn không được báo không thể thực hiện yêu cầu này",
                "[Không thể thực hiện] " + reason, serviceRequestId, staffUserId);
    }

    @Override
    public void reschedule(long serviceRequestId, java.time.LocalDateTime requestedForAt, String note) {
        String sql = "UPDATE service_requests SET requested_for_at=?, "
                + "notes=COALESCE(notes + CHAR(13)+CHAR(10), '') + ? "
                + "WHERE service_request_id=? AND status_code='PENDING'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            bindTs(ps, 1, requestedForAt);
            ps.setString(2, note == null || note.isBlank()
                    ? "[Lễ tân đổi thời gian phục vụ]" : "[Lễ tân đổi lịch] " + note.trim());
            ps.setLong(3, serviceRequestId);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Chỉ có thể đổi lịch cho yêu cầu đang chờ phân công");
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void cancel(long serviceRequestId, String note) {
        String sql = "UPDATE service_requests SET status_code='CANCELLED', "
                + "notes=COALESCE(notes + CHAR(13)+CHAR(10), '') + ? "
                + "WHERE service_request_id=? AND status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')";
        updateState(sql, "Yêu cầu không thể hủy từ trạng thái hiện tại",
                note == null || note.isBlank() ? "[Đã hủy bởi lễ tân]" : "[Đã hủy] " + note.trim(),
                serviceRequestId);
    }

    @Override
    public List<ServiceRequest> findCompletedNotInvoiced(long reservationId) {
        String sql = JOIN_SQL + "WHERE sr.reservation_id=? AND sr.status_code='COMPLETED' "
                + "AND NOT EXISTS (SELECT 1 FROM invoice_items ii "
                + "WHERE ii.service_request_id=sr.service_request_id)";
        return queryList(sql, ps -> ps.setLong(1, reservationId));
    }

    private void updateState(String sql, String error, Object... params) {
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value instanceof Long v) ps.setLong(i + 1, v);
                else ps.setString(i + 1, String.valueOf(value));
            }
            if (ps.executeUpdate() == 0) throw new IllegalStateException(error);
        } catch (SQLException e) { throw wrap(e); }
    }

    private List<ServiceRequest> queryList(String sql, SqlBinder binder) {
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @FunctionalInterface
    private interface SqlBinder { void bind(PreparedStatement ps) throws SQLException; }
}
