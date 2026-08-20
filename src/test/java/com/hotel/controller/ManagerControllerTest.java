package com.hotel.controller;

import com.hotel.entity.ManagementReport;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.service.ManagerService;
import com.hotel.ultis.Constants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ManagerControllerTest {
    @Test void dashboardShowsRetryStateWhenRepositoryFails()throws Exception{ManagerService service=mock(ManagerService.class);when(service.getDashboard(any())).thenThrow(new RuntimeException("db"));ManagerDashboardController controller=new ManagerDashboardController(service);HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);RequestDispatcher dispatcher=mock(RequestDispatcher.class);when(req.getRequestDispatcher("/WEB-INF/views/manager-dashboard.jsp")).thenReturn(dispatcher);controller.doGet(req,resp);verify(req).setAttribute(eq("dashboardError"),anyString());verify(dispatcher).forward(req,resp);}
    @Test void reportSupportsEmptyDataset()throws Exception{ManagerService service=mock(ManagerService.class);ManagementReport report=new ManagementReport();when(service.getReport(any(),any())).thenReturn(report);ManagerReportController controller=new ManagerReportController(service);HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);RequestDispatcher dispatcher=mock(RequestDispatcher.class);when(req.getParameter(anyString())).thenReturn(null);when(req.getRequestDispatcher("/WEB-INF/views/manager-reports.jsp")).thenReturn(dispatcher);controller.doGet(req,resp);verify(req).setAttribute("report",report);verify(dispatcher).forward(req,resp);}
    @Test void housekeepingKeepsCreateFormUsableWhenTaskListFails()throws Exception{ManagerService service=mock(ManagerService.class);when(service.getHousekeepingTasks(any(),any(),any())).thenThrow(new RuntimeException("db"));Room room=new Room();User staff=new User();when(service.getRooms(null,null,null)).thenReturn(List.of(room));when(service.getServiceStaff()).thenReturn(List.of(staff));HousekeepingController controller=new HousekeepingController(service);HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);HttpSession session=mock(HttpSession.class);RequestDispatcher dispatcher=mock(RequestDispatcher.class);User manager=new User();manager.setRoleCode(Constants.ROLE_MANAGER);when(req.getSession()).thenReturn(session);when(session.getAttribute(Constants.SESSION_USER)).thenReturn(manager);when(req.getRequestDispatcher("/WEB-INF/views/housekeeping.jsp")).thenReturn(dispatcher);controller.doGet(req,resp);verify(req).setAttribute("tasks",List.of());verify(req).setAttribute("rooms",List.of(room));verify(req).setAttribute("staff",List.of(staff));verify(req).setAttribute(eq("err"),contains("vẫn có thể tạo"));verify(dispatcher).forward(req,resp);}
    @Test void emptyHousekeepingListShowsEmptyStateWithoutError()throws Exception{ManagerService service=mock(ManagerService.class);when(service.getHousekeepingTasks(any(),any(),any())).thenReturn(List.of());when(service.getRooms(null,null,null)).thenReturn(List.of());when(service.getServiceStaff()).thenReturn(List.of());HousekeepingController controller=new HousekeepingController(service);HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);HttpSession session=mock(HttpSession.class);RequestDispatcher dispatcher=mock(RequestDispatcher.class);User manager=new User();manager.setRoleCode(Constants.ROLE_MANAGER);when(req.getSession()).thenReturn(session);when(session.getAttribute(Constants.SESSION_USER)).thenReturn(manager);when(req.getRequestDispatcher("/WEB-INF/views/housekeeping.jsp")).thenReturn(dispatcher);controller.doGet(req,resp);verify(req,never()).setAttribute(eq("err"),any());verify(dispatcher).forward(req,resp);}
}
