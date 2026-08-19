package com.hotel.repository;

import com.hotel.entity.Payment;
import com.hotel.interfaces.IPaymentRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F08 - đặt cọc, F14 - thanh toán cuối */
public class PaymentRepository extends BaseRepository implements IPaymentRepository {

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getLong("payment_id"));
        p.setReservationId(rs.getLong("reservation_id"));
        p.setInvoiceId(longOf(rs, "invoice_id"));
        p.setRecordedByUserId(longOf(rs, "recorded_by_user_id"));
        p.setPaymentType(rs.getString("payment_type"));
        p.setMethodCode(rs.getString("method_code"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setCurrencyCode(rs.getString("currency_code"));
        p.setStatusCode(rs.getString("status_code"));
        p.setProviderName(rs.getString("provider_name"));
        p.setProviderReference(rs.getString("provider_reference"));
        p.setFailureReason(rs.getString("failure_reason"));
        p.setPaidAt(tsOf(rs, "paid_at"));
        p.setCreatedAt(tsOf(rs, "created_at"));
        return p;
    }

    @Override
    public long insert(Payment p) {
        String sql = "INSERT INTO payments (reservation_id, invoice_id, recorded_by_user_id, payment_type, "
                   + "method_code, amount, currency_code, status_code, provider_name, provider_reference, paid_at) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?, CASE WHEN ? = 'SUCCESS' THEN SYSUTCDATETIME() END)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getReservationId());
            bindLong(ps, 2, p.getInvoiceId());
            bindLong(ps, 3, p.getRecordedByUserId());
            ps.setString(4, p.getPaymentType());
            ps.setString(5, p.getMethodCode());
            ps.setBigDecimal(6, p.getAmount());
            ps.setString(7, p.getCurrencyCode() == null ? "VND" : p.getCurrencyCode());
            ps.setString(8, p.getStatusCode());
            ps.setString(9, p.getProviderName());
            ps.setString(10, p.getProviderReference());
            ps.setString(11, p.getStatusCode());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Payment findById(long paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Payment> findByReservation(long reservationId) {
        String sql = "SELECT * FROM payments WHERE reservation_id = ? ORDER BY created_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public BigDecimal sumSuccess(long reservationId, String paymentType) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE reservation_id = ? "
                   + "AND status_code = 'SUCCESS'" + (paymentType != null ? " AND payment_type = ?" : "");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            if (paymentType != null) ps.setString(2, paymentType);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markSuccess(long paymentId, String providerReference) {
        String sql = "UPDATE payments SET status_code = 'SUCCESS', paid_at = SYSUTCDATETIME(), "
                   + "provider_reference = COALESCE(?, provider_reference) WHERE payment_id = ? AND status_code = 'PENDING'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, providerReference);
            ps.setLong(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markFailed(long paymentId, String reason) {
        String sql = "UPDATE payments SET status_code = 'FAILED', failure_reason = ? WHERE payment_id = ? AND status_code = 'PENDING'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setLong(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
