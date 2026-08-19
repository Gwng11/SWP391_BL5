package com.hotel.repository;

import com.hotel.entity.UserToken;
import com.hotel.interfaces.IUserTokenRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** F04 - token xác thực email / reset mật khẩu */
public class UserTokenRepository extends BaseRepository implements IUserTokenRepository {

    @Override
    public void insert(UserToken t) {
        String sql = "INSERT INTO user_tokens (user_id, token_hash, token_type, expires_at) VALUES (?, ?, ?, ?)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, t.getUserId());
            ps.setString(2, t.getTokenHash());
            ps.setString(3, t.getTokenType());
            bindTs(ps, 4, t.getExpiresAt());
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public UserToken findValid(String tokenHash, String tokenType) {
        String sql = "SELECT * FROM user_tokens WHERE token_hash = ? AND token_type = ? "
                   + "AND used_at IS NULL AND expires_at > SYSUTCDATETIME()";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.setString(2, tokenType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserToken t = new UserToken();
                    t.setUserTokenId(rs.getLong("user_token_id"));
                    t.setUserId(rs.getLong("user_id"));
                    t.setTokenHash(rs.getString("token_hash"));
                    t.setTokenType(rs.getString("token_type"));
                    t.setExpiresAt(tsOf(rs, "expires_at"));
                    t.setUsedAt(tsOf(rs, "used_at"));
                    t.setCreatedAt(tsOf(rs, "created_at"));
                    return t;
                }
                return null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markUsed(long userTokenId) {
        String sql = "UPDATE user_tokens SET used_at = SYSUTCDATETIME() WHERE user_token_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userTokenId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void invalidateOld(long userId, String tokenType) {
        String sql = "UPDATE user_tokens SET used_at = SYSUTCDATETIME() "
                   + "WHERE user_id = ? AND token_type = ? AND used_at IS NULL";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, tokenType);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
