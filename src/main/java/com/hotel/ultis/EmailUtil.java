package com.hotel.ultis;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Gửi email qua SMTP (Gmail: bật App Password).
 * TODO: đổi cấu hình SMTP theo tài khoản của bạn.
 */
public final class EmailUtil {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "quangson2112004@gmail.com";
    private static final String SMTP_PASS = "vohlzaoawyitzwig";
    private static final String FROM_NAME = "Hotel Management";

    private EmailUtil() {
    }

    /**
     * Gửi email HTML. Ném exception nếu thất bại để EmailService ghi log FAILED.
     */
    public static void send(String to, String subject, String htmlBody) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.connectiontimeout", "5000"); // không để lỗi SMTP treo request
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SMTP_USER, FROM_NAME, StandardCharsets.UTF_8.name()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, StandardCharsets.UTF_8.name());
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(msg);
    }
}
