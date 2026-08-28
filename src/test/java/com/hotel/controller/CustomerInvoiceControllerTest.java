package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.entity.User;
import com.hotel.service.InvoiceService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.payment.PaymentStartResult;
import com.hotel.entity.Payment;
import com.hotel.ultis.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CustomerInvoiceControllerTest {
    private ReservationService reservationService;
    private InvoiceService invoiceService;
    private PaymentService paymentService;
    private CustomerInvoiceController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        reservationService = mock(ReservationService.class);
        invoiceService = mock(InvoiceService.class);
        paymentService = mock(PaymentService.class);
        controller = new CustomerInvoiceController(reservationService, invoiceService, paymentService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("reservationId")).thenReturn("12");
        when(request.getContextPath()).thenReturn("/HotelManagement");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(9981);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        User user = new User();
        user.setRoleCode(Constants.ROLE_CUSTOMER);
        when(session.getAttribute(Constants.SESSION_USER)).thenReturn(user);
    }

    @Test
    void customerCannotOpenAnotherCustomersInvoice() throws Exception {
        Reservation reservation = reservation(12, 99);
        Customer customer = new Customer();
        customer.setCustomerId(88);
        when(session.getAttribute(Constants.SESSION_CUSTOMER)).thenReturn(customer);
        when(reservationService.getById(12)).thenReturn(reservation);

        controller.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(invoiceService, paymentService);
    }

    @Test
    void customerFinalPaymentAlwaysUsesOnlineAndCannotSubmitAmountOrMethod() throws Exception {
        Reservation reservation = reservation(12, 99);
        Customer customer = new Customer();
        customer.setCustomerId(99);
        when(session.getAttribute(Constants.SESSION_CUSTOMER)).thenReturn(customer);
        when(reservationService.getById(12)).thenReturn(reservation);
        when(request.getParameter("method")).thenReturn("CASH");
        when(request.getParameter("amount")).thenReturn("1");
        when(paymentService.getOnlinePaymentProviderName()).thenReturn("MOMO");
        when(paymentService.startFinalInvoice(eq(12L), eq("ONLINE"), isNull(), anyString(), anyString()))
                .thenReturn(new PaymentStartResult(new Payment(), "https://sandbox.vnpayment.vn/pay"));

        controller.doPost(request, response);

        verify(paymentService).startFinalInvoice(eq(12L), eq("ONLINE"), isNull(),
                eq("http://localhost:9981/HotelManagement/payment/momo-return"), eq("127.0.0.1"));
        verify(response).sendRedirect("https://sandbox.vnpayment.vn/pay");
    }

    private Reservation reservation(long id, long customerId) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(id);
        reservation.setCustomerId(customerId);
        return reservation;
    }
}
