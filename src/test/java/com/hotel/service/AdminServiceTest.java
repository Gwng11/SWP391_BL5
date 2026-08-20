package com.hotel.service;

import com.hotel.entity.EmailLog;
import com.hotel.entity.EmailTemplate;
import com.hotel.entity.User;
import com.hotel.interfaces.IEmailLogRepository;
import com.hotel.interfaces.IEmailTemplateRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.ultis.Constants;
import com.hotel.ultis.PasswordUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {
    private IUserRepository userRepo;
    private IEmailTemplateRepository templateRepo;
    private IEmailLogRepository logRepo;
    private AuthService authService;
    private AdminService service;

    @BeforeEach
    void setUp() {
        userRepo = mock(IUserRepository.class);
        templateRepo = mock(IEmailTemplateRepository.class);
        logRepo = mock(IEmailLogRepository.class);
        authService = mock(AuthService.class);
        service = new AdminService(userRepo, templateRepo, logRepo, authService);
    }

    @Test
    void listUsersCallsFindAll() {
        List<User> list = List.of(new User());
        when(userRepo.findAll("search", "ADMIN", "ACTIVE")).thenReturn(list);
        assertSame(list, service.listUsers("search", "ADMIN", "ACTIVE"));
    }

    @Test
    void getUserCallsFindById() {
        User u = new User();
        when(userRepo.findById(123)).thenReturn(u);
        assertSame(u, service.getUser(123));
    }

    @Test
    void createEmployeeValidatesInput() {
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee("invalid-email", "Pass@123", "Name", null, null, null, "RECEPTIONIST", null, "http://localhost"));
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee("staff@hotel.vn", "Pass@123", "", null, null, null, "RECEPTIONIST", null, "http://localhost"));
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee("staff@hotel.vn", "Pass@123", "Name", null, null, null, "", null, "http://localhost"));
    }

    @Test
    void createEmployeeChecksUniqueEmail() {
        when(userRepo.findByEmail("staff@hotel.vn")).thenReturn(new User());
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee("staff@hotel.vn", "Pass@123", "Name", null, null, null, "RECEPTIONIST", null, "http://localhost"));
    }

    @Test
    void createEmployeeWithManualPasswordSavesHashedPasswordAndMarksVerified() {
        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(userRepo.insert(any(User.class))).thenReturn(123L);

        long id = service.createEmployee("staff@hotel.vn", "StaffPassword123", "Staff Name", "0900000001", "Hanoi", "ID123", "RECEPTIONIST", "FRONT_DESK", "http://localhost");

        assertEquals(123L, id);
        verify(userRepo).markEmailVerified(123L);
        verifyNoInteractions(authService);
    }

    @Test
    void createEmployeeWithBlankPasswordTriggersVerificationEmail() {
        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(userRepo.insert(any(User.class))).thenReturn(123L);

        long id = service.createEmployee("staff@hotel.vn", "", "Staff Name", "0900000001", "Hanoi", "ID123", "RECEPTIONIST", "FRONT_DESK", "http://localhost");

        assertEquals(123L, id);
        verify(authService).sendVerificationEmail(any(User.class), eq("http://localhost"));
    }

    @Test
    void updateUserValidatesInputAndUpdates() {
        User u = new User();
        when(userRepo.findById(123L)).thenReturn(u);

        service.updateUser(123L, "New Name", "0900000002", "Hanoi 2", "ID456", "MANAGER", null, "ACTIVE", "2026-08-20T12:00:00");

        verify(userRepo).updateByAdmin(eq(123L), eq("New Name"), eq("0900000002"), eq("Hanoi 2"), eq("ID456"), eq("MANAGER"), any(), eq("ACTIVE"), any(LocalDateTime.class));
    }

    @Test
    void resetUserPasswordSavesHashedPassword() {
        service.resetUserPassword(123L, "NewPassword123");
        verify(userRepo).updatePassword(eq(123L), anyString());
    }

    @Test
    void sendResetPasswordLinkCallsAuthService() {
        User u = new User();
        u.setEmail("test@hotel.vn");
        when(userRepo.findById(123L)).thenReturn(u);

        service.sendResetPasswordLink(123L, "http://localhost");

        verify(authService).sendResetPasswordEmail(u, "http://localhost");
    }

    @Test
    void saveTemplateInsertsOrUpdates() {
        EmailTemplate tNew = new EmailTemplate();
        tNew.setTemplateCode("CODE");
        tNew.setTemplateName("NAME");
        tNew.setEventCode("EVENT");
        tNew.setSubjectTemplate("SUBJ");
        tNew.setBodyHtml("HTML");

        service.saveTemplate(tNew);
        verify(templateRepo).insert(tNew);

        EmailTemplate tEdit = new EmailTemplate();
        tEdit.setEmailTemplateId(55L);
        tEdit.setTemplateCode("CODE");
        tEdit.setTemplateName("NAME");
        tEdit.setEventCode("EVENT");
        tEdit.setSubjectTemplate("SUBJ");
        tEdit.setBodyHtml("HTML");
        when(templateRepo.findById(55L)).thenReturn(tEdit);

        service.saveTemplate(tEdit);
        verify(templateRepo).update(tEdit);
    }

    @Test
    void retryEmailLogSendsAndMarksSent() {
        EmailLog log = new EmailLog();
        log.setEmailLogId(77L);
        log.setRecipientEmail("recipient@test.vn");
        log.setSubjectSnapshot("Subj");
        log.setBodySnapshot("Body");
        when(logRepo.findById(77L)).thenReturn(log);

        try (org.mockito.MockedStatic<com.hotel.ultis.EmailUtil> emailUtilMock = mockStatic(com.hotel.ultis.EmailUtil.class)) {
            boolean ok = service.retryEmailLog(77L);
            assertTrue(ok);
        }

        verify(logRepo).incrementRetryCount(77L);
        verify(logRepo).markSent(eq(77L), eq("SMTP-Retry"), any());
    }

    @Test
    void deleteUserPreventsSelfDeletion() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteUser(99L, 99L));
    }

    @Test
    void deleteUserDeletesOrDeactivates() {
        User u = new User();
        u.setUserId(88L);
        u.setFullName("Staff");
        u.setRoleCode("RECEPTIONIST");
        when(userRepo.findById(88L)).thenReturn(u);

        service.deleteUser(88L, 99L);
        verify(userRepo).delete(88L);
    }

    @Test
    void deleteTemplateDeletesOrDeactivates() {
        EmailTemplate t = new EmailTemplate();
        t.setEmailTemplateId(55L);
        when(templateRepo.findById(55L)).thenReturn(t);

        service.deleteTemplate(55L);
        verify(templateRepo).delete(55L);
    }
}
