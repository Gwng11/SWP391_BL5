package com.hotel.payment;

import com.hotel.entity.Payment;

public record PaymentStartResult(Payment payment, String redirectUrl) {
    public boolean requiresRedirect() { return redirectUrl != null && !redirectUrl.isBlank(); }
}
