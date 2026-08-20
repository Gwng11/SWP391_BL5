package com.hotel.repository;

import com.hotel.entity.EmailLog;
import com.hotel.interfaces.IEmailLogRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class EmailLogRepository extends BaseRepository implements IEmailLogRepository {

    @Override
    public long insert(EmailLog log) {
        String sql = "INSERT INTO email_logs (email_template_id, recipient_user_id, reservation_id, payment_id, "
                   + "invoice_id, triggered_by_user_id, recipient_email, subject_snapshot, body_snapshot, status_code) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,'QUEUED')";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, log.getEmailTemplateId());
            bindLong(ps, 2, log.getRecipientUserId());
            bindLong(ps, 3, log.getReservationId());
            bindLong(ps, 4, log.getPaymentId());
            bindLong(ps, 5, log.getInvoiceId());
            bindLong(ps, 6, log.getTriggeredByUserId());
            ps.setString(7, log.getRecipientEmail());
            ps.setString(8, log.getSubjectSnapshot());
            ps.setString(9, log.getBodySnapshot());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markSent(long emailLogId, String providerName, String providerMessageId) {
        String sql = "UPDATE email_logs SET status_code = 'SENT', sent_at = SYSUTCDATETIME(), failed_at = NULL, last_error = NULL, "
                   + "provider_name = ?, provider_message_id = ? WHERE email_log_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, providerName);
            ps.setString(2, providerMessageId);
            ps.setLong(3, emailLogId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    private EmailLog map(ResultSet rs) throws SQLException {
        EmailLog log = new EmailLog();
        log.setEmailLogId(rs.getLong("email_log_id"));
        log.setEmailTemplateId(rs.getLong("email_template_id"));
        log.setRecipientUserId(longOf(rs, "recipient_user_id"));
        log.setReservationId(longOf(rs, "reservation_id"));
        log.setPaymentId(longOf(rs, "payment_id"));
        log.setInvoiceId(longOf(rs, "invoice_id"));
        log.setTriggeredByUserId(longOf(rs, "triggered_by_user_id"));
        log.setRecipientEmail(rs.getString("recipient_email"));
        log.setSubjectSnapshot(rs.getString("subject_snapshot"));
        log.setBodySnapshot(rs.getString("body_snapshot"));
        log.setStatusCode(rs.getString("status_code"));
        log.setProviderName(rs.getString("provider_name"));
        log.setProviderMessageId(rs.getString("provider_message_id"));
        log.setRetryCount(rs.getInt("retry_count"));
        log.setQueuedAt(tsOf(rs, "queued_at"));
        log.setSentAt(tsOf(rs, "sent_at"));
        log.setFailedAt(tsOf(rs, "failed_at"));
        log.setLastError(rs.getString("last_error"));
        return log;
    }

    @Override
    public void markFailed(long emailLogId, String error) {
        String sql = "UPDATE email_logs SET status_code = 'FAILED', failed_at = SYSUTCDATETIME(), sent_at = NULL, "
                   + "last_error = ? WHERE email_log_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, error == null ? null : error.substring(0, Math.min(500, error.length())));
            ps.setLong(2, emailLogId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<EmailLog> findAll(String search, String statusCode) {
        StringBuilder sql = new StringBuilder("SELECT * FROM email_logs WHERE 1=1 ");
        List<Object> params = new java.util.ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (recipient_email LIKE ? OR subject_snapshot LIKE ?) ");
            String match = "%" + search.trim() + "%";
            params.add(match);
            params.add(match);
        }
        if (statusCode != null && !statusCode.trim().isEmpty()) {
            sql.append("AND status_code = ? ");
            params.add(statusCode.trim());
        }
        sql.append("ORDER BY queued_at DESC");
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<EmailLog> list = new java.util.ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public EmailLog findById(long emailLogId) {
        String sql = "SELECT * FROM email_logs WHERE email_log_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, emailLogId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void incrementRetryCount(long emailLogId) {
        String sql = "UPDATE email_logs SET retry_count = retry_count + 1 WHERE email_log_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, emailLogId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
