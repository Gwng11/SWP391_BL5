package com.hotel.interfaces;

import com.hotel.entity.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface IPaymentRepository {
    long insert(Payment p);
    Payment findById(long paymentId);
    Payment findByProviderReference(String providerName, String providerReference);
    List<Payment> findByReservation(long reservationId);
    /** Tổng tiền đã thanh toán SUCCESS của đơn (paymentType null = mọi loại) */
    BigDecimal sumSuccess(long reservationId, String paymentType);
    /** Hoàn tất payment cọc và xác nhận reservation trong cùng transaction. */
    boolean completeDeposit(long paymentId, String providerReference);
    /** Hoàn tất payment cuối và cập nhật invoice trong cùng transaction. */
    boolean completeFinalPayment(long paymentId, String providerReference);
    void markSuccess(long paymentId, String providerReference);
    void markFailed(long paymentId, String reason);
}
