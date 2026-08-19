package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.interfaces.IRoomRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F02 (đếm tồn phòng), F11 (gán phòng), F13 (đổi trạng thái dọn) */
public class RoomRepository extends BaseRepository implements IRoomRepository {

    private Room map(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getLong("room_id"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setFloorNumber(intOf(rs, "floor_number"));
        r.setOperationalStatus(rs.getString("operational_status"));
        r.setCleaningStatus(rs.getString("cleaning_status"));
        r.setNotes(rs.getString("notes"));
        r.setActive(rs.getBoolean("is_active"));
        r.setCreatedAt(tsOf(rs, "created_at"));
        r.setUpdatedAt(tsOf(rs, "updated_at"));
        try { r.setTypeName(rs.getString("type_name")); } catch (SQLException ignored) { /* optional join column */ }
        return r;
    }

    @Override
    public int countSellableByType(long roomTypeId) {
        String sql = "SELECT COUNT(*) FROM rooms r JOIN room_types rt ON rt.room_type_id=r.room_type_id "
                   + "WHERE r.room_type_id = ? AND r.is_active = 1 AND rt.is_active = 1 "
                   + "AND r.operational_status = 'AVAILABLE' "
                   + "AND NOT EXISTS (SELECT 1 FROM maintenance_tickets mt WHERE mt.room_id=r.room_id "
                   + "AND mt.status_code NOT IN ('CLOSED','CANCELLED'))";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Room> findAssignableRooms(long roomTypeId) {
        String sql = "SELECT r.* FROM rooms r JOIN room_types rt ON rt.room_type_id=r.room_type_id "
                   + "WHERE r.room_type_id = ? AND r.is_active = 1 AND rt.is_active = 1 "
                   + "AND r.operational_status = 'AVAILABLE' "
                   + "AND r.cleaning_status IN ('READY','INSPECTED') "
                   + "AND NOT EXISTS (SELECT 1 FROM maintenance_tickets mt WHERE mt.room_id=r.room_id "
                   + "AND mt.status_code NOT IN ('CLOSED','CANCELLED')) "
                   + "AND NOT EXISTS (SELECT 1 FROM room_assignments ra WHERE ra.room_id = r.room_id AND ra.is_current = 1) "
                   + "ORDER BY r.room_number";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Room> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Room> findAll(Long roomTypeId, Integer floorNumber, String operationalStatus) {
        StringBuilder sql = new StringBuilder("SELECT r.*, rt.type_name FROM rooms r "
                + "JOIN room_types rt ON rt.room_type_id = r.room_type_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (roomTypeId != null) { sql.append("AND r.room_type_id = ? "); params.add(roomTypeId); }
        if (floorNumber != null) { sql.append("AND r.floor_number = ? "); params.add(floorNumber); }
        if (operationalStatus != null && !operationalStatus.isBlank()) {
            sql.append("AND r.operational_status = ? "); params.add(operationalStatus);
        }
        sql.append("ORDER BY r.floor_number, r.room_number");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Room> rooms = new ArrayList<>();
                while (rs.next()) rooms.add(map(rs));
                return rooms;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Room findById(long roomId) {
        String sql = "SELECT r.*, rt.type_name FROM rooms r JOIN room_types rt "
                   + "ON rt.room_type_id = r.room_type_id WHERE r.room_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(Room room) {
        String sql = "INSERT INTO rooms (room_type_id, room_number, floor_number, operational_status, "
                   + "cleaning_status, notes, is_active) VALUES (?,?,?,?,?,?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());
            if (room.getFloorNumber() == null) ps.setNull(3, java.sql.Types.SMALLINT); else ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getOperationalStatus());
            ps.setString(5, room.getCleaningStatus());
            ps.setString(6, room.getNotes());
            ps.setBoolean(7, room.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void update(Room room) {
        String sql = "UPDATE rooms SET room_type_id = ?, room_number = ?, floor_number = ?, notes = ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE room_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());
            if (room.getFloorNumber() == null) ps.setNull(3, java.sql.Types.SMALLINT); else ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getNotes());
            ps.setLong(5, room.getRoomId());
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Phòng không tồn tại");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateOperationalStatus(long roomId, String operationalStatus, boolean active) {
        String sql = "UPDATE rooms SET operational_status = ?, is_active = ?, updated_at = SYSUTCDATETIME() "
                   + "WHERE room_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, operationalStatus);
            ps.setBoolean(2, active);
            ps.setLong(3, roomId);
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Phòng không tồn tại");
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateStatus(long roomId, String operationalStatus, String cleaningStatus) {
        String sql = "UPDATE rooms SET operational_status = ?, cleaning_status = ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE room_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, operationalStatus);
            ps.setString(2, cleaningStatus);
            ps.setLong(3, roomId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
