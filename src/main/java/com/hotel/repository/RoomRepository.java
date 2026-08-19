package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.interfaces.IRoomRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        return r;
    }

    @Override
    public int countSellableByType(long roomTypeId) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_type_id = ? AND is_active = 1 "
                   + "AND operational_status <> 'OUT_OF_SERVICE'";
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
        String sql = "SELECT * FROM rooms r WHERE r.room_type_id = ? AND r.is_active = 1 "
                   + "AND r.operational_status = 'AVAILABLE' "
                   + "AND r.cleaning_status IN ('CLEAN','INSPECTED') "
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
    public List<Room> findAllAssignableRooms() {
        String sql = "SELECT r.*, rt.type_name FROM rooms r "
                   + "JOIN room_types rt ON rt.room_type_id = r.room_type_id "
                   + "WHERE r.is_active = 1 AND rt.is_active = 1 "
                   + "AND r.operational_status = 'AVAILABLE' "
                   + "AND r.cleaning_status IN ('CLEAN','INSPECTED') "
                   + "AND NOT EXISTS (SELECT 1 FROM room_assignments ra "
                   + "WHERE ra.room_id = r.room_id AND ra.is_current = 1) "
                   + "ORDER BY rt.base_price, r.room_number";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Room> list = new ArrayList<>();
            while (rs.next()) {
                Room room = map(rs);
                room.setTypeName(rs.getString("type_name"));
                list.add(room);
            }
            return list;
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Room findById(long roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
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
