package com.hotel.repository;

import com.hotel.entity.ReservationRoom;
import com.hotel.interfaces.IReservationRoomRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationRoomRepository extends BaseRepository implements IReservationRoomRepository {

    private ReservationRoom map(ResultSet rs) throws SQLException {
        ReservationRoom r = new ReservationRoom();
        r.setReservationRoomId(rs.getLong("reservation_room_id"));
        r.setReservationId(rs.getLong("reservation_id"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setQuantity(rs.getInt("quantity"));
        r.setAdultCount(rs.getInt("adult_count"));
        r.setChildCount(rs.getInt("child_count"));
        r.setNightlyPriceSnapshot(rs.getBigDecimal("nightly_price_snapshot"));
        r.setNumberOfNights(rs.getInt("number_of_nights"));
        r.setLineTotal(rs.getBigDecimal("line_total"));
        r.setNotes(rs.getString("notes"));
        r.setTypeName(rs.getString("type_name"));
        return r;
    }

    @Override
    public List<ReservationRoom> findByReservation(long reservationId) {
        String sql = "SELECT rr.*, rt.type_name FROM reservation_rooms rr "
                   + "JOIN room_types rt ON rt.room_type_id = rr.room_type_id WHERE rr.reservation_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ReservationRoom> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public ReservationRoom findById(long reservationRoomId) {
        String sql = "SELECT rr.*, rt.type_name FROM reservation_rooms rr "
                   + "JOIN room_types rt ON rt.room_type_id = rr.room_type_id WHERE rr.reservation_room_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationRoomId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }
}
