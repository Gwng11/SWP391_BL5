package com.hotel.service;

import com.hotel.entity.Invoice;
import com.hotel.entity.InvoiceItem;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationRoom;
import com.hotel.entity.ServiceRequest;
import com.hotel.interfaces.IInvoiceItemRepository;
import com.hotel.interfaces.IInvoiceRepository;
import com.hotel.interfaces.IPaymentRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IReservationRoomRepository;
import com.hotel.interfaces.IServiceRequestRepository;
import com.hotel.repository.InvoiceItemRepository;
import com.hotel.repository.InvoiceRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ReservationRoomRepository;
import com.hotel.repository.ServiceRequestRepository;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.util.List;

/** F14 - Phát hành hóa đơn & thanh toán cuối */
public class InvoiceService {

    private final IInvoiceRepository invoiceRepo = new InvoiceRepository();
    private final IInvoiceItemRepository invoiceItemRepo = new InvoiceItemRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final IReservationRoomRepository resRoomRepo = new ReservationRoomRepository();
    private final IServiceRequestRepository serviceRequestRepo = new ServiceRequestRepository();
    private final ServiceRequestService serviceRequestService = new ServiceRequestService();
    private final IPaymentRepository paymentRepo = new PaymentRepository();
    private final FrontDeskService frontDeskService = new FrontDeskService();
    private final PaymentService paymentService = new PaymentService();

    public Invoice getByReservation(long reservationId) { return invoiceRepo.findByReservation(reservationId); }
    public List<InvoiceItem> getItems(long invoiceId) { return invoiceItemRepo.findByInvoice(invoiceId); }

    /**
     * F14.1: sinh hóa đơn cuối:
     * - dòng ROOM từ reservation_rooms
     * - dòng SERVICE từ các service_requests COMPLETED chưa lên hóa đơn
     * - giữ nguyên các dòng EXTRA đã thêm trong kỳ ở (F12)
     * - tính lại subtotal / tax / total rồi ISSUE
     */
    public Invoice generateFinalInvoice(long reservationId, long byUserId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
            throw new IllegalStateException("Chỉ phát hành hóa đơn cho khách đang ở (CHECKED_IN)");
        serviceRequestService.requireReadyForFinalInvoice(reservationId);

        Invoice inv = frontDeskService.getOrCreateDraftInvoice(r, byUserId);
        if (!Constants.INV_DRAFT.equals(inv.getStatusCode())) return inv; // đã phát hành rồi

        List<InvoiceItem> existing = invoiceItemRepo.findByInvoice(inv.getInvoiceId());
        boolean hasRoomLines = existing.stream().anyMatch(i -> "ROOM".equals(i.getItemType()));

        if (!hasRoomLines) {
            for (ReservationRoom rr : resRoomRepo.findByReservation(reservationId)) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoiceId(inv.getInvoiceId());
                item.setPostedByUserId(byUserId);
                item.setItemType("ROOM");
                item.setDescription(rr.getTypeName() + " x" + rr.getQuantity() + " (" + rr.getNumberOfNights() + " đêm)");
                item.setQuantity(BigDecimal.valueOf((long) rr.getQuantity() * rr.getNumberOfNights()));
                item.setUnitPrice(rr.getNightlyPriceSnapshot());
                item.setAmount(rr.getLineTotal());
                invoiceItemRepo.insert(item);
            }
        }
        for (ServiceRequest sr : serviceRequestRepo.findCompletedNotInvoiced(reservationId)) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoiceId(inv.getInvoiceId());
            item.setServiceRequestId(sr.getServiceRequestId()); // unique index chặn post 2 lần
            item.setPostedByUserId(byUserId);
            item.setItemType("SERVICE");
            item.setDescription(sr.getServiceName());
            item.setQuantity(sr.getQuantity());
            item.setUnitPrice(sr.getUnitPriceSnapshot());
            item.setAmount(sr.getTotalAmount());
            invoiceItemRepo.insert(item);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceItem i : invoiceItemRepo.findByInvoice(inv.getInvoiceId())) subtotal = subtotal.add(i.getAmount());
        // Giữ snapshot thuế của reservation lịch sử; reservation mới có tax_amount = 0
        // vì SRS hiện hành không quy định tự cộng VAT.
        BigDecimal tax = r.getTaxAmount() == null ? BigDecimal.ZERO : r.getTaxAmount();
        BigDecimal total = subtotal.add(tax);
        invoiceRepo.updateTotals(inv.getInvoiceId(), subtotal, tax, total);
        invoiceRepo.issue(inv.getInvoiceId(), byUserId);

        // Tiền cọc đã nộp tính vào paid_amount
        BigDecimal paidSoFar = paymentRepo.sumSuccess(reservationId, null).min(total);
        invoiceRepo.applyPayment(inv.getInvoiceId(), paidSoFar,
                paidSoFar.compareTo(total) >= 0 ? Constants.INV_PAID
                        : paidSoFar.compareTo(BigDecimal.ZERO) > 0 ? Constants.INV_PARTIALLY_PAID : Constants.INV_ISSUED);
        return invoiceRepo.findById(inv.getInvoiceId());
    }

    /** Hủy dòng phụ thu nhập nhầm - chỉ khi hóa đơn còn DRAFT (chưa phát hành) */
    public void voidExtraItem(long reservationId, long invoiceItemId, long byUserId) {
        Invoice inv = invoiceRepo.findByReservation(reservationId);
        if (inv == null) throw new IllegalArgumentException("Chưa có hóa đơn cho đơn này");
        if (!Constants.INV_DRAFT.equals(inv.getStatusCode()))
            throw new IllegalStateException("Hóa đơn đã phát hành — không thể hủy dòng phụ thu");
        for (InvoiceItem i : invoiceItemRepo.findByInvoice(inv.getInvoiceId())) {
            if (i.getInvoiceItemId() == invoiceItemId) {
                if (!"EXTRA".equals(i.getItemType()))
                    throw new IllegalStateException("Chỉ hủy được dòng phụ thu (EXTRA)");
                invoiceItemRepo.voidItem(invoiceItemId);
                return;
            }
        }
        throw new IllegalArgumentException("Dòng phụ thu không tồn tại trong hóa đơn");
    }

    /** Số tiền còn phải thu */
    public BigDecimal getOutstanding(Invoice inv) {
        return inv.getTotalAmount().subtract(inv.getPaidAmount()).max(BigDecimal.ZERO);
    }

    /** F14.2: thu phần còn lại + gửi email hóa đơn */
    public void processFinalPayment(long reservationId, String methodCode, Long byUserId) {
        Invoice inv = invoiceRepo.findByReservation(reservationId);
        if (inv == null || Constants.INV_DRAFT.equals(inv.getStatusCode()))
            throw new IllegalStateException("Chưa phát hành hóa đơn");
        paymentService.payFinalInvoice(reservationId, methodCode, byUserId);
    }
}
