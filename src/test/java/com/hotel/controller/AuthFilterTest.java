package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.ultis.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuthFilterTest {
    private AuthFilter filter;private HttpServletRequest req;private HttpServletResponse resp;private HttpSession session;private FilterChain chain;private RequestDispatcher dispatcher;
    @BeforeEach void setUp(){filter=new AuthFilter();req=mock(HttpServletRequest.class);resp=mock(HttpServletResponse.class);session=mock(HttpSession.class);chain=mock(FilterChain.class);dispatcher=mock(RequestDispatcher.class);when(req.getContextPath()).thenReturn("");when(req.getSession()).thenReturn(session);when(req.getRequestDispatcher("/WEB-INF/views/forbidden.jsp")).thenReturn(dispatcher);}
    @Test void managerCanAccessEveryManagerRoute()throws Exception{for(String path:new String[]{"/manager/dashboard","/manager/rooms","/manager/room-types","/manager/room-types/images","/manager/pricing","/manager/housekeeping","/manager/maintenance","/manager/reports"}){reset(chain);when(req.getRequestURI()).thenReturn(path);when(session.getAttribute(Constants.SESSION_USER)).thenReturn(user(Constants.ROLE_MANAGER));filter.doFilter(req,resp,chain);verify(chain).doFilter(req,resp);}}
    @Test void managerCannotAccessReceptionOrStaffScreens()throws Exception{when(session.getAttribute(Constants.SESSION_USER)).thenReturn(user(Constants.ROLE_MANAGER));for(String path:new String[]{"/reception/checkin","/staff/housekeeping","/staff/maintenance","/staff/service-requests"}){reset(chain,dispatcher);when(req.getRequestDispatcher("/WEB-INF/views/forbidden.jsp")).thenReturn(dispatcher);when(req.getRequestURI()).thenReturn(path);filter.doFilter(req,resp,chain);verify(dispatcher).forward(req,resp);verify(chain,never()).doFilter(req,resp);}}
    @Test void nonManagerCannotAccessManagerRoute()throws Exception{when(req.getRequestURI()).thenReturn("/manager/dashboard");when(session.getAttribute(Constants.SESSION_USER)).thenReturn(user(Constants.ROLE_RECEPTIONIST));filter.doFilter(req,resp,chain);verify(resp).setStatus(403);verify(dispatcher).forward(req,resp);}
    @Test void anonymousUserIsRedirected()throws Exception{when(req.getRequestURI()).thenReturn("/manager/dashboard");when(req.getMethod()).thenReturn("GET");when(session.getAttribute(Constants.SESSION_USER)).thenReturn(null);filter.doFilter(req,resp,chain);verify(session).setAttribute("redirectAfterLogin","/manager/dashboard");verify(resp).sendRedirect("/login?redirect=%2Fmanager%2Fdashboard");}
    private User user(String role){User u=new User();u.setRoleCode(role);return u;}
}
