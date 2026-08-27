package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IUserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private IUserRepository users;private ICustomerRepository customers;private UserService service;
    @BeforeEach void setUp(){users=mock(IUserRepository.class);customers=mock(ICustomerRepository.class);service=new UserService(users,customers);}
    @Test void updatesOnlyPermittedOwnProfileFields(){service.updateProfile(7," Manager Name ","0901234567"," Hanoi ","ID-123",null);verify(users).updateProfile(7,"Manager Name","0901234567","Hanoi","ID-123");}
    @Test void validatesPhone(){assertThrows(IllegalArgumentException.class,()->service.updateProfile(7,"Name","abc",null,null,null));verifyNoInteractions(users);}

    @Test void completesRequiredCustomerProfileBeforeReservation() {
        Customer existing = customer(11, "CUS011");
        when(customers.findByUserId(7)).thenReturn(existing);

        Customer submitted = completeProfile();
        Customer result = service.completeCustomerProfileForReservation(7, submitted);

        assertSame(existing, result);
        verify(users).updateProfile(7, "Nguyễn Văn An", "0901234567", "Hà Nội", "012345678901");
        verify(customers).update(argThat(c -> c.getCustomerId() == 11
                && "CUS011".equals(c.getCustomerCode())
                && "an@example.com".equals(c.getEmail())));
    }

    @Test void rejectsIncompleteCustomerProfileBeforeReservation() {
        when(customers.findByUserId(7)).thenReturn(customer(11, "CUS011"));
        Customer submitted = completeProfile();
        submitted.setPhone(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.completeCustomerProfileForReservation(7, submitted));

        assertEquals("Số điện thoại không được để trống", ex.getMessage());
        verifyNoInteractions(users);
        verify(customers, never()).update(any());
    }

    @Test void rejectsIdentityDocumentOwnedByAnotherCustomer() {
        when(customers.findByUserId(7)).thenReturn(customer(11, "CUS011"));
        when(customers.findByDocument("CCCD", "012345678901"))
                .thenReturn(customer(12, "CUS012"));

        assertThrows(IllegalArgumentException.class,
                () -> service.completeCustomerProfileForReservation(7, completeProfile()));

        verifyNoInteractions(users);
        verify(customers, never()).update(any());
    }

    private Customer completeProfile() {
        Customer c = new Customer();
        c.setFullName(" Nguyễn Văn An ");
        c.setPhone("0901 234 567");
        c.setDateOfBirth(LocalDate.of(1995, 5, 20));
        c.setIdDocumentType("CCCD");
        c.setIdDocumentNumber("012345678901");
        c.setNationality("Việt Nam");
        c.setAddress("Hà Nội");
        return c;
    }

    private Customer customer(long id, String code) {
        Customer c = new Customer();
        c.setCustomerId(id);
        c.setUserId(7L);
        c.setCustomerCode(code);
        c.setEmail("an@example.com");
        c.setStatusCode("ACTIVE");
        return c;
    }
}
