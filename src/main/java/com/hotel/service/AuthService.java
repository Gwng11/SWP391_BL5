package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.User;
import com.hotel.entity.UserToken;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.interfaces.IUserTokenRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.UserRepository;
import com.hotel.repository.UserTokenRepository;
import com.hotel.ultis.CodeGenerator;
import com.hotel.ultis.Constants;
import com.hotel.ultis.PasswordUtil;
import com.hotel.ultis.TokenUtil;
import com.hotel.ultis.ValidationUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/** F04 - Đăng ký, xác thực email, đăng nhập, khôi phục mật khẩu */
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    private final IUserRepository userRepo;
    private final IUserTokenRepository tokenRepo;
    private final ICustomerRepository customerRepo;
    private final EmailService emailService;

    public AuthService() {
        this(new UserRepository(), new UserTokenRepository(), new CustomerRepository(), new EmailService());
    }

    public AuthService(IUserRepository userRepo, IUserTokenRepository tokenRepo,
                       ICustomerRepository customerRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.customerRepo = customerRepo;
        this.emailService = emailService;
    }

    /** Đăng ký tài khoản CUSTOMER: tạo user + customer + gửi email xác thực */
    public User register(String email, String rawPassword, String fullName, String phone, String appBaseUrl) {
        if (!ValidationUtil.isEmail(email)) throw new IllegalArgumentException("Email không hợp lệ");
        if (!ValidationUtil.isStrongPassword(rawPassword))
            throw new IllegalArgumentException("Mật khẩu tối thiểu 8 ký tự, gồm chữ và số");
        if (ValidationUtil.isBlank(fullName)) throw new IllegalArgumentException("Họ tên không được để trống");
        if (userRepo.findByEmail(email) != null) throw new IllegalArgumentException("Email đã được đăng ký");

        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setPasswordHash(PasswordUtil.hash(rawPassword));
        u.setFullName(fullName.trim());
        u.setPhone(phone);
        u.setRoleCode(Constants.ROLE_CUSTOMER);
        long userId = userRepo.insert(u);
        u.setUserId(userId);

        Customer c = new Customer();
        c.setUserId(userId);
        c.setCustomerCode(CodeGenerator.customerCode());
        c.setFullName(fullName.trim());
        c.setEmail(u.getEmail());
        c.setPhone(phone);
        customerRepo.insert(c);

        sendVerificationEmail(u, appBaseUrl);
        return u;
    }

    public void sendVerificationEmail(User u, String appBaseUrl) {
        String raw = issueToken(u.getUserId(), Constants.TK_EMAIL_VERIFICATION, 24 * 60);
        String link = appBaseUrl + "/verify?token=" + raw;
        emailService.send(Constants.EV_ACCOUNT_VERIFICATION, u.getEmail(),
                Map.of("full_name", u.getFullName(), "verify_link", link),
                u.getUserId(), null, null, null, null);
    }

    /** Xác thực email từ link trong mail */
    public boolean verifyEmail(String rawToken) {
        UserToken t = tokenRepo.findValid(TokenUtil.sha256(rawToken), Constants.TK_EMAIL_VERIFICATION);
        if (t == null) return false;
        tokenRepo.markUsed(t.getUserTokenId());
        userRepo.markEmailVerified(t.getUserId());
        return true;
    }

    /** Đăng nhập: sai quá MAX_FAILED_ATTEMPTS lần thì khóa tạm LOCK_MINUTES phút */
    public User login(String email, String rawPassword) {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(rawPassword))
            throw new IllegalArgumentException(Constants.MSG_LOGIN_REQUIRED);
        User u = userRepo.findByEmail(email == null ? "" : email.trim().toLowerCase());
        if (u == null) throw new IllegalArgumentException(Constants.MSG_INVALID_LOGIN);
        if ("LOCKED".equals(u.getStatusCode()) || "INACTIVE".equals(u.getStatusCode()))
            throw new IllegalArgumentException(Constants.MSG_ACCOUNT_INACTIVE);
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(nowUtc))
            throw new IllegalArgumentException(Constants.MSG_ACCOUNT_LOCKED);

        if (!PasswordUtil.verify(rawPassword, u.getPasswordHash())) {
            int attempts = u.getFailedLoginAttempts() + 1;
            LocalDateTime lockedUntil = attempts >= MAX_FAILED_ATTEMPTS ? nowUtc.plusMinutes(LOCK_MINUTES) : null;
            userRepo.recordLoginFailure(u.getUserId(), attempts, lockedUntil);
            if (lockedUntil != null) throw new IllegalArgumentException(Constants.MSG_ACCOUNT_LOCKED);
            throw new IllegalArgumentException(Constants.MSG_INVALID_LOGIN);
        }
        if (Constants.ROLE_CUSTOMER.equals(u.getRoleCode()) && u.getEmailVerifiedAt() == null)
            throw new IllegalArgumentException("Please verify your email address before logging in.");
        userRepo.recordLoginSuccess(u.getUserId());
        return u;
    }

    /** Quên mật khẩu: gửi link reset (không tiết lộ email có tồn tại hay không) */
    public void forgotPassword(String email, String appBaseUrl) {
        User u = userRepo.findByEmail(email == null ? "" : email.trim().toLowerCase());
        if (u == null || !Constants.ROLE_CUSTOMER.equals(u.getRoleCode())) return;
        String raw = issueToken(u.getUserId(), Constants.TK_PASSWORD_RESET, 60);
        String link = appBaseUrl + "/reset-password?token=" + raw;
        emailService.send(Constants.EV_PASSWORD_RESET, u.getEmail(),
                Map.of("full_name", u.getFullName(), "reset_link", link),
                u.getUserId(), null, null, null, null);
    }

    /** Đặt lại mật khẩu bằng token */
    public boolean resetPassword(String rawToken, String newPassword) {
        if (!ValidationUtil.isStrongPassword(newPassword))
            throw new IllegalArgumentException("Mật khẩu tối thiểu 8 ký tự, gồm chữ và số");
        UserToken t = tokenRepo.findValid(TokenUtil.sha256(rawToken), Constants.TK_PASSWORD_RESET);
        if (t == null) return false;
        tokenRepo.markUsed(t.getUserTokenId());
        userRepo.updatePassword(t.getUserId(), PasswordUtil.hash(newPassword));
        return true;
    }

    /** F05: đổi mật khẩu khi đã đăng nhập */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        User u = userRepo.findById(userId);
        if (u == null || !PasswordUtil.verify(oldPassword, u.getPasswordHash()))
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        if (!ValidationUtil.isStrongPassword(newPassword))
            throw new IllegalArgumentException("Mật khẩu mới tối thiểu 8 ký tự, gồm chữ và số");
        userRepo.updatePassword(userId, PasswordUtil.hash(newPassword));
    }

    private String issueToken(long userId, String type, int minutesValid) {
        tokenRepo.invalidateOld(userId, type);
        String raw = TokenUtil.newRawToken();
        UserToken t = new UserToken();
        t.setUserId(userId);
        t.setTokenHash(TokenUtil.sha256(raw));
        t.setTokenType(type);
        t.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(minutesValid));
        tokenRepo.insert(t);
        return raw;
    }
}
