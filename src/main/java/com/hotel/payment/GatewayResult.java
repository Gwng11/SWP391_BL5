package com.hotel.payment;

public record GatewayResult(boolean successful, String providerReference, String failureReason) {
    public static GatewayResult success(String reference) {
        return new GatewayResult(true, reference, null);
    }

    public static GatewayResult failed(String reason) {
        return new GatewayResult(false, null, reason);
    }
}
