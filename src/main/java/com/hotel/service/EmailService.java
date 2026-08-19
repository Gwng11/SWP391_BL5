package com.hotel.service;

import com.hotel.entity.EmailLog;
import com.hotel.entity.EmailTemplate;
import com.hotel.interfaces.IEmailLogRepository;
import com.hotel.interfaces.IEmailTemplateRepository;
import com.hotel.repository.EmailLogRepository;
import com.hotel.repository.EmailTemplateRepository;
import com.hotel.ultis.EmailUtil;
import java.util.Map;

/**
 * Gửi email theo template trong DB (email_templates) + ghi email_logs.
 * Placeholder trong template dạng {{key}}.
 */
public class EmailService {

    private final IEmailTemplateRepository templateRepo = new EmailTemplateRepository();
    private final IEmailLogRepository logRepo = new EmailLogRepository();

    /**
     * @return true nếu gửi thành công. Không ném exception để nghiệp vụ chính không bị hỏng vì lỗi email.
     */
    public boolean send(String eventCode, String recipientEmail, Map<String, String> params,
                        Long recipientUserId, Long reservationId, Long paymentId, Long invoiceId,
                        Long triggeredByUserId) {
        EmailTemplate tpl = templateRepo.findActiveByEvent(eventCode);
        if (tpl == null) {
            System.err.println("[EmailService] Không có template active cho event: " + eventCode);
            return false;
        }
        String subject = render(tpl.getSubjectTemplate(), params);
        String body = render(tpl.getBodyHtml(), params);

        EmailLog log = new EmailLog();
        log.setEmailTemplateId(tpl.getEmailTemplateId());
        log.setRecipientUserId(recipientUserId);
        log.setReservationId(reservationId);
        log.setPaymentId(paymentId);
        log.setInvoiceId(invoiceId);
        log.setTriggeredByUserId(triggeredByUserId);
        log.setRecipientEmail(recipientEmail);
        log.setSubjectSnapshot(subject);
        log.setBodySnapshot(body);
        long logId = logRepo.insert(log);

        try {
            EmailUtil.send(recipientEmail, subject, body);
            logRepo.markSent(logId, "SMTP", null);
            return true;
        } catch (Exception e) {
            logRepo.markFailed(logId, e.getMessage());
            return false;
        }
    }

    private String render(String template, Map<String, String> params) {
        if (template == null) return "";
        String out = template;
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                out = out.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
            }
        }
        return out;
    }
}
