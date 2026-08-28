package com.hotel.controller;

import com.hotel.entity.Payment;
import com.hotel.payment.PaymentCallbackOutcome;
import com.hotel.service.PaymentService;
import com.hotel.ultis.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class MomoCallbackControllerTest {
    private PaymentService paymentService;
    private MomoCallbackController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        controller = new MomoCallbackController(paymentService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/HotelManagement");
    }

    @Test
    void validIpnIsAcknowledgedWithNoContent() throws Exception {
        when(request.getServletPath()).thenReturn("/payment/momo-ipn");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"partnerCode\":\"TEST\",\"orderId\":\"HMS1\",\"amount\":180000}")));
        Payment payment = new Payment();
        payment.setReservationId(26);
        payment.setPaymentType(Constants.PAY_DEPOSIT);
        when(paymentService.handleGatewayCallback(anyMap()))
                .thenReturn(new PaymentCallbackOutcome(payment, true, false));

        controller.doPost(request, response);

        verify(paymentService).handleGatewayCallback(argThat(values ->
                "HMS1".equals(values.get("orderId")) && "180000".equals(values.get("amount"))));
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void invalidIpnSignatureReturnsBadRequest() throws Exception {
        when(request.getServletPath()).thenReturn("/payment/momo-ipn");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(paymentService.handleGatewayCallback(anyMap())).thenThrow(new SecurityException("bad"));

        controller.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature");
    }
}
