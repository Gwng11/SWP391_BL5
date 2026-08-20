package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.ManagerService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** UC54/UC57 - shared issue screen with role-specific actions. */
@WebServlet(urlPatterns={"/manager/maintenance","/staff/maintenance"})
public class MaintenanceController extends BaseController{
    private final ManagerService service=new ManagerService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User me=currentUser(req);boolean manager=Constants.ROLE_MANAGER.equals(me.getRoleCode());
        try{req.setAttribute("isManager",manager);req.setAttribute("issues",service.getMaintenanceTickets(req.getParameter("status"),req.getParameter("priority"),longParamOrNull(req,"roomId"),manager?longParamOrNull(req,"staffId"):null));req.setAttribute("rooms",service.getRooms(null,null,null));if(manager)req.setAttribute("staff",service.getServiceStaff());}
        catch(RuntimeException e){req.setAttribute("err","Không tải được maintenance issues.");}
        req.getRequestDispatcher("/WEB-INF/views/maintenance.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User me=currentUser(req);boolean manager=Constants.ROLE_MANAGER.equals(me.getRoleCode());String path=manager?"/manager/maintenance":"/staff/maintenance";
        try{
            String action=req.getParameter("action");
            if(manager){
                if("assign".equals(action))service.assignMaintenance(longParam(req,"ticketId"),req.getParameter("priority"),longParam(req,"staffId"));
                else if("close".equals(action))service.closeMaintenance(longParam(req,"ticketId"));
                else if("reopen".equals(action))service.reopenMaintenance(longParam(req,"ticketId"),longParamOrNull(req,"staffId"));
                else throw new IllegalStateException(Constants.MSG_NO_PERMISSION);
            }else{
                if("report".equals(action))service.reportMaintenance(longParam(req,"roomId"),me.getUserId(),req.getParameter("title"),req.getParameter("description"),req.getParameter("priority"),null);
                else if("start".equals(action))service.startMaintenance(longParam(req,"ticketId"),me.getUserId());
                else if("resolve".equals(action))service.resolveMaintenance(longParam(req,"ticketId"),me.getUserId(),req.getParameter("resolutionNote"));
                else throw new IllegalStateException(Constants.MSG_NO_PERMISSION);
            }
            redirect(req,resp,path,"msg","Đã cập nhật maintenance issue");
        }catch(IllegalArgumentException|IllegalStateException e){redirect(req,resp,path,"err",e.getMessage());}catch(RuntimeException e){redirect(req,resp,path,"err",Constants.MSG_SYSTEM_ERROR);}
    }
}
