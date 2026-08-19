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
    private static final String SMTP_USER = "your.email@gmail.com";   // TODO
    private static final String SMTP_PASS = "your-app-password";      // TODO
    private static final String FROM_NAME = "Hotel Management";

    private EmailUtil() {}

    /** Gửi email HTML. Ném exception nếu thất bại để EmailService ghi log FAILED. */
    public static void send(String to, String subject, String htmlBody) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

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
