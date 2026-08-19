package com.hotel.interfaces;

import com.hotel.entity.InvoiceItem;
import java.util.List;

public interface IInvoiceItemRepository {
    List<InvoiceItem> findByInvoice(long invoiceId);
    long insert(InvoiceItem item);
    boolean existsForServiceRequest(long serviceRequestId);
    void voidItem(long invoiceItemId);
}
