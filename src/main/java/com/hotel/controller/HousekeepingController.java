package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.ManagerService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** UC48 - Manager creates/assigns/monitors; Service Staff executes/inspects. */
@WebServlet(urlPatterns={"/manager/housekeeping","/staff/housekeeping"})
public class HousekeepingController extends BaseController{
    private static final Logger LOGGER=Logger.getLogger(HousekeepingController.class.getName());
    private final ManagerService service;
    public HousekeepingController(){this(new ManagerService());}
    HousekeepingController(ManagerService service){this.service=service;}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User me=currentUser(req);boolean manager=Constants.ROLE_MANAGER.equals(me.getRoleCode());
        req.setAttribute("isManager",manager);
        Long staffFilter=manager?longParamOrNull(req,"staffId"):Long.valueOf(me.getUserId());
        try{req.setAttribute("tasks",service.getHousekeepingTasks(req.getParameter("status"),longParamOrNull(req,"roomId"),staffFilter));}
        catch(RuntimeException e){LOGGER.log(Level.WARNING,"Housekeeping task list failed",e);req.setAttribute("tasks",List.of());req.setAttribute("err","Không tải được danh sách housekeeping task. Bạn vẫn có thể tạo task mới bên dưới.");}
        try{req.setAttribute("rooms",service.getRooms(null,null,null));}
        catch(RuntimeException e){LOGGER.log(Level.WARNING,"Housekeeping room list failed",e);req.setAttribute("rooms",List.of());req.setAttribute("err","Không tải được danh sách phòng. Vui lòng thử lại.");}
        if(manager)try{req.setAttribute("staff",service.getServiceStaff());}
        catch(RuntimeException e){LOGGER.log(Level.WARNING,"Housekeeping staff list failed",e);req.setAttribute("staff",List.of());req.setAttribute("err","Không tải được danh sách nhân viên. Vui lòng thử lại.");}
        req.getRequestDispatcher("/WEB-INF/views/housekeeping.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User me=currentUser(req);boolean manager=Constants.ROLE_MANAGER.equals(me.getRoleCode());String path=manager?"/manager/housekeeping":"/staff/housekeeping";
        try{
            String action=req.getParameter("action");
            if(manager){
                if("create".equals(action))service.createHousekeepingTask(longParam(req,"roomId"),longParamOrNull(req,"reservationId"),longParamOrNull(req,"staffId"),req.getParameter("taskType"),req.getParameter("priority"),dateTimeParam(req,"scheduledAt"),req.getParameter("notes"),me.getUserId());
                else if("assign".equals(action))service.assignHousekeeping(longParam(req,"taskId"),longParam(req,"staffId"));
                else throw new IllegalStateException(Constants.MSG_NO_PERMISSION);
            }else{
                if("start".equals(action))service.startHousekeeping(longParam(req,"taskId"),me.getUserId());
                else if("complete".equals(action))service.completeCleaning(longParam(req,"taskId"),me.getUserId(),req.getParameter("notes"));
                else if("inspectPass".equals(action))service.inspectHousekeeping(longParam(req,"taskId"),me.getUserId(),true,req.getParameter("notes"));
                else if("inspectFail".equals(action))service.inspectHousekeeping(longParam(req,"taskId"),me.getUserId(),false,req.getParameter("notes"));
                else throw new IllegalStateException(Constants.MSG_NO_PERMISSION);
            }
            redirect(req,resp,path,"msg","Đã cập nhật housekeeping task");
        }catch(IllegalArgumentException|IllegalStateException e){redirect(req,resp,path,"err",e.getMessage());}catch(RuntimeException e){redirect(req,resp,path,"err",Constants.MSG_SYSTEM_ERROR);}
    }
}
