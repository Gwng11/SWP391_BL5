package com.hotel.controller;

import com.hotel.entity.Payment;
import com.hotel.payment.PaymentCallbackOutcome;
import com.hotel.service.PaymentService;
import com.hotel.ultis.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class VnPayCallbackControllerTest {
    private PaymentService paymentService;
    private VnPayCallbackController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        controller = new VnPayCallbackController(paymentService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/HotelManagement");
        when(request.getParameterMap()).thenReturn(Map.of("vnp_TxnRef", new String[]{"HMS-1"}));
    }

    @Test
    void ipnConfirmsNewlyCompletedPayment() throws Exception {
        when(request.getServletPath()).thenReturn("/payment/vnpay-ipn");
        Payment payment = payment();
        when(paymentService.handleGatewayCallback(anyMap()))
                .thenReturn(new PaymentCallbackOutcome(payment, true, false));
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        controller.doGet(request, response);

        assertTrue(body.toString().contains("\"RspCode\":\"00\""));
        verify(response).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void repeatedIpnDoesNotChargeAgain() throws Exception {
        when(request.getServletPath()).thenReturn("/payment/vnpay-ipn");
        when(paymentService.handleGatewayCallback(anyMap()))
                .thenReturn(new PaymentCallbackOutcome(payment(), true, true));
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        controller.doGet(request, response);

        assertTrue(body.toString().contains("\"RspCode\":\"02\""));
    }

    @Test
    void returnRedirectsDepositBackToItsOwnScreen() throws Exception {
        when(request.getServletPath()).thenReturn("/payment/vnpay-return");
        when(paymentService.handleGatewayCallback(anyMap()))
                .thenReturn(new PaymentCallbackOutcome(payment(), true, false));

        controller.doGet(request, response);

        verify(response).sendRedirect(contains("/deposit?reservationId=12&msg="));
    }

    private Payment payment() {
        Payment payment = new Payment();
        payment.setReservationId(12);
        payment.setPaymentType(Constants.PAY_DEPOSIT);
        return payment;
    }
}
