package com.hotel.repository;

import com.hotel.dal.DBContext;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Lớp cha của mọi repository: kế thừa DBContext + helper map dữ liệu null-safe */
public abstract class BaseRepository extends DBContext {

    protected LocalDateTime tsOf(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    protected LocalDate dateOf(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    protected LocalTime timeOf(ResultSet rs, String col) throws SQLException {
        Time t = rs.getTime(col);
        return t == null ? null : t.toLocalTime();
    }

    protected Long longOf(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    protected Integer intOf(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    protected void bindLong(PreparedStatement ps, int idx, Long v) throws SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.BIGINT); else ps.setLong(idx, v);
    }

    protected void bindTs(PreparedStatement ps, int idx, LocalDateTime v) throws SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.TIMESTAMP); else ps.setTimestamp(idx, Timestamp.valueOf(v));
    }

    protected void bindDate(PreparedStatement ps, int idx, LocalDate v) throws SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.DATE); else ps.setDate(idx, Date.valueOf(v));
    }

    protected RuntimeException wrap(SQLException e) {
        return new RuntimeException("Database error: " + e.getMessage(), e);
    }
}
