package com.hotel.repository;

import com.hotel.entity.ReservationGuest;
import com.hotel.interfaces.IReservationGuestRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationGuestRepository extends BaseRepository implements IReservationGuestRepository {

    @Override
    public List<ReservationGuest> findByReservation(long reservationId) {
        String sql = "SELECT * FROM reservation_guests WHERE reservation_id = ? ORDER BY is_primary_guest DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ReservationGuest> list = new ArrayList<>();
                while (rs.next()) {
                    ReservationGuest g = new ReservationGuest();
                    g.setReservationGuestId(rs.getLong("reservation_guest_id"));
                    g.setReservationId(rs.getLong("reservation_id"));
                    g.setCustomerId(longOf(rs, "customer_id"));
                    g.setFullName(rs.getString("full_name"));
                    g.setDateOfBirth(dateOf(rs, "date_of_birth"));
                    g.setIdDocumentType(rs.getString("id_document_type"));
                    g.setIdDocumentNumber(rs.getString("id_document_number"));
                    g.setNationality(rs.getString("nationality"));
                    g.setPrimaryGuest(rs.getBoolean("is_primary_guest"));
                    list.add(g);
                }
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void replaceGuests(long reservationId, List<ReservationGuest> guests) {
        String del = "DELETE FROM reservation_guests WHERE reservation_id = ?";
        String ins = "INSERT INTO reservation_guests (reservation_id, customer_id, full_name, date_of_birth, "
                   + "id_document_type, id_document_number, nationality, is_primary_guest) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(del)) {
                    ps.setLong(1, reservationId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(ins)) {
                    for (ReservationGuest g : guests) {
                        ps.setLong(1, reservationId);
                        bindLong(ps, 2, g.getCustomerId());
                        ps.setString(3, g.getFullName());
                        bindDate(ps, 4, g.getDateOfBirth());
                        ps.setString(5, g.getIdDocumentType());
                        ps.setString(6, g.getIdDocumentNumber());
                        ps.setString(7, g.getNationality());
                        ps.setBoolean(8, g.isPrimaryGuest());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) { throw wrap(e); }
    }
}
