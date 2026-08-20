package com.hotel.service;

import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private IUserRepository users;private ICustomerRepository customers;private UserService service;
    @BeforeEach void setUp(){users=mock(IUserRepository.class);customers=mock(ICustomerRepository.class);service=new UserService(users,customers);}
    @Test void updatesOnlyPermittedOwnProfileFields(){service.updateProfile(7," Manager Name ","0901234567"," Hanoi ","ID-123",null);verify(users).updateProfile(7,"Manager Name","0901234567","Hanoi","ID-123");}
    @Test void validatesPhone(){assertThrows(IllegalArgumentException.class,()->service.updateProfile(7,"Name","abc",null,null,null));verifyNoInteractions(users);}
}
