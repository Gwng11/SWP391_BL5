package com.hotel.repository;

import com.hotel.entity.InvoiceItem;
import com.hotel.interfaces.IInvoiceItemRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F12 (phụ thu), F14 (chi tiết hóa đơn), F16 (tiền dịch vụ) */
public class InvoiceItemRepository extends BaseRepository implements IInvoiceItemRepository {

    @Override
    public List<InvoiceItem> findByInvoice(long invoiceId) {
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? AND is_voided = 0 ORDER BY posted_at";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                List<InvoiceItem> list = new ArrayList<>();
                while (rs.next()) {
                    InvoiceItem i = new InvoiceItem();
                    i.setInvoiceItemId(rs.getLong("invoice_item_id"));
                    i.setInvoiceId(rs.getLong("invoice_id"));
                    i.setServiceRequestId(longOf(rs, "service_request_id"));
                    i.setPostedByUserId(longOf(rs, "posted_by_user_id"));
                    i.setItemType(rs.getString("item_type"));
                    i.setDescription(rs.getString("description"));
                    i.setQuantity(rs.getBigDecimal("quantity"));
                    i.setUnitPrice(rs.getBigDecimal("unit_price"));
                    i.setAmount(rs.getBigDecimal("amount"));
                    i.setPostedAt(tsOf(rs, "posted_at"));
                    i.setVoided(rs.getBoolean("is_voided"));
                    list.add(i);
                }
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(InvoiceItem item) {
        String sql = "INSERT INTO invoice_items (invoice_id, service_request_id, posted_by_user_id, item_type, "
                   + "description, quantity, unit_price, amount) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getInvoiceId());
            bindLong(ps, 2, item.getServiceRequestId());
            bindLong(ps, 3, item.getPostedByUserId());
            ps.setString(4, item.getItemType());
            ps.setString(5, item.getDescription());
            ps.setBigDecimal(6, item.getQuantity());
            ps.setBigDecimal(7, item.getUnitPrice());
            ps.setBigDecimal(8, item.getAmount());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public boolean existsForServiceRequest(long serviceRequestId) {
        String sql = "SELECT COUNT(*) FROM invoice_items WHERE service_request_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, serviceRequestId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void voidItem(long invoiceItemId) {
        String sql = "UPDATE invoice_items SET is_voided = 1 WHERE invoice_item_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, invoiceItemId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
