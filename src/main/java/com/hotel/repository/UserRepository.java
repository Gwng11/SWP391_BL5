package com.hotel.repository;

import com.hotel.entity.User;
import com.hotel.interfaces.IUserRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/** F04 (đăng ký/đăng nhập/khóa), F05 (hồ sơ cá nhân) */
public class UserRepository extends BaseRepository implements IUserRepository {

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
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
    public void updateProfile(long userId, String fullName, String phone) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setLong(3, userId);
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
}
