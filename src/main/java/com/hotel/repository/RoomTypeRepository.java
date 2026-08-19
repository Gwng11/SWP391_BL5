package com.hotel.repository;

import com.hotel.entity.RoomType;
import com.hotel.interfaces.IRoomTypeRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public RoomType findById(long roomTypeId) {
        String sql = "SELECT * FROM room_types WHERE room_type_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }
}
