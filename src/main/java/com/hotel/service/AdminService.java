package com.hotel.service;

import com.hotel.entity.EmailLog;
import com.hotel.entity.EmailTemplate;
import com.hotel.entity.User;
import com.hotel.interfaces.IEmailLogRepository;
import com.hotel.interfaces.IEmailTemplateRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.repository.EmailLogRepository;
import com.hotel.repository.EmailTemplateRepository;
import com.hotel.repository.UserRepository;
import com.hotel.ultis.PasswordUtil;
import com.hotel.ultis.ValidationUtil;
import com.hotel.ultis.EmailUtil;
import java.time.LocalDateTime;
import java.util.List;

public class AdminService {
    private final IUserRepository userRepo;
    private final IEmailTemplateRepository templateRepo;
    private final IEmailLogRepository logRepo;
    private final AuthService authService;

    public AdminService() {
        this(new UserRepository(), new EmailTemplateRepository(), new EmailLogRepository(), new AuthService());
    }

    public AdminService(IUserRepository userRepo, IEmailTemplateRepository templateRepo, IEmailLogRepository logRepo, AuthService authService) {
        this.userRepo = userRepo;
        this.templateRepo = templateRepo;
        this.logRepo = logRepo;
        this.authService = authService;
    }

    // User Management
    public List<User> listUsers(String search, String roleCode, String statusCode) {
        return userRepo.findAll(search, roleCode, statusCode);
    }

    public User getUser(long userId) {
        return userRepo.findById(userId);
    }

    public long createEmployee(String email, String rawPassword, String fullName, String phone, String address, String identificationNumber, String roleCode, String departmentCode, String appBaseUrl) {
        if (!ValidationUtil.isEmail(email)) throw new IllegalArgumentException("Email không hợp lệ");
        if (ValidationUtil.isBlank(fullName)) throw new IllegalArgumentException("Họ tên không được để trống");
        if (ValidationUtil.isBlank(roleCode)) throw new IllegalArgumentException("Vai trò không được để trống");
        if (userRepo.findByEmail(email) != null) throw new IllegalArgumentException("Email đã tồn tại");

        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setFullName(fullName.trim());
        u.setPhone(phone != null ? phone.trim() : null);
        u.setAddress(address != null ? address.trim() : null);
        u.setIdentificationNumber(identificationNumber != null ? identificationNumber.trim() : null);
        u.setRoleCode(roleCode.trim());
        u.setDepartmentCode(ValidationUtil.isBlank(departmentCode) ? null : departmentCode.trim());
        u.setStatusCode("ACTIVE");

        String passwordToHash = rawPassword;
        boolean sendActivation = false;
        if (ValidationUtil.isBlank(passwordToHash)) {
            passwordToHash = java.util.UUID.randomUUID().toString().substring(0, 10) + "a1";
            sendActivation = true;
        } else {
            if (!ValidationUtil.isStrongPassword(passwordToHash)) {
                throw new IllegalArgumentException("Mật khẩu phải tối thiểu 8 ký tự, gồm chữ và số");
            }
        }

        u.setPasswordHash(PasswordUtil.hash(passwordToHash));
        long userId = userRepo.insert(u);
        u.setUserId(userId);

        if (sendActivation) {
            authService.sendVerificationEmail(u, appBaseUrl);
        } else {
            userRepo.markEmailVerified(userId);
        }

        return userId;
    }

