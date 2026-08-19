package com.hotel.repository;

import com.hotel.entity.Invoice;
import com.hotel.interfaces.IInvoiceRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** F14 - Hóa đơn */
public class InvoiceRepository extends BaseRepository implements IInvoiceRepository {

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setInvoiceId(rs.getLong("invoice_id"));
        i.setReservationId(rs.getLong("reservation_id"));
        i.setCustomerId(rs.getLong("customer_id"));
        i.setIssuedByUserId(longOf(rs, "issued_by_user_id"));
        i.setInvoiceNumber(rs.getString("invoice_number"));
        i.setIssuedAt(tsOf(rs, "issued_at"));
        i.setCurrencyCode(rs.getString("currency_code"));
        i.setSubtotal(rs.getBigDecimal("subtotal"));
        i.setTaxAmount(rs.getBigDecimal("tax_amount"));
        i.setTotalAmount(rs.getBigDecimal("total_amount"));
        i.setPaidAmount(rs.getBigDecimal("paid_amount"));
        i.setStatusCode(rs.getString("status_code"));
        i.setCreatedAt(tsOf(rs, "created_at"));
        i.setUpdatedAt(tsOf(rs, "updated_at"));
        return i;
    }

    @Override
    public Invoice findByReservation(long reservationId) {
        String sql = "SELECT * FROM invoices WHERE reservation_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Invoice findById(long invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(Invoice inv) {
        String sql = "INSERT INTO invoices (reservation_id, customer_id, issued_by_user_id, invoice_number, "
                   + "currency_code, subtotal, tax_amount, total_amount, paid_amount, status_code) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, inv.getReservationId());
            ps.setLong(2, inv.getCustomerId());
            bindLong(ps, 3, inv.getIssuedByUserId());
            ps.setString(4, inv.getInvoiceNumber());
            ps.setString(5, inv.getCurrencyCode() == null ? "VND" : inv.getCurrencyCode());
            ps.setBigDecimal(6, inv.getSubtotal());
            ps.setBigDecimal(7, inv.getTaxAmount());
            ps.setBigDecimal(8, inv.getTotalAmount());
            ps.setBigDecimal(9, inv.getPaidAmount() == null ? BigDecimal.ZERO : inv.getPaidAmount());
            ps.setString(10, inv.getStatusCode() == null ? "DRAFT" : inv.getStatusCode());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateTotals(long invoiceId, BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
        String sql = "UPDATE invoices SET subtotal = ?, tax_amount = ?, total_amount = ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE invoice_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, subtotal);
            ps.setBigDecimal(2, tax);
            ps.setBigDecimal(3, total);
            ps.setLong(4, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void issue(long invoiceId, long byUserId) {
        String sql = "UPDATE invoices SET status_code = 'ISSUED', issued_at = SYSUTCDATETIME(), "
                   + "issued_by_user_id = ?, updated_at = SYSUTCDATETIME() WHERE invoice_id = ? AND status_code = 'DRAFT'";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, byUserId);
            ps.setLong(2, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void applyPayment(long invoiceId, BigDecimal newPaidAmount, String statusCode) {
        String sql = "UPDATE invoices SET paid_amount = ?, status_code = ?, updated_at = SYSUTCDATETIME() WHERE invoice_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newPaidAmount);
            ps.setString(2, statusCode);
            ps.setLong(3, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
