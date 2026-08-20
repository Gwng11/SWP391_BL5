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

/** F15 - yêu cầu dịch vụ, F16 - xử lý yêu cầu */
public class ServiceRequestRepository extends BaseRepository implements IServiceRequestRepository {

//    private static final String JOIN_SQL =
//            "SELECT sr.*, hs.service_name, hs.unit_name, r.booking_code, u.full_name AS staff_name "
//          + "FROM service_requests sr "
//          + "JOIN hotel_services hs ON hs.hotel_service_id = sr.hotel_service_id "
//          + "JOIN reservations r ON r.reservation_id = sr.reservation_id "
//          + "LEFT JOIN users u ON u.user_id = sr.assigned_staff_user_id ";
private static final String JOIN_SQL =
        "SELECT sr.service_request_id, sr.reservation_id, sr.customer_id, "
                + "sr.hotel_service_id, sr.assigned_staff_user_id, sr.quantity, "
                + "sr.unit_price_snapshot, sr.total_amount, sr.status_code, "
                + "sr.requested_at, sr.scheduled_at, sr.assigned_at, sr.started_at, "
                + "sr.completed_at, sr.notes, "
                + "hs.service_name, hs.unit_name, r.booking_code, u.full_name AS staff_name "
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
        s.setScheduledAt(tsOf(rs, "scheduled_at"));
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
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
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
        String sql = JOIN_SQL + "WHERE sr.reservation_id = ? ORDER BY sr.requested_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<ServiceRequest> findWorkQueue(String statusCode) {
        String sql = JOIN_SQL
                + (statusCode == null ? "WHERE sr.status_code <> 'CANCELLED' " : "WHERE sr.status_code = ? ")
                + "ORDER BY sr.requested_at";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (statusCode != null) ps.setString(1, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void assign(long serviceRequestId, long staffUserId) {
        String sql = "UPDATE service_requests SET assigned_staff_user_id = ?, status_code = 'ASSIGNED', "
                   + "assigned_at = SYSUTCDATETIME() WHERE service_request_id = ? AND status_code IN ('PENDING','ASSIGNED')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, staffUserId);
            ps.setLong(2, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không ở trạng thái có thể phân công");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void start(long serviceRequestId) {
        String sql = "UPDATE service_requests SET status_code = 'IN_PROGRESS', started_at = SYSUTCDATETIME() "
                   + "WHERE service_request_id = ? AND status_code = 'ASSIGNED'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu chưa được phân công");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void complete(long serviceRequestId) {
        String sql = "UPDATE service_requests SET status_code = 'COMPLETED', completed_at = SYSUTCDATETIME(), "
                   + "started_at = COALESCE(started_at, SYSUTCDATETIME()) "
                   + "WHERE service_request_id = ? AND status_code IN ('ASSIGNED','IN_PROGRESS')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không thể hoàn tất từ trạng thái hiện tại");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void cancel(long serviceRequestId, String note) {
        String sql = "UPDATE service_requests SET status_code = 'CANCELLED', "
                   + "notes = COALESCE(notes + ' | ', '') + ? "
                   + "WHERE service_request_id = ? AND status_code IN ('PENDING','ASSIGNED','IN_PROGRESS')";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, note == null ? "Cancelled" : note);
            ps.setLong(2, serviceRequestId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Yêu cầu không thể hủy từ trạng thái hiện tại");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<ServiceRequest> findCompletedNotInvoiced(long reservationId) {
        String sql = JOIN_SQL + "WHERE sr.reservation_id = ? AND sr.status_code = 'COMPLETED' "
                   + "AND NOT EXISTS (SELECT 1 FROM invoice_items ii WHERE ii.service_request_id = sr.service_request_id)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceRequest> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }
}
