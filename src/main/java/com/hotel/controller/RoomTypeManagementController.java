package com.hotel.controller;

import com.hotel.entity.RoomType;
import com.hotel.service.ManagerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/** UC60/UC61/UC63 - room type catalogue, amenities and sales status. */
@WebServlet(urlPatterns={"/manager/room-types"})
public class RoomTypeManagementController extends BaseController{
    private final ManagerService service=new ManagerService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            req.setAttribute("roomTypes",service.getRoomTypes());Long edit=longParamOrNull(req,"edit");
            if(edit!=null){req.setAttribute("editType",service.getRoomType(edit));req.setAttribute("amenities",service.getAmenities(edit));req.setAttribute("images",service.getImages(edit));}
        }catch(IllegalArgumentException|IllegalStateException e){req.setAttribute("err",e.getMessage());}
        catch(RuntimeException e){req.setAttribute("err","Không tải được loại phòng. Vui lòng thử lại.");}
        req.getRequestDispatcher("/WEB-INF/views/manager-room-types.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        Long typeId=longParamOrNull(req,"roomTypeId");
        try{
            String action=req.getParameter("action");
            if("toggle".equals(action))service.setRoomTypeActive(longParam(req,"roomTypeId"),Boolean.parseBoolean(req.getParameter("active")));
            else if("amenityAdd".equals(action))service.addAmenity(longParam(req,"roomTypeId"),req.getParameter("value"));
            else if("amenityUpdate".equals(action))service.updateAmenity(longParam(req,"roomTypeId"),intParam(req,"index",-1),req.getParameter("value"));
            else if("amenityRemove".equals(action))service.removeAmenity(longParam(req,"roomTypeId"),intParam(req,"index",-1));
            else{
                RoomType t=new RoomType();if(typeId!=null)t.setRoomTypeId(typeId);t.setTypeCode(req.getParameter("typeCode"));t.setTypeName(req.getParameter("typeName"));t.setDescription(req.getParameter("description"));t.setMaxAdults(intParam(req,"maxAdults",0));t.setMaxChildren(intParam(req,"maxChildren",0));t.setBedType(req.getParameter("bedType"));t.setRoomSizeM2(decimalParam(req,"roomSizeM2"));t.setBasePrice(decimalParam(req,"basePrice"));t.setActive(req.getParameter("active")==null||Boolean.parseBoolean(req.getParameter("active")));if(typeId==null)service.createRoomType(t);else service.updateRoomType(t);
            }
            String path="/manager/room-types"+(typeId==null?"":"?edit="+typeId);redirect(req,resp,path,"msg","Đã lưu loại phòng");
        }catch(IllegalArgumentException|IllegalStateException e){String path="/manager/room-types"+(typeId==null?"":"?edit="+typeId);redirect(req,resp,path,"err",e.getMessage());}
        catch(RuntimeException e){String path="/manager/room-types"+(typeId==null?"":"?edit="+typeId);redirect(req,resp,path,"err","Không thể lưu. Kiểm tra mã/tên trùng hoặc thử lại.");}
    }
}
