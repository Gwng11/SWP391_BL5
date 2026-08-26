package com.hotel.controller;

import com.hotel.entity.Invoice;
import com.hotel.entity.Reservation;
import com.hotel.entity.User;
import com.hotel.service.FrontDeskService;
import com.hotel.service.InvoiceService;
import com.hotel.service.ReservationService;
import com.hotel.ultis.Constants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CheckOutControllerTest {
    private FrontDeskService frontDeskService;
    private ReservationService reservationService;
    private InvoiceService invoiceService;
    private CheckOutController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        frontDeskService = mock(FrontDeskService.class);
        reservationService = mock(ReservationService.class);
        invoiceService = mock(InvoiceService.class);
        controller = new CheckOutController(frontDeskService, reservationService, invoiceService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/HotelManagement");
    }

    @Test
    void checkoutScreenProvidesOutstandingBalanceForUnpaidInvoice() throws Exception {
        when(request.getParameter("id")).thenReturn("12");
        when(frontDeskService.getActiveStays()).thenReturn(List.of());
        Reservation reservation = new Reservation();
        reservation.setReservationId(12);
        when(reservationService.getById(12)).thenReturn(reservation);
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(20);
        invoice.setStatusCode(Constants.INV_PARTIALLY_PAID);
        invoice.setTotalAmount(new BigDecimal("900000"));
        invoice.setPaidAmount(new BigDecimal("200000"));
        when(invoiceService.getByReservation(12)).thenReturn(invoice);
        when(invoiceService.getOutstanding(invoice)).thenReturn(new BigDecimal("700000"));
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/WEB-INF/views/checkout.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("outstanding", new BigDecimal("700000"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void successfulPostDelegatesCheckoutToTransactionalService() throws Exception {
        when(request.getParameter("id")).thenReturn("12");
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        User receptionist = new User();
        receptionist.setUserId(7);
        receptionist.setRoleCode(Constants.ROLE_RECEPTIONIST);
        when(session.getAttribute(Constants.SESSION_USER)).thenReturn(receptionist);

        controller.doPost(request, response);

        verify(frontDeskService).checkOut(12, 7);
        verify(response).sendRedirect("/HotelManagement/reception/checkout?done=1");
    }
}
