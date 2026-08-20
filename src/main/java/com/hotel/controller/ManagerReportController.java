package com.hotel.controller;

import com.hotel.service.ManagerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/** UC66 - date-range reports from production data. */
@WebServlet(urlPatterns={"/manager/reports"})
public class ManagerReportController extends BaseController{
    private final ManagerService service;
    public ManagerReportController(){this(new ManagerService());}
    ManagerReportController(ManagerService service){this.service=service;}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        LocalDate to=dateParam(req,"to");if(to==null)to=LocalDate.now();LocalDate from=dateParam(req,"from");if(from==null)from=to.withDayOfMonth(1);req.setAttribute("from",from);req.setAttribute("to",to);
        try{req.setAttribute("report",service.getReport(from,to));}catch(IllegalArgumentException|IllegalStateException e){req.setAttribute("err",e.getMessage());}catch(RuntimeException e){req.setAttribute("reportError","Không tải được báo cáo. Vui lòng thử lại.");}
        req.getRequestDispatcher("/WEB-INF/views/manager-reports.jsp").forward(req,resp);
    }
}
