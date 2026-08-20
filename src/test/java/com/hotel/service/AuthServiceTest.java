package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.interfaces.IUserTokenRepository;
import com.hotel.ultis.Constants;
import com.hotel.ultis.PasswordUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private IUserRepository users;
    private IUserTokenRepository tokens;
    private ICustomerRepository customers;
    private EmailService email;
    private AuthService service;

    @BeforeEach void setUp(){users=mock(IUserRepository.class);tokens=mock(IUserTokenRepository.class);customers=mock(ICustomerRepository.class);email=mock(EmailService.class);service=new AuthService(users,tokens,customers,email);}

    @Test void managerLoginResetsFailedAttempts(){
        User manager=manager("Manager@123");manager.setFailedLoginAttempts(3);when(users.findByEmail("manager@hotel.vn")).thenReturn(manager);
        assertSame(manager,service.login(" manager@hotel.vn ","Manager@123"));
        verify(users).recordLoginSuccess(manager.getUserId());
    }

    @Test void requiredFieldsUseSrsMessage(){
        IllegalArgumentException ex=assertThrows(IllegalArgumentException.class,()->service.login("",""));
        assertEquals(Constants.MSG_LOGIN_REQUIRED,ex.getMessage());verifyNoInteractions(users);
    }

    @Test void fifthFailureLocksForThirtyMinutes(){
        User manager=manager("Manager@123");manager.setFailedLoginAttempts(4);when(users.findByEmail("manager@hotel.vn")).thenReturn(manager);
        LocalDateTime before=LocalDateTime.now(ZoneOffset.UTC).plusMinutes(29);
        IllegalArgumentException ex=assertThrows(IllegalArgumentException.class,()->service.login("manager@hotel.vn","wrong"));
        assertEquals(Constants.MSG_ACCOUNT_LOCKED,ex.getMessage());ArgumentCaptor<LocalDateTime> lock=ArgumentCaptor.forClass(LocalDateTime.class);
        verify(users).recordLoginFailure(eq(manager.getUserId()),eq(5),lock.capture());assertTrue(lock.getValue().isAfter(before));
    }

    @Test void inactiveManagerIsRejected(){User manager=manager("Manager@123");manager.setStatusCode("INACTIVE");when(users.findByEmail(anyString())).thenReturn(manager);assertEquals(Constants.MSG_ACCOUNT_INACTIVE,assertThrows(IllegalArgumentException.class,()->service.login("manager@hotel.vn","Manager@123")).getMessage());}

    @Test void accountRecoveryDoesNotIssueStaffToken(){User manager=manager("Manager@123");when(users.findByEmail(anyString())).thenReturn(manager);service.forgotPassword("manager@hotel.vn","http://localhost");verifyNoInteractions(tokens,email);}

    private User manager(String password){User u=new User();u.setUserId(42);u.setEmail("manager@hotel.vn");u.setPasswordHash(PasswordUtil.hash(password));u.setRoleCode(Constants.ROLE_MANAGER);u.setStatusCode("ACTIVE");return u;}
}
