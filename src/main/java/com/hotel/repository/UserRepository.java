package com.hotel.repository;

import com.hotel.entity.User;
import com.hotel.interfaces.IUserRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** F04 (đăng ký/đăng nhập/khóa), F05 (hồ sơ cá nhân) */
public class UserRepository extends BaseRepository implements IUserRepository {

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setIdentificationNumber(rs.getString("identification_number"));
        u.setRoleCode(rs.getString("role_code"));
        u.setDepartmentCode(rs.getString("department_code"));
        u.setStatusCode(rs.getString("status_code"));
        u.setLastLoginAt(tsOf(rs, "last_login_at"));
        u.setCreatedAt(tsOf(rs, "created_at"));
        u.setUpdatedAt(tsOf(rs, "updated_at"));
        u.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        u.setLockedUntil(tsOf(rs, "locked_until"));
        u.setEmailVerifiedAt(tsOf(rs, "email_verified_at"));
        return u;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public User findById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<User> findActiveByRole(String roleCode) {
        String sql = "SELECT * FROM users WHERE role_code = ? AND status_code = 'ACTIVE' ORDER BY full_name";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(User u) {
        String sql = "INSERT INTO users (email, password_hash, full_name, phone, role_code, department_code, status_code) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getRoleCode());
            ps.setString(6, u.getDepartmentCode());
            ps.setString(7, u.getStatusCode() == null ? "ACTIVE" : u.getStatusCode());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateProfile(long userId, String fullName, String phone, String address, String identificationNumber) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, address = ?, identification_number = ?, "
                   + "updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, identificationNumber);
            ps.setLong(5, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updatePassword(long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void recordLoginSuccess(long userId) {
        String sql = "UPDATE users SET last_login_at = SYSUTCDATETIME(), failed_login_attempts = 0, "
                   + "locked_until = NULL WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void recordLoginFailure(long userId, int failedAttempts, LocalDateTime lockedUntil) {
        String sql = "UPDATE users SET failed_login_attempts = ?, locked_until = ? WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            bindTs(ps, 2, lockedUntil);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markEmailVerified(long userId) {
        String sql = "UPDATE users SET email_verified_at = SYSUTCDATETIME(), updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    /** Danh sách SERVICE_STAFF đang hoạt động, kèm số việc ASSIGNED/IN_PROGRESS. */
    @Override
    public List<User> findActiveServiceStaffWithWorkload(String departmentCode) {
        String sql = "SELECT u.*, COALESCE(w.cnt, 0) AS active_task_count "
                + "FROM users u LEFT JOIN ("
                + "SELECT assigned_staff_user_id, COUNT(*) cnt FROM service_requests "
                + "WHERE status_code IN ('ASSIGNED','IN_PROGRESS') GROUP BY assigned_staff_user_id"
                + ") w ON w.assigned_staff_user_id = u.user_id "
                + "WHERE u.role_code = 'SERVICE_STAFF' AND u.status_code = 'ACTIVE' "
                + "AND u.department_code = ? "
                + "ORDER BY COALESCE(w.cnt, 0), u.full_name";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, departmentCode);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    User user = map(rs);
                    user.setActiveTaskCount(rs.getInt("active_task_count"));
                    users.add(user);
                }
                return users;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<User> findAll(String search, String roleCode, String statusCode) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (email LIKE ? OR full_name LIKE ? OR phone LIKE ?) ");
            String match = "%" + search.trim() + "%";
            params.add(match);
            params.add(match);
            params.add(match);
        }
        if (roleCode != null && !roleCode.trim().isEmpty()) {
            sql.append("AND role_code = ? ");
            params.add(roleCode.trim());
        }
        if (statusCode != null && !statusCode.trim().isEmpty()) {
            sql.append("AND status_code = ? ");
            params.add(statusCode.trim());
        }
        sql.append("ORDER BY created_at DESC");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void updateByAdmin(long userId, String fullName, String phone, String address, String identificationNumber, String roleCode, String departmentCode, String statusCode, LocalDateTime lockedUntil) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, address = ?, identification_number = ?, role_code = ?, department_code = ?, status_code = ?, locked_until = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, identificationNumber);
            ps.setString(5, roleCode);
            ps.setString(6, departmentCode);
            ps.setString(7, statusCode);
            bindTs(ps, 8, lockedUntil);
            ps.setLong(9, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void delete(long userId) {
        String deleteTokens = "DELETE FROM user_tokens WHERE user_id = ?";
        String deleteUser = "DELETE FROM users WHERE user_id = ?";
        try (Connection cn = getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(deleteTokens)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(deleteUser)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) { throw wrap(e); }
    }
}
