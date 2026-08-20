package com.hotel.repository;

import com.hotel.entity.EmailTemplate;
import com.hotel.interfaces.IEmailTemplateRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EmailTemplateRepository extends BaseRepository implements IEmailTemplateRepository {

    private EmailTemplate map(ResultSet rs) throws SQLException {
        EmailTemplate t = new EmailTemplate();
        t.setEmailTemplateId(rs.getLong("email_template_id"));
        t.setTemplateCode(rs.getString("template_code"));
        t.setTemplateName(rs.getString("template_name"));
        t.setEventCode(rs.getString("event_code"));
        t.setSubjectTemplate(rs.getString("subject_template"));
        t.setBodyHtml(rs.getString("body_html"));
        t.setBodyText(rs.getString("body_text"));
        t.setActive(rs.getBoolean("is_active"));
        return t;
    }

    @Override
    public EmailTemplate findActiveByEvent(String eventCode) {
        String sql = "SELECT * FROM email_templates WHERE event_code = ? AND is_active = 1";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, eventCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public List<EmailTemplate> findAll() {
        String sql = "SELECT * FROM email_templates ORDER BY template_code";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<EmailTemplate> list = new java.util.ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public EmailTemplate findById(long templateId) {
        String sql = "SELECT * FROM email_templates WHERE email_template_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public long insert(EmailTemplate t) {
        String sql = "INSERT INTO email_templates (template_code, template_name, event_code, subject_template, body_html, body_text, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTemplateCode());
            ps.setString(2, t.getTemplateName());
            ps.setString(3, t.getEventCode());
            ps.setString(4, t.getSubjectTemplate());
            ps.setString(5, t.getBodyHtml());
            ps.setString(6, t.getBodyText());
            ps.setBoolean(7, t.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void update(EmailTemplate t) {
        String sql = "UPDATE email_templates SET template_code = ?, template_name = ?, event_code = ?, subject_template = ?, body_html = ?, body_text = ?, is_active = ? WHERE email_template_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, t.getTemplateCode());
            ps.setString(2, t.getTemplateName());
            ps.setString(3, t.getEventCode());
            ps.setString(4, t.getSubjectTemplate());
            ps.setString(5, t.getBodyHtml());
            ps.setString(6, t.getBodyText());
            ps.setBoolean(7, t.isActive());
            ps.setLong(8, t.getEmailTemplateId());
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }

    @Override
    public void delete(long templateId) {
        String sql = "DELETE FROM email_templates WHERE email_template_id = ?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, templateId);
            ps.executeUpdate();
        } catch (SQLException e) { throw wrap(e); }
    }
}
