package com.hotel.controller;

import com.hotel.entity.RoomAvailability;
import com.hotel.entity.RoomType;
import com.hotel.service.ManagerService;
import com.hotel.service.RoomService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CataloguePaginationControllerTest {
    @Test void pricingDefaultsToTwentyFiveActiveRoomTypes() throws Exception {
        ManagerService service=mock(ManagerService.class);
        List<RoomType> types=new ArrayList<>();
        for(int i=1;i<=26;i++)types.add(type(i,true));
        types.add(type(99,false));
        when(service.getRoomTypes()).thenReturn(types);
        RoomPricingController controller=new RoomPricingController(service);
        HttpServletRequest req=request("/WEB-INF/views/manager-pricing.jsp");
        HttpServletResponse resp=mock(HttpServletResponse.class);

        controller.doGet(req,resp);

        @SuppressWarnings("unchecked") ArgumentCaptor<List<RoomType>> dropdown=ArgumentCaptor.forClass(List.class);
        verify(req).setAttribute(eq("roomTypes"),dropdown.capture());
        assertEquals(26,dropdown.getValue().size());
        assertTrue(dropdown.getValue().stream().allMatch(RoomType::isActive));
        @SuppressWarnings("unchecked") ArgumentCaptor<List<RoomType>> page=ArgumentCaptor.forClass(List.class);
        verify(req).setAttribute(eq("pricingTypes"),page.capture());
        assertEquals(25,page.getValue().size());
        verify(req).setAttribute("currentPage",1);
        verify(req).setAttribute("totalPages",2);
    }

    @Test void roomSearchShowsDefaultAvailabilityAndPaginatesAtTwentyFive() throws Exception {
        RoomService service=mock(RoomService.class);
        List<RoomAvailability> availability=new ArrayList<>();
        for(int i=1;i<=26;i++){RoomAvailability item=new RoomAvailability();item.setRoomType(type(i,true));item.setAvailableRooms(1);availability.add(item);}
        when(service.searchAvailability(any(),any(),eq(1),eq(0))).thenReturn(availability);
        when(service.getAllActiveTypes()).thenReturn(List.of());
        RoomController controller=new RoomController(service);
        HttpServletRequest req=request("/WEB-INF/views/rooms.jsp");
        when(req.getServletPath()).thenReturn("/rooms");
        HttpServletResponse resp=mock(HttpServletResponse.class);

        controller.doGet(req,resp);

        verify(service).searchAvailability(LocalDate.now().plusDays(1),LocalDate.now().plusDays(2),1,0);
        @SuppressWarnings("unchecked") ArgumentCaptor<List<RoomAvailability>> results=ArgumentCaptor.forClass(List.class);
        verify(req).setAttribute(eq("results"),results.capture());
        assertEquals(25,results.getValue().size());
        verify(req).setAttribute("currentPage",1);
        verify(req).setAttribute("totalPages",2);
    }

    private static HttpServletRequest request(String view){
        HttpServletRequest req=mock(HttpServletRequest.class);RequestDispatcher dispatcher=mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(view)).thenReturn(dispatcher);return req;
    }
    private static RoomType type(long id,boolean active){RoomType t=new RoomType();t.setRoomTypeId(id);t.setTypeCode("T"+id);t.setTypeName("Type "+id);t.setMaxAdults(2);t.setMaxChildren(1);t.setBasePrice(BigDecimal.valueOf(100000+id));t.setActive(active);return t;}
}
