package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.AdminService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class AdminUserControllerTest {

    @Test
    void doGetLoadsUsersAndForwards() throws Exception {
        AdminService service = mock(AdminService.class);
        AdminUserController controller = new AdminUserController(service);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getParameter("q")).thenReturn("John");
        when(req.getParameter("roleCode")).thenReturn("MANAGER");
        when(req.getParameter("statusCode")).thenReturn("ACTIVE");
        when(req.getParameter("edit")).thenReturn("123");
        when(req.getContextPath()).thenReturn("");

        User u = new User();
        when(service.listUsers("John", "MANAGER", "ACTIVE")).thenReturn(List.of(u));
        when(service.getUser(123L)).thenReturn(u);
        when(req.getRequestDispatcher("/WEB-INF/views/admin-users.jsp")).thenReturn(dispatcher);

        controller.doGet(req, resp);

        verify(req).setAttribute("users", List.of(u));
        verify(req).setAttribute("editUser", u);
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPostHandlesCreateUser() throws Exception {
        AdminService service = mock(AdminService.class);
        AdminUserController controller = new AdminUserController(service);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getParameter("action")).thenReturn("create");
        when(req.getParameter("email")).thenReturn("new@hotel.vn");
        when(req.getParameter("password")).thenReturn("Secret@123");
        when(req.getParameter("fullName")).thenReturn("New Employee");
        when(req.getParameter("phone")).thenReturn("0911111111");
        when(req.getParameter("address")).thenReturn("Hanoi");
        when(req.getParameter("identificationNumber")).thenReturn("ID999");
        when(req.getParameter("roleCode")).thenReturn("RECEPTIONIST");
        when(req.getParameter("departmentCode")).thenReturn("FRONT_DESK");
        when(req.getContextPath()).thenReturn("/Hotel");

        controller.doPost(req, resp);

        verify(service).createEmployee(
            eq("new@hotel.vn"), eq("Secret@123"), eq("New Employee"), 
            eq("0911111111"), eq("Hanoi"), eq("ID999"), 
            eq("RECEPTIONIST"), eq("FRONT_DESK"), anyString()
        );
        verify(resp).sendRedirect(contains("/admin/users"));
    }
}
