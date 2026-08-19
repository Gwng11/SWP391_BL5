package com.hotel.repository;

import com.hotel.entity.HotelProfile;
import com.hotel.interfaces.IHotelProfileRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** F01 - Thông tin khách sạn */
public class HotelProfileRepository extends BaseRepository implements IHotelProfileRepository {

    @Override
    public HotelProfile getProfile() {
        String sql = "SELECT * FROM hotel_profile WHERE profile_id = 1";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                HotelProfile p = new HotelProfile();
                p.setProfileId(rs.getInt("profile_id"));
                p.setHotelName(rs.getString("hotel_name"));
                p.setDescription(rs.getString("description"));
                p.setAddress(rs.getString("address"));
                p.setPhone(rs.getString("phone"));
                p.setEmail(rs.getString("email"));
                p.setCheckInTime(timeOf(rs, "check_in_time"));
                p.setCheckOutTime(timeOf(rs, "check_out_time"));
                p.setCurrencyCode(rs.getString("currency_code"));
                p.setLogoUrl(rs.getString("logo_url"));
                p.setCoverImageUrl(rs.getString("cover_image_url"));
                p.setUpdatedAt(tsOf(rs, "updated_at"));
                return p;
            }
            return null;
        } catch (SQLException e) { throw wrap(e); }
    }
}
