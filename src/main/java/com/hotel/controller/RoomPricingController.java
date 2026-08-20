package com.hotel.controller;

import com.hotel.service.ManagerService;
import com.hotel.entity.RoomRate;
import com.hotel.entity.RoomType;
import com.hotel.ultis.PageSlice;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** UC64 - base price, daily future price and stop-sell. */
@WebServlet(urlPatterns={"/manager/pricing"})
public class RoomPricingController extends BaseController{
    private static final int PAGE_SIZE=25;
    private final ManagerService service;

    public RoomPricingController(){this(new ManagerService());}
    RoomPricingController(ManagerService service){this.service=service;}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            List<RoomType> activeTypes=service.getRoomTypes().stream().filter(RoomType::isActive).toList();
            PageSlice<RoomType> pricingPage=PageSlice.of(activeTypes,intParam(req,"page",1),PAGE_SIZE);
            req.setAttribute("roomTypes",activeTypes);req.setAttribute("pricingTypes",pricingPage.items());
            req.setAttribute("currentPage",pricingPage.currentPage());req.setAttribute("totalPages",pricingPage.totalPages());req.setAttribute("totalItems",pricingPage.totalItems());
            Long typeId=longParamOrNull(req,"roomTypeId");
            LocalDate from=dateParam(req,"from");LocalDate to=dateParam(req,"to");
            if(from==null)from=LocalDate.now();if(to==null)to=from.plusDays(30);
            req.setAttribute("from",from);req.setAttribute("to",to);
            if(typeId!=null){
                RoomType selected=service.getRoomType(typeId);if(!selected.isActive())throw new IllegalStateException("Loại phòng đã ngừng hoạt động");
                List<RoomRate> allRates=service.getRates(typeId,from,to);
                PageSlice<RoomRate> ratePage=PageSlice.of(allRates,intParam(req,"ratePage",1),PAGE_SIZE);
                req.setAttribute("selectedType",selected);req.setAttribute("rates",ratePage.items());
                req.setAttribute("rateCurrentPage",ratePage.currentPage());req.setAttribute("rateTotalPages",ratePage.totalPages());req.setAttribute("rateTotalItems",ratePage.totalItems());
            }
        }catch(IllegalArgumentException|IllegalStateException e){req.setAttribute("err",e.getMessage());}catch(RuntimeException e){req.setAttribute("err","Không tải được dữ liệu giá.");}
        req.getRequestDispatcher("/WEB-INF/views/manager-pricing.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        long typeId=longParam(req,"roomTypeId");try{service.updatePricing(typeId,decimalParam(req,"basePrice"),dateParam(req,"from"),dateParam(req,"to"),decimalParam(req,"nightlyPrice"),"on".equals(req.getParameter("stopSell")));redirect(req,resp,"/manager/pricing?roomTypeId="+typeId,"msg","Đã cập nhật giá phòng");}catch(IllegalArgumentException|IllegalStateException e){redirect(req,resp,"/manager/pricing?roomTypeId="+typeId,"err",e.getMessage());}catch(RuntimeException e){redirect(req,resp,"/manager/pricing?roomTypeId="+typeId,"err","Không thể cập nhật giá phòng");}
    }
}
