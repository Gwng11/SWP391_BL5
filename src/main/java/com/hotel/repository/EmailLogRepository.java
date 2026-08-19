package com.hotel.repository;

import com.hotel.entity.EmailLog;
import com.hotel.interfaces.IEmailLogRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        String sql = "UPDATE email_logs SET status_code = 'SENT', sent_at = SYSUTCDATETIME(), "
                   + "provider_name = ?, provider_message_id = ? WHERE email_log_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, providerName);
            ps.setString(2, providerMessageId);
            ps.setLong(3, emailLogId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void markFailed(long emailLogId, String error) {
        String sql = "UPDATE email_logs SET status_code = 'FAILED', failed_at = SYSUTCDATETIME(), "
                   + "last_error = ? WHERE email_log_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, error == null ? null : error.substring(0, Math.min(500, error.length())));
            ps.setLong(2, emailLogId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
