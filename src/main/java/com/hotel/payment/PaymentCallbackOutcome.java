package com.hotel.payment;

import com.hotel.entity.Payment;

public record PaymentCallbackOutcome(Payment payment, boolean successful, boolean alreadyProcessed) {}
