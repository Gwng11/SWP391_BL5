package com.hotel.interfaces;

import com.hotel.entity.Invoice;
import java.math.BigDecimal;

public interface IInvoiceRepository {
    Invoice findByReservation(long reservationId);
    Invoice findById(long invoiceId);
    long insert(Invoice inv);
    void updateTotals(long invoiceId, BigDecimal subtotal, BigDecimal tax, BigDecimal total);
    void issue(long invoiceId, long byUserId);
    void applyPayment(long invoiceId, BigDecimal newPaidAmount, String statusCode);
}
