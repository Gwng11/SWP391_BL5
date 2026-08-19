package com.hotel.repository;

import com.hotel.entity.RoomType;
import com.hotel.interfaces.IRoomTypeRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F02, F03 - Loại phòng */
public class RoomTypeRepository extends BaseRepository implements IRoomTypeRepository {

    private RoomType map(ResultSet rs) throws SQLException {
        RoomType t = new RoomType();
        t.setRoomTypeId(rs.getLong("room_type_id"));
        t.setTypeCode(rs.getString("type_code"));
        t.setTypeName(rs.getString("type_name"));
        t.setDescription(rs.getString("description"));
        t.setMaxAdults(rs.getInt("max_adults"));
        t.setMaxChildren(rs.getInt("max_children"));
        t.setBedType(rs.getString("bed_type"));
        t.setRoomSizeM2(rs.getBigDecimal("room_size_m2"));
        t.setBasePrice(rs.getBigDecimal("base_price"));
        t.setAmenitiesJson(rs.getString("amenities_json"));
        t.setImagesJson(rs.getString("images_json"));
        t.setActive(rs.getBoolean("is_active"));
        t.setCreatedAt(tsOf(rs, "created_at"));
        t.setUpdatedAt(tsOf(rs, "updated_at"));
        return t;
    }

    @Override
    public List<RoomType> findAllActive() {
        String sql = "SELECT * FROM room_types WHERE is_active = 1 ORDER BY base_price";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RoomType> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<RoomType> findAll() {
        String sql = "SELECT * FROM room_types ORDER BY type_name";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RoomType> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public RoomType findById(long roomTypeId) {
        String sql = "SELECT * FROM room_types WHERE room_type_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(RoomType t) {
        String sql = "INSERT INTO room_types (type_code,type_name,description,max_adults,max_children,bed_type,"
                   + "room_size_m2,base_price,amenities_json,images_json,is_active) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRoomType(ps, t, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void update(RoomType t) {
        String sql = "UPDATE room_types SET type_code=?,type_name=?,description=?,max_adults=?,max_children=?,"
                   + "bed_type=?,room_size_m2=?,base_price=?,amenities_json=?,images_json=?,is_active=?,"
                   + "updated_at=SYSUTCDATETIME() WHERE room_type_id=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            bindRoomType(ps, t, true);
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Loại phòng không tồn tại");
        } catch (SQLException e) { throw wrap(e); }
    }

    private void bindRoomType(PreparedStatement ps, RoomType t, boolean includeId) throws SQLException {
        ps.setString(1, t.getTypeCode());
        ps.setString(2, t.getTypeName());
        ps.setString(3, t.getDescription());
        ps.setInt(4, t.getMaxAdults());
        ps.setInt(5, t.getMaxChildren());
        ps.setString(6, t.getBedType());
        ps.setBigDecimal(7, t.getRoomSizeM2());
        ps.setBigDecimal(8, t.getBasePrice());
        ps.setString(9, t.getAmenitiesJson());
        ps.setString(10, t.getImagesJson());
        ps.setBoolean(11, t.isActive());
        if (includeId) ps.setLong(12, t.getRoomTypeId());
    }

    @Override
    public void setActive(long roomTypeId, boolean active) {
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE room_types SET is_active=?,updated_at=SYSUTCDATETIME() WHERE room_type_id=?")) {
                    ps.setBoolean(1, active);
                    ps.setLong(2, roomTypeId);
                    if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Loại phòng không tồn tại");
                }
                String roomSql;
                if (active) {
                    roomSql = "UPDATE rooms SET operational_status=CASE "
                            + "WHEN operational_status='OCCUPIED' THEN 'OCCUPIED' "
                            + "WHEN cleaning_status IN ('READY','INSPECTED') THEN 'AVAILABLE' ELSE 'BLOCKED' END, "
                            + "is_active=1,updated_at=SYSUTCDATETIME() WHERE room_type_id=? AND is_active=0 "
                            + "AND operational_status IN ('OUT_OF_SERVICE','OCCUPIED')";
                } else {
                    roomSql = "UPDATE rooms SET operational_status=CASE WHEN operational_status='OCCUPIED' "
                            + "THEN 'OCCUPIED' ELSE 'OUT_OF_SERVICE' END,is_active=0,"
                            + "updated_at=SYSUTCDATETIME() WHERE room_type_id=?";
                }
                try (PreparedStatement ps = cn.prepareStatement(roomSql)) {
                    ps.setLong(1, roomTypeId);
                    ps.executeUpdate();
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
    public void updateAmenities(long roomTypeId, String amenitiesJson) {
        updateColumn(roomTypeId, "amenities_json", amenitiesJson);
    }

    @Override
    public void updateImages(long roomTypeId, String imagesJson) {
        updateColumn(roomTypeId, "images_json", imagesJson);
    }

    @Override
    public void updateBasePrice(long roomTypeId, java.math.BigDecimal basePrice) {
        updateColumn(roomTypeId, "base_price", basePrice);
    }

    private void updateColumn(long roomTypeId, String column, Object value) {
        // Column is selected only by the repository methods above, never from request data.
        String sql = "UPDATE room_types SET " + column + " = ?, updated_at = SYSUTCDATETIME() WHERE room_type_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, value);
            ps.setLong(2, roomTypeId);
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Loại phòng không tồn tại");
        } catch (SQLException e) { throw wrap(e); }
    }
}
