package com.hotel.repository;

import com.hotel.entity.HotelService;
import com.hotel.interfaces.IHotelServiceRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F15 - Quản lý & Danh mục dịch vụ */
public class HotelServiceRepository extends BaseRepository implements IHotelServiceRepository {

    private HotelService map(ResultSet rs) throws SQLException {
        HotelService s = new HotelService();
        s.setHotelServiceId(rs.getLong("hotel_service_id"));
        s.setServiceCode(rs.getString("service_code"));
        s.setServiceName(rs.getString("service_name"));
        s.setDescription(rs.getString("description"));
        s.setUnitName(rs.getString("unit_name"));
        s.setUnitPrice(rs.getBigDecimal("unit_price"));
        s.setImageUrl(rs.getString("image_url")); // <-- THÊM MỚI: Map dữ liệu image_url
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
    public List<HotelService> searchActive(String keyword) {
        String sql = "SELECT * FROM hotel_services WHERE is_active = 1 AND service_name LIKE ? ORDER BY service_name";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + (keyword == null ? "" : keyword.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<HotelService> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<HotelService> findAll() {
        String sql = "SELECT * FROM hotel_services ORDER BY service_name";
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

    @Override
    public long insert(HotelService s) {
        String sql = "INSERT INTO hotel_services (service_code, service_name, description, unit_name, unit_price, image_url, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getServiceCode());
            ps.setString(2, s.getServiceName());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getUnitName());
            ps.setBigDecimal(5, s.getUnitPrice());
            ps.setString(6, s.getImageUrl());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void update(HotelService s) {
        String sql = "UPDATE hotel_services SET service_code = ?, service_name = ?, description = ?, "
                + "unit_name = ?, unit_price = ?, image_url = ?, updated_at = SYSUTCDATETIME() "
                + "WHERE hotel_service_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, s.getServiceCode());
            ps.setString(2, s.getServiceName());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getUnitName());
            ps.setBigDecimal(5, s.getUnitPrice());
            ps.setString(6, s.getImageUrl());
            ps.setLong(7, s.getHotelServiceId());
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void toggleActive(long hotelServiceId, boolean active) {
        String sql = "UPDATE hotel_services SET is_active = ?, updated_at = SYSUTCDATETIME() WHERE hotel_service_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, hotelServiceId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}