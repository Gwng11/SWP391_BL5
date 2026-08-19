package com.hotel.repository;

import com.hotel.entity.RoomAssignment;
import com.hotel.interfaces.IRoomAssignmentRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** F11 - Gán / đổi phòng, F13 - trả phòng */
public class RoomAssignmentRepository extends BaseRepository implements IRoomAssignmentRepository {

    private RoomAssignment map(ResultSet rs) throws SQLException {
        RoomAssignment a = new RoomAssignment();
        a.setRoomAssignmentId(rs.getLong("room_assignment_id"));
        a.setReservationRoomId(rs.getLong("reservation_room_id"));
        a.setRoomId(rs.getLong("room_id"));
        a.setAssignedByUserId(longOf(rs, "assigned_by_user_id"));
        a.setAssignedAt(tsOf(rs, "assigned_at"));
        a.setUnassignedAt(tsOf(rs, "unassigned_at"));
        a.setUnassignedReason(rs.getString("unassigned_reason"));
        a.setCurrent(rs.getBoolean("is_current"));
        a.setRoomNumber(rs.getString("room_number"));
        a.setTypeName(rs.getString("type_name"));
        return a;
    }

    private static final String JOIN_SQL =
            "SELECT ra.*, rm.room_number, rt.type_name FROM room_assignments ra "
          + "JOIN rooms rm ON rm.room_id = ra.room_id "
          + "JOIN reservation_rooms rr ON rr.reservation_room_id = ra.reservation_room_id "
          + "JOIN room_types rt ON rt.room_type_id = rr.room_type_id ";

    @Override
    public List<RoomAssignment> findCurrentByReservation(long reservationId) {
        String sql = JOIN_SQL + "WHERE rr.reservation_id = ? AND ra.is_current = 1";
        return query(sql, reservationId);
    }

    @Override
    public List<RoomAssignment> historyByReservation(long reservationId) {
        String sql = JOIN_SQL + "WHERE rr.reservation_id = ? ORDER BY ra.assigned_at";
        return query(sql, reservationId);
    }

    private List<RoomAssignment> query(String sql, long reservationId) {
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<RoomAssignment> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public int countCurrentByReservationRoom(long reservationRoomId) {
        String sql = "SELECT COUNT(*) FROM room_assignments WHERE reservation_room_id = ? AND is_current = 1";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationRoomId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void assign(long reservationRoomId, long roomId, long byUserId) {
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                insertAssignment(cn, reservationRoomId, roomId, byUserId);
                setRoomStatus(cn, roomId, "OCCUPIED", null);
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
    public void changeRoom(long roomAssignmentId, long newRoomId, long byUserId, String reason) {
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                long oldRoomId;
                long reservationRoomId;
                String q = "SELECT room_id, reservation_room_id FROM room_assignments "
                         + "WHERE room_assignment_id = ? AND is_current = 1";
                try (PreparedStatement ps = cn.prepareStatement(q)) {
                    ps.setLong(1, roomAssignmentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalStateException("Assignment không tồn tại hoặc đã bị hủy");
                        oldRoomId = rs.getLong(1);
                        reservationRoomId = rs.getLong(2);
                    }
                }
                String upd = "UPDATE room_assignments SET is_current = 0, unassigned_at = SYSUTCDATETIME(), "
                           + "unassigned_reason = ? WHERE room_assignment_id = ?";
                try (PreparedStatement ps = cn.prepareStatement(upd)) {
                    ps.setString(1, reason);
                    ps.setLong(2, roomAssignmentId);
                    ps.executeUpdate();
                }
                setRoomStatus(cn, oldRoomId, "AVAILABLE", "DIRTY");
                insertAssignment(cn, reservationRoomId, newRoomId, byUserId);
                setRoomStatus(cn, newRoomId, "OCCUPIED", null);
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
    public void releaseAllForReservation(long reservationId, String reason) {
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                List<long[]> currents = new ArrayList<>(); // [assignmentId, roomId]
                String q = "SELECT ra.room_assignment_id, ra.room_id FROM room_assignments ra "
                         + "JOIN reservation_rooms rr ON rr.reservation_room_id = ra.reservation_room_id "
                         + "WHERE rr.reservation_id = ? AND ra.is_current = 1";
                try (PreparedStatement ps = cn.prepareStatement(q)) {
                    ps.setLong(1, reservationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) currents.add(new long[]{rs.getLong(1), rs.getLong(2)});
                    }
                }
                String upd = "UPDATE room_assignments SET is_current = 0, unassigned_at = SYSUTCDATETIME(), "
                           + "unassigned_reason = ? WHERE room_assignment_id = ?";
                for (long[] cur : currents) {
                    try (PreparedStatement ps = cn.prepareStatement(upd)) {
                        ps.setString(1, reason);
                        ps.setLong(2, cur[0]);
                        ps.executeUpdate();
                    }
                    setRoomStatus(cn, cur[1], "AVAILABLE", "DIRTY");
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

    private void insertAssignment(Connection cn, long reservationRoomId, long roomId, long byUserId) throws SQLException {
        String ins = "INSERT INTO room_assignments (reservation_room_id, room_id, assigned_by_user_id) VALUES (?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(ins)) {
            ps.setLong(1, reservationRoomId);
            ps.setLong(2, roomId);
            ps.setLong(3, byUserId);
            ps.executeUpdate();
        }
    }

    private void setRoomStatus(Connection cn, long roomId, String opStatus, String cleanStatus) throws SQLException {
        String sql = cleanStatus == null
                ? "UPDATE rooms SET operational_status = ?, updated_at = SYSUTCDATETIME() WHERE room_id = ?"
                : "UPDATE rooms SET operational_status = ?, cleaning_status = ?, updated_at = SYSUTCDATETIME() WHERE room_id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, opStatus);
            if (cleanStatus == null) ps.setLong(2, roomId);
            else { ps.setString(2, cleanStatus); ps.setLong(3, roomId); }
            ps.executeUpdate();
        }
    }
}
