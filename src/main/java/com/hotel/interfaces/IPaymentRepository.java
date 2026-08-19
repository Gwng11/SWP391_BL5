package com.hotel.interfaces;

import com.hotel.entity.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface IPaymentRepository {
    long insert(Payment p);
    Payment findById(long paymentId);
    List<Payment> findByReservation(long reservationId);
    /** Tổng tiền đã thanh toán SUCCESS của đơn (paymentType null = mọi loại) */
    BigDecimal sumSuccess(long reservationId, String paymentType);
    void markSuccess(long paymentId, String providerReference);
    void markFailed(long paymentId, String reason);
}
