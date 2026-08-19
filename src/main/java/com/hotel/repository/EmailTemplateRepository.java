package com.hotel.repository;

import com.hotel.entity.EmailTemplate;
import com.hotel.interfaces.IEmailTemplateRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmailTemplateRepository extends BaseRepository implements IEmailTemplateRepository {

    @Override
    public EmailTemplate findActiveByEvent(String eventCode) {
        String sql = "SELECT * FROM email_templates WHERE event_code = ? AND is_active = 1";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, eventCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                return null;
            }
        } catch (SQLException e) { throw wrap(e); }
    }
}
