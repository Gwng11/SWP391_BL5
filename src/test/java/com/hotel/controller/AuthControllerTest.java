package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.AuthService;
import com.hotel.ultis.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test void managerDestinationIsDashboardAndOtherRolesStayUnchanged(){User u=new User();u.setRoleCode(Constants.ROLE_MANAGER);assertEquals("/manager/dashboard",AuthController.destinationFor(u));u.setRoleCode(Constants.ROLE_RECEPTIONIST);assertEquals("/reception/checkin",AuthController.destinationFor(u));u.setRoleCode(Constants.ROLE_SERVICE_STAFF);assertEquals("/staff/service-requests",AuthController.destinationFor(u));u.setRoleCode(Constants.ROLE_ADMIN);assertEquals("/reception/checkin",AuthController.destinationFor(u));}
    @Test void managerLoginRotatesSessionAndRedirectsToDashboard()throws Exception{AuthService auth=mock(AuthService.class);AuthController controller=new AuthController(auth);HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);HttpSession session=mock(HttpSession.class);User manager=new User();manager.setRoleCode(Constants.ROLE_MANAGER);when(req.getServletPath()).thenReturn("/login");when(req.getParameter("email")).thenReturn("manager@hotel.vn");when(req.getParameter("password")).thenReturn("x");when(req.getSession()).thenReturn(session);when(req.getContextPath()).thenReturn("/HotelManagement");when(auth.login(anyString(),anyString())).thenReturn(manager);controller.doPost(req,resp);verify(req).changeSessionId();verify(session).setAttribute(Constants.SESSION_USER,manager);verify(resp).sendRedirect("/HotelManagement/manager/dashboard");}
    @Test void logoutIsIdempotent()throws Exception{AuthController controller=new AuthController(mock(AuthService.class));HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);when(req.getServletPath()).thenReturn("/logout");when(req.getSession(false)).thenReturn(null);when(req.getContextPath()).thenReturn("");controller.doGet(req,resp);verify(resp).sendRedirect("/login");}
}
