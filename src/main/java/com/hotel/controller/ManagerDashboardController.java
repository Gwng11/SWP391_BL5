package com.hotel.controller;

import com.hotel.service.ManagerService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/** UC65 - Manager landing page with live operational aggregates. */
@WebServlet(urlPatterns = {"/manager/dashboard"})
public class ManagerDashboardController extends BaseController {
    private final ManagerService managerService;
    public ManagerDashboardController(){this(new ManagerService());}
    ManagerDashboardController(ManagerService managerService){this.managerService=managerService;}

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("dashboard",managerService.getDashboard(LocalDate.now()));}
        catch(RuntimeException e){req.setAttribute("dashboardError",Constants.MSG_SYSTEM_ERROR);}
        req.getRequestDispatcher("/WEB-INF/views/manager-dashboard.jsp").forward(req,resp);
    }
}
