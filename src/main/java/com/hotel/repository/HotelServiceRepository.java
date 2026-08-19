package com.hotel.repository;

import com.hotel.entity.HotelService;
import com.hotel.interfaces.IHotelServiceRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** F15 - Danh mục dịch vụ */
public class HotelServiceRepository extends BaseRepository implements IHotelServiceRepository {

    private HotelService map(ResultSet rs) throws SQLException {
        HotelService s = new HotelService();
        s.setHotelServiceId(rs.getLong("hotel_service_id"));
        s.setServiceCode(rs.getString("service_code"));
        s.setServiceName(rs.getString("service_name"));
        s.setDescription(rs.getString("description"));
        s.setUnitName(rs.getString("unit_name"));
        s.setUnitPrice(rs.getBigDecimal("unit_price"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(tsOf(rs, "created_at"));
        s.setUpdatedAt(tsOf(rs, "updated_at"));
        return s;
    }

    @Override
    public List<HotelService> findAllActive() {
        String sql = "SELECT * FROM hotel_services WHERE is_active = 1 ORDER BY service_name";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<HotelService> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public HotelService findById(long hotelServiceId) {
        String sql = "SELECT * FROM hotel_services WHERE hotel_service_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, hotelServiceId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }
}