    public void updateUser(long userId, String fullName, String phone, String address, String identificationNumber, String roleCode, String departmentCode, String statusCode, String lockedUntilStr) {
        User u = userRepo.findById(userId);
        if (u == null) throw new IllegalArgumentException("Tài khoản không tồn tại");

        if (ValidationUtil.isBlank(fullName)) throw new IllegalArgumentException("Họ tên không được để trống");
        if (ValidationUtil.isBlank(roleCode)) throw new IllegalArgumentException("Vai trò không được để trống");
        if (ValidationUtil.isBlank(statusCode)) throw new IllegalArgumentException("Trạng thái không được để trống");

        LocalDateTime lockedUntil = null;
        if (lockedUntilStr != null && !lockedUntilStr.trim().isEmpty()) {
            try {
                lockedUntil = LocalDateTime.parse(lockedUntilStr.trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Định dạng ngày khóa không hợp lệ (yyyy-MM-ddThh:mm:ss)");
            }
        }

        String dep = ValidationUtil.isBlank(departmentCode) ? null : departmentCode.trim();
        userRepo.updateByAdmin(userId, fullName.trim(), phone != null ? phone.trim() : null, address != null ? address.trim() : null, identificationNumber != null ? identificationNumber.trim() : null, roleCode.trim(), dep, statusCode.trim(), lockedUntil);
    }

    public void resetUserPassword(long userId, String newPassword) {
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới phải tối thiểu 8 ký tự, gồm chữ và số");
        }
        userRepo.updatePassword(userId, PasswordUtil.hash(newPassword));
    }

    public void sendResetPasswordLink(long userId, String appBaseUrl) {
        User u = userRepo.findById(userId);
        if (u == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
        authService.sendResetPasswordEmail(u, appBaseUrl);
    }

    public void deleteUser(long userId, long currentAdminUserId) {
        if (userId == currentAdminUserId) {
            throw new IllegalArgumentException("Không thể xóa tài khoản của chính bạn!");
        }
        User u = userRepo.findById(userId);
        if (u == null) throw new IllegalArgumentException("Tài khoản không tồn tại");

        try {
            userRepo.delete(userId);
        } catch (Exception e) {
            userRepo.updateByAdmin(userId, u.getFullName(), u.getPhone(), u.getAddress(), u.getIdentificationNumber(), u.getRoleCode(), u.getDepartmentCode(), "INACTIVE", null);
        }
    }

    // Email Template Management
    public List<EmailTemplate> listTemplates() {
        return templateRepo.findAll();
    }

    public EmailTemplate getTemplate(long templateId) {
        return templateRepo.findById(templateId);
    }

    public void saveTemplate(EmailTemplate t) {
        if (ValidationUtil.isBlank(t.getTemplateCode())) throw new IllegalArgumentException("Mã template không được để trống");
        if (ValidationUtil.isBlank(t.getTemplateName())) throw new IllegalArgumentException("Tên template không được để trống");
        if (ValidationUtil.isBlank(t.getEventCode())) throw new IllegalArgumentException("Mã sự kiện không được để trống");
        if (ValidationUtil.isBlank(t.getSubjectTemplate())) throw new IllegalArgumentException("Tiêu đề không được để trống");
        if (ValidationUtil.isBlank(t.getBodyHtml())) throw new IllegalArgumentException("Nội dung HTML không được để trống");

        if (t.getEmailTemplateId() > 0) {
            EmailTemplate existing = templateRepo.findById(t.getEmailTemplateId());
            if (existing == null) throw new IllegalArgumentException("Template không tồn tại");
            templateRepo.update(t);
        } else {
            templateRepo.insert(t);
        }
    }

    public void deleteTemplate(long templateId) {
        EmailTemplate t = templateRepo.findById(templateId);
        if (t == null) throw new IllegalArgumentException("Mẫu email không tồn tại");
        try {
            templateRepo.delete(templateId);
        } catch (Exception e) {
            t.setActive(false);
            templateRepo.update(t);
        }
    }

    // Email Log Management
    public List<EmailLog> listEmailLogs(String search, String statusCode) {
        return logRepo.findAll(search, statusCode);
    }

    public boolean retryEmailLog(long emailLogId) {
        EmailLog log = logRepo.findById(emailLogId);
        if (log == null) throw new IllegalArgumentException("Log email không tồn tại");
        logRepo.incrementRetryCount(emailLogId);
        try {
            EmailUtil.send(log.getRecipientEmail(), log.getSubjectSnapshot(), log.getBodySnapshot());
            logRepo.markSent(emailLogId, "SMTP-Retry", null);
            return true;
        } catch (Exception e) {
            logRepo.markFailed(emailLogId, e.getMessage());
            return false;
        }
    }
}
