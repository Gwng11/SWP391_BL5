package com.hotel.repository;

import com.hotel.entity.ServiceRequest;
import com.hotel.interfaces.IServiceRequestRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** F15 - Yêu cầu dịch vụ, F16 - Xử lý yêu cầu */
public class ServiceRequestRepository extends BaseRepository implements IServiceRequestRepository {

    private static final String JOIN_SQL =
            "SELECT sr.*, hs.service_name, hs.unit_name, r.booking_code, u.full_name AS staff_name "
                    + "FROM service_requests sr "
                    + "JOIN hotel_services hs ON hs.hotel_service_id = sr.hotel_service_id "
                    + "JOIN reservations r ON r.reservation_id = sr.reservation_id "
                    + "LEFT JOIN users u ON u.user_id = sr.assigned_staff_user_id ";

    private ServiceRequest map(ResultSet rs) throws SQLException {
        ServiceRequest s = new ServiceRequest();
        // Ánh xạ an toàn hỗ trợ các biến thể tên cột khóa chính & trạng thái trong DB
        s.setServiceRequestId(getLongSafe(rs, "service_request_id", "request_id", "id"));
        s.setReservationId(getLongSafe(rs, "reservation_id"));
        s.setCustomerId(getLongSafe(rs, "customer_id"));
        s.setHotelServiceId(getLongSafe(rs, "hotel_service_id"));
        s.setAssignedStaffUserId(longOfSafe(rs, "assigned_staff_user_id", "staff_user_id"));
        s.setQuantity(getBigDecimalSafe(rs, "quantity"));
        s.setUnitPriceSnapshot(getBigDecimalSafe(rs, "unit_price_snapshot", "unit_price"));
        s.setTotalAmount(getBigDecimalSafe(rs, "total_amount"));
        s.setStatusCode(getStringSafe(rs, "status_code", "status"));
        s.setRequestedAt(tsOfSafe(rs, "requested_at", "created_at"));
        s.setScheduledAt(tsOfSafe(rs, "scheduled_at"));
        s.setAssignedAt(tsOfSafe(rs, "assigned_at"));
        s.setStartedAt(tsOfSafe(rs, "started_at"));
        s.setCompletedAt(tsOfSafe(rs, "completed_at"));
        s.setNotes(getStringSafe(rs, "notes"));
        s.setServiceName(getStringSafe(rs, "service_name"));
        s.setUnitName(getStringSafe(rs, "unit_name"));
        s.setBookingCode(getStringSafe(rs, "booking_code"));
        s.setStaffName(getStringSafe(rs, "staff_name"));
        return s;
    }

    @Override
    public long insert(ServiceRequest sr) {
        String sql = "INSERT INTO service_requests (reservation_id, customer_id, hotel_service_id, quantity, "
                + "unit_price_snapshot, total_amount, status_code, scheduled_at, notes) "
                + "VALUES (?,?,?,?,?,?,'PENDING',?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, sr.getReservationId());
            ps.setLong(2, sr.getCustomerId());
            ps.setLong(3, sr.getHotelServiceId());
            ps.setBigDecimal(4, sr.getQuantity());
            ps.setBigDecimal(5, sr.getUnitPriceSnapshot());
            ps.setBigDecimal(6, sr.getTotalAmount());
            bindTs(ps, 7, sr.getScheduledAt());
            ps.setString(8, sr.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public ServiceRequest findById(long serviceRequestId) {
        String sql = JOIN_SQL + "WHERE sr.service_request_id = ? OR sr.request_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            ps.setLong(2, serviceRequestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public List<ServiceRequest> findByReservation(long reservationId) {
        String sql = JOIN_SQL + "WHERE sr.reservation_id = ? ORDER BY sr.requested_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public List<ServiceRequest> findWorkQueue(String statusCode) {
        String sql = JOIN_SQL
                + (statusCode == null || statusCode.isBlank() ? "WHERE sr.status_code <> 'CANCELLED' " : "WHERE sr.status_code = ? ")
                + "ORDER BY sr.requested_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (statusCode != null && !statusCode.isBlank()) {
                ps.setString(1, statusCode);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public void assign(long serviceRequestId, long staffUserId) {
        String sql = "UPDATE service_requests SET assigned_staff_user_id = ?, status_code = 'ASSIGNED', "
                + "assigned_at = SYSUTCDATETIME() WHERE (service_request_id = ? OR request_id = ?) AND status_code IN ('PENDING','ASSIGNED')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, staffUserId);
            ps.setLong(2, serviceRequestId);
            ps.setLong(3, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không ở trạng thái có thể phân công");
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public void start(long serviceRequestId) {
        String sql = "UPDATE service_requests SET status_code = 'IN_PROGRESS', started_at = SYSUTCDATETIME() "
                + "WHERE (service_request_id = ? OR request_id = ?) AND status_code IN ('ASSIGNED', 'PENDING')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            ps.setLong(2, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không thể bắt đầu thực hiện");
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public void complete(long serviceRequestId) {
        String sql = "UPDATE service_requests SET status_code = 'COMPLETED', completed_at = SYSUTCDATETIME(), "
                + "started_at = COALESCE(started_at, SYSUTCDATETIME()) "
                + "WHERE (service_request_id = ? OR request_id = ?) AND status_code IN ('ASSIGNED','IN_PROGRESS')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            ps.setLong(2, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không thể hoàn tất từ trạng thái hiện tại");
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public void cancel(long serviceRequestId, String note) {
        String sql = "UPDATE service_requests SET status_code = 'CANCELLED', "
                + "notes = COALESCE(notes + ' | ', '') + ? "
                + "WHERE (service_request_id = ? OR request_id = ?) AND status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, note == null ? "Cancelled" : note);
            ps.setLong(2, serviceRequestId);
            ps.setLong(3, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không thể hủy từ trạng thái hiện tại");
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public List<ServiceRequest> findCompletedNotInvoiced(long reservationId) {
        String sql = JOIN_SQL + "WHERE sr.reservation_id = ? AND sr.status_code = 'COMPLETED' "
                + "AND NOT EXISTS (SELECT 1 FROM invoice_items ii WHERE ii.service_request_id = sr.service_request_id OR ii.service_request_id = sr.request_id)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    // =========================================================================
    // HÀM BỔ TRỢ ĐỌC CỘT AN TOÀN - CHỐNG LỖI "COLUMN NAME NOT VALID"
    // =========================================================================

    private long getLongSafe(ResultSet rs, String... cols) {
        for (String col : cols) {
            try { return rs.getLong(col); } catch (SQLException ignored) {}
        }
        return 0L;
    }

    private Long longOfSafe(ResultSet rs, String... cols) {
        for (String col : cols) {
            try {
                long val = rs.getLong(col);
                return rs.wasNull() ? null : val;
            } catch (SQLException ignored) {}
        }
        return null;
    }

    private String getStringSafe(ResultSet rs, String... cols) {
        for (String col : cols) {
            try { return rs.getString(col); } catch (SQLException ignored) {}
        }
        return null;
    }

    private BigDecimal getBigDecimalSafe(ResultSet rs, String... cols) {
        for (String col : cols) {
            try { return rs.getBigDecimal(col); } catch (SQLException ignored) {}
        }
        return null;
    }

    private LocalDateTime tsOfSafe(ResultSet rs, String... cols) {
        for (String col : cols) {
            try {
                Timestamp ts = rs.getTimestamp(col);
                return ts != null ? ts.toLocalDateTime() : null;
            } catch (SQLException ignored) {}
        }
        return null;
    }
}