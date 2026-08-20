package com.hotel.controller;

import com.hotel.entity.Room;
import com.hotel.service.ManagerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** UC58/UC59 - physical rooms and operational state. */
@WebServlet(urlPatterns={"/manager/rooms"})
public class ManagerRoomController extends BaseController{
    private final ManagerService service=new ManagerService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            Long typeId=longParamOrNull(req,"roomTypeId");Integer floor=req.getParameter("floor")==null||req.getParameter("floor").isBlank()?null:intParam(req,"floor",0);
            req.setAttribute("rooms",service.getRooms(typeId,floor,req.getParameter("status")));req.setAttribute("roomTypes",service.getRoomTypes());
            Long edit=longParamOrNull(req,"edit");if(edit!=null)req.setAttribute("editRoom",service.getRoom(edit));
        }catch(IllegalArgumentException|IllegalStateException e){req.setAttribute("err",e.getMessage());}
        catch(RuntimeException e){req.setAttribute("err","Không tải được dữ liệu phòng. Vui lòng thử lại.");}
        req.getRequestDispatcher("/WEB-INF/views/manager-rooms.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        try{
            String action=req.getParameter("action");
            if("status".equals(action)){service.changeOperationalStatus(longParam(req,"roomId"),req.getParameter("status"));}
            else{
                Room room=new Room();Long id=longParamOrNull(req,"roomId");if(id!=null)room.setRoomId(id);room.setRoomTypeId(longParam(req,"roomTypeId"));room.setRoomNumber(req.getParameter("roomNumber"));
                room.setFloorNumber(req.getParameter("floorNumber")==null||req.getParameter("floorNumber").isBlank()?null:intParam(req,"floorNumber",0));room.setNotes(req.getParameter("notes"));
                if(id==null)service.createRoom(room);else service.updateRoom(room);
            }
            redirect(req,resp,"/manager/rooms","msg","Đã lưu thay đổi phòng");
        }catch(IllegalArgumentException|IllegalStateException e){redirect(req,resp,"/manager/rooms","err",e.getMessage());}
        catch(RuntimeException e){redirect(req,resp,"/manager/rooms","err","Không thể lưu phòng. Kiểm tra mã phòng trùng hoặc thử lại.");}
    }
}
