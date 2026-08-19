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
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public List<RoomRate> findRateList(long roomTypeId, LocalDate from, LocalDate toInclusive) {
        String sql = "SELECT * FROM room_rates WHERE room_type_id = ? AND rate_date >= ? AND rate_date <= ? "
                   + "ORDER BY rate_date";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(toInclusive));
            try (ResultSet rs = ps.executeQuery()) {
                List<RoomRate> list = new ArrayList<>();
                while (rs.next()) {
                    RoomRate r = new RoomRate();
                    r.setRoomRateId(rs.getLong("room_rate_id"));
                    r.setRoomTypeId(rs.getLong("room_type_id"));
                    r.setRateDate(dateOf(rs, "rate_date"));
                    r.setNightlyPrice(rs.getBigDecimal("nightly_price"));
                    r.setStopSell(rs.getBoolean("stop_sell"));
                    list.add(r);
                }
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void upsertRange(long roomTypeId, LocalDate from, LocalDate toInclusive,
                            java.math.BigDecimal nightlyPrice, boolean stopSell) {
        String update = "UPDATE room_rates SET nightly_price=?, stop_sell=?, updated_at=SYSUTCDATETIME() "
                      + "WHERE room_type_id=? AND rate_date=?";
        String insert = "INSERT INTO room_rates (room_type_id,rate_date,nightly_price,stop_sell) VALUES (?,?,?,?)";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                for (LocalDate date = from; !date.isAfter(toInclusive); date = date.plusDays(1)) {
                    int changed;
                    try (PreparedStatement ps = cn.prepareStatement(update)) {
                        ps.setBigDecimal(1, nightlyPrice); ps.setBoolean(2, stopSell);
                        ps.setLong(3, roomTypeId); ps.setDate(4, Date.valueOf(date));
                        changed = ps.executeUpdate();
                    }
                    if (changed == 0) {
                        try (PreparedStatement ps = cn.prepareStatement(insert)) {
                            ps.setLong(1, roomTypeId); ps.setDate(2, Date.valueOf(date));
                            ps.setBigDecimal(3, nightlyPrice); ps.setBoolean(4, stopSell); ps.executeUpdate();
                        }
                    }
                }
                cn.commit();
            } catch (SQLException e) { cn.rollback(); throw e; }
            finally { cn.setAutoCommit(true); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updatePricing(long roomTypeId, java.math.BigDecimal basePrice, LocalDate from,
                              LocalDate toInclusive, java.math.BigDecimal nightlyPrice, boolean stopSell) {
        String baseSql = "UPDATE room_types SET base_price=?,updated_at=SYSUTCDATETIME() WHERE room_type_id=?";
        String update = "UPDATE room_rates SET nightly_price=?,stop_sell=?,updated_at=SYSUTCDATETIME() WHERE room_type_id=? AND rate_date=?";
        String insert = "INSERT INTO room_rates (room_type_id,rate_date,nightly_price,stop_sell) VALUES (?,?,?,?)";
        try (Connection cn=getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps=cn.prepareStatement(baseSql)) { ps.setBigDecimal(1,basePrice);ps.setLong(2,roomTypeId);if(ps.executeUpdate()==0)throw new IllegalArgumentException("Loại phòng không tồn tại"); }
                if (from != null) {
                    for (LocalDate date=from;!date.isAfter(toInclusive);date=date.plusDays(1)) {
                        int changed;
                        try (PreparedStatement ps=cn.prepareStatement(update)) { ps.setBigDecimal(1,nightlyPrice);ps.setBoolean(2,stopSell);ps.setLong(3,roomTypeId);ps.setDate(4,Date.valueOf(date));changed=ps.executeUpdate(); }
                        if(changed==0)try(PreparedStatement ps=cn.prepareStatement(insert)){ps.setLong(1,roomTypeId);ps.setDate(2,Date.valueOf(date));ps.setBigDecimal(3,nightlyPrice);ps.setBoolean(4,stopSell);ps.executeUpdate();}
                    }
                }
                cn.commit();
            } catch(SQLException|RuntimeException e){cn.rollback();throw e;} finally {cn.setAutoCommit(true);}
        } catch(SQLException e){throw wrap(e);}
    }
}
