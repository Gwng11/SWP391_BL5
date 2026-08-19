package com.hotel.repository;

import com.hotel.entity.RoomRate;
import com.hotel.interfaces.IRoomRateRepository;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** F02 - giá theo ngày + stop sell */
public class RoomRateRepository extends BaseRepository implements IRoomRateRepository {

    @Override
    public Map<LocalDate, RoomRate> findRates(long roomTypeId, LocalDate from, LocalDate toExclusive) {
        String sql = "SELECT * FROM room_rates WHERE room_type_id = ? AND rate_date >= ? AND rate_date < ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(toExclusive));
            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDate, RoomRate> map = new HashMap<>();
                while (rs.next()) {
                    RoomRate r = new RoomRate();
                    r.setRoomRateId(rs.getLong("room_rate_id"));
                    r.setRoomTypeId(rs.getLong("room_type_id"));
                    r.setRateDate(dateOf(rs, "rate_date"));
                    r.setNightlyPrice(rs.getBigDecimal("nightly_price"));
                    r.setStopSell(rs.getBoolean("stop_sell"));
                    map.put(r.getRateDate(), r);
                }
                return map;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public boolean hasStopSell(long roomTypeId, LocalDate from, LocalDate toExclusive) {
        String sql = "SELECT COUNT(*) FROM room_rates WHERE room_type_id = ? "
                   + "AND rate_date >= ? AND rate_date < ? AND stop_sell = 1";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(toExclusive));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { throw wrap(e); }
    }
}
