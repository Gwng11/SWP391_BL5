package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.ultis.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="HMS_IT",matches="1")
class ManagerLoginIntegrationTest {
    @Test void seededManagerCanLoginAndIsRoutedAsManager(){User user=new AuthService().login("manager.demo@hotel.vn","Manager@123");assertEquals(Constants.ROLE_MANAGER,user.getRoleCode());assertEquals("ACTIVE",user.getStatusCode());}
}
