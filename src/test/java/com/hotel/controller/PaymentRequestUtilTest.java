package com.hotel.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaymentRequestUtilTest {
    @Test
    void localRequestKeepsLocalPort() {
        HttpServletRequest request = request("http", "localhost", 9981, "/HotelManagement");

        assertEquals("http://localhost:9981/HotelManagement/payment/vnpay-return",
                PaymentRequestUtil.gatewayReturnUrl(request, "VNPAY"));
    }

    @Test
    void cloudflareForwardedHeadersProducePublicHttpsCallback() {
        HttpServletRequest request = request("http", "localhost", 9981, "/HotelManagement");
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("hotel-demo.example.com");

        assertEquals("https://hotel-demo.example.com/HotelManagement/payment/vnpay-return",
                PaymentRequestUtil.gatewayReturnUrl(request, "VNPAY"));
    }

    @Test
    void untrustedForwardedHostIsIgnored() {
        HttpServletRequest request = request("http", "hotel.example.com", 80, "/HotelManagement");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("evil.test/path");

        assertEquals("http://hotel.example.com/HotelManagement/payment/vnpay-return",
                PaymentRequestUtil.gatewayReturnUrl(request, "VNPAY"));
    }

    @Test
    void momoUsesItsOwnPublicReturnEndpoint() {
        HttpServletRequest request = request("http", "localhost", 9981, "/HotelManagement");
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("hotel-demo.example.com");

        assertEquals("https://hotel-demo.example.com/HotelManagement/payment/momo-return",
                PaymentRequestUtil.gatewayReturnUrl(request, "MOMO"));
    }

    private HttpServletRequest request(String scheme, String host, int port, String contextPath) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn(scheme);
        when(request.getServerName()).thenReturn(host);
        when(request.getServerPort()).thenReturn(port);
        when(request.getContextPath()).thenReturn(contextPath);
        return request;
    }
}
