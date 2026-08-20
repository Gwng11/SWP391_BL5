package com.hotel.repository;

import com.hotel.entity.Customer;
import com.hotel.interfaces.ICustomerRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** F05 (hồ sơ), F09 (quản lý khách hàng) */
public class CustomerRepository extends BaseRepository implements ICustomerRepository {

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setUserId(longOf(rs, "user_id"));
        c.setCreatedByUserId(longOf(rs, "created_by_user_id"));
        c.setCustomerCode(rs.getString("customer_code"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setDateOfBirth(dateOf(rs, "date_of_birth"));
        c.setIdDocumentType(rs.getString("id_document_type"));
        c.setIdDocumentNumber(rs.getString("id_document_number"));
        c.setNationality(rs.getString("nationality"));
        c.setAddress(rs.getString("address"));
        c.setStatusCode(rs.getString("status_code"));
        c.setCreatedAt(tsOf(rs, "created_at"));
        c.setUpdatedAt(tsOf(rs, "updated_at"));
        return c;
    }

    @Override
    public Customer findById(long customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Customer findByUserId(long userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public Customer findByDocument(String documentType, String documentNumber) {
        String sql = "SELECT * FROM customers WHERE id_document_type = ? AND id_document_number = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documentType);
            ps.setString(2, documentNumber);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<Customer> search(String keyword) {
        String sql = "SELECT TOP 50 * FROM customers WHERE full_name LIKE ? OR email LIKE ? "
                   + "OR phone LIKE ? OR customer_code LIKE ? OR id_document_number LIKE ? "
                   + "ORDER BY created_at DESC";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                List<Customer> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(Customer c) {
        String sql = "INSERT INTO customers (user_id, created_by_user_id, customer_code, full_name, email, phone, "
                   + "date_of_birth, id_document_type, id_document_number, nationality, address, status_code) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindLong(ps, 1, c.getUserId());
            bindLong(ps, 2, c.getCreatedByUserId());
            ps.setString(3, c.getCustomerCode());
            ps.setString(4, c.getFullName());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getPhone());
            bindDate(ps, 7, c.getDateOfBirth());
            ps.setString(8, c.getIdDocumentType());
            ps.setString(9, c.getIdDocumentNumber());
            ps.setString(10, c.getNationality());
            ps.setString(11, c.getAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void update(Customer c) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone = ?, date_of_birth = ?, "
                   + "id_document_type = ?, id_document_number = ?, nationality = ?, address = ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE customer_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            bindDate(ps, 4, c.getDateOfBirth());
            ps.setString(5, c.getIdDocumentType());
            ps.setString(6, c.getIdDocumentNumber());
            ps.setString(7, c.getNationality());
            ps.setString(8, c.getAddress());
            ps.setLong(9, c.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
