package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.Invoice;
import com.hotel.entity.Payment;
import com.hotel.entity.Reservation;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IInvoiceRepository;
import com.hotel.interfaces.IPaymentRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.payment.GatewayResult;
import com.hotel.payment.PaymentCallbackOutcome;
import com.hotel.payment.PaymentGateway;
import com.hotel.payment.PaymentGatewayFactory;
import com.hotel.payment.PaymentStartResult;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.InvoiceRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** F08/F14 - Thanh toán cọc và hóa đơn cuối. */
public class PaymentService {

    private final IPaymentRepository paymentRepo;
    private final IReservationRepository reservationRepo;
    private final IInvoiceRepository invoiceRepo;
    private final ICustomerRepository customerRepo;
    private final EmailService emailService;
    private final PaymentGateway onlineGateway;

    public PaymentService() {
        this(new PaymentRepository(), new ReservationRepository(), new InvoiceRepository(),
                new CustomerRepository(), new EmailService(), PaymentGatewayFactory.create());
    }

    PaymentService(IPaymentRepository paymentRepo, IReservationRepository reservationRepo,
                   IInvoiceRepository invoiceRepo, ICustomerRepository customerRepo,
                   EmailService emailService, PaymentGateway onlineGateway) {
        this.paymentRepo = paymentRepo;
        this.reservationRepo = reservationRepo;
        this.invoiceRepo = invoiceRepo;
        this.customerRepo = customerRepo;
        this.emailService = emailService;
        this.onlineGateway = onlineGateway;
    }

    public List<Payment> getByReservation(long reservationId) {
        return paymentRepo.findByReservation(reservationId);
    }

    public BigDecimal getDepositPaid(long reservationId) {
        return paymentRepo.sumSuccess(reservationId, Constants.PAY_DEPOSIT);
    }

    public BigDecimal getTotalPaid(long reservationId) {
        return paymentRepo.sumSuccess(reservationId, null);
    }

    /** Số tiền cọc còn phải nộp */
    public BigDecimal getDepositOutstanding(Reservation r) {
        BigDecimal remaining = r.getDepositRequired().subtract(getDepositPaid(r.getReservationId()));
        return remaining.max(BigDecimal.ZERO);
    }

    /**
     * F08: ghi nhận đặt cọc.
     * Payment luôn được tạo PENDING trước. Sau khi phương thức thanh toán xác nhận,
     * payment và reservation được cập nhật nguyên tử trong repository.
     */
    public Payment payDeposit(long reservationId, BigDecimal amount, String methodCode, Long recordedByUserId) {
        PaymentStartResult started = startDeposit(reservationId, amount, methodCode, recordedByUserId, null, null);
        if (started.requiresRedirect())
            throw new IllegalStateException("Thanh toán cần chuyển hướng đến VNPAY");
        return started.payment();
    }

    public PaymentStartResult startDeposit(long reservationId, BigDecimal amount, String methodCode,
                                           Long recordedByUserId, String returnUrl, String clientIp) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null)
            throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_PENDING.equals(r.getStatusCode()) && !Constants.RES_CONFIRMED.equals(r.getStatusCode()))
            throw new IllegalStateException("Đơn không ở trạng thái nhận đặt cọc");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền không hợp lệ");
        // V9: không cho thu vượt phần còn phải thu của cả đơn (tính mọi khoản SUCCESS
        // đã nhận)
        BigDecimal remaining = r.getTotalAmount().subtract(getTotalPaid(reservationId));
        if (amount.compareTo(remaining) > 0)
            throw new IllegalArgumentException("Số tiền vượt phần còn phải thu của đơn(tối đa "
                    + remaining.toPlainString() + " đ)");

        validateMethod(methodCode);
        Payment p = new Payment();
        p.setReservationId(reservationId);
        p.setRecordedByUserId(recordedByUserId);
        p.setPaymentType(Constants.PAY_DEPOSIT);
        p.setMethodCode(methodCode);
        p.setAmount(amount);
        p.setStatusCode(Constants.PAY_PENDING);
        prepareOnlinePayment(p);
        String paymentUrl = null;
        if ("ONLINE".equals(methodCode) && onlineGateway.requiresRedirect())
            paymentUrl = onlineGateway.buildPaymentUrl(p, returnUrl, clientIp);
        long paymentId = paymentRepo.insert(p);
        p.setPaymentId(paymentId);
        if (paymentUrl != null) return new PaymentStartResult(p, paymentUrl);
        Payment completed = completeImmediate(p, true);
        return new PaymentStartResult(completed, null);
    }

    /** Thanh toán toàn bộ số dư của hóa đơn đã phát hành. */
    public Payment payFinalInvoice(long reservationId, String methodCode, Long recordedByUserId) {
        PaymentStartResult started = startFinalInvoice(reservationId, methodCode, recordedByUserId, null, null);
        if (started.requiresRedirect())
            throw new IllegalStateException("Thanh toán cần chuyển hướng đến VNPAY");
        return started.payment();
    }

    public PaymentStartResult startFinalInvoice(long reservationId, String methodCode, Long recordedByUserId,
                                                String returnUrl, String clientIp) {
        validateMethod(methodCode);
        Invoice inv = invoiceRepo.findByReservation(reservationId);
        if (inv == null || Constants.INV_DRAFT.equals(inv.getStatusCode()))
            throw new IllegalStateException("Chưa phát hành hóa đơn");
        if (Constants.INV_PAID.equals(inv.getStatusCode()))
            throw new IllegalStateException("Hóa đơn đã được thanh toán");
        BigDecimal outstanding = inv.getTotalAmount().subtract(inv.getPaidAmount()).max(BigDecimal.ZERO);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("Hóa đơn không còn số dư cần thanh toán");

        Payment p = new Payment();
        p.setReservationId(reservationId);
        p.setInvoiceId(inv.getInvoiceId());
        p.setRecordedByUserId(recordedByUserId);
        p.setPaymentType(Constants.PAY_FINAL);
        p.setMethodCode(methodCode);
        p.setAmount(outstanding);
        p.setStatusCode(Constants.PAY_PENDING);
        prepareOnlinePayment(p);
        String paymentUrl = null;
        if ("ONLINE".equals(methodCode) && onlineGateway.requiresRedirect())
            paymentUrl = onlineGateway.buildPaymentUrl(p, returnUrl, clientIp);
        long paymentId = paymentRepo.insert(p);
        p.setPaymentId(paymentId);
        if (paymentUrl != null) return new PaymentStartResult(p, paymentUrl);
        return new PaymentStartResult(completeImmediate(p, false), null);
    }

    /** Xác minh Return URL/IPN, kiểm tra số tiền rồi hoàn tất payment đúng một lần. */
    public PaymentCallbackOutcome handleGatewayCallback(Map<String, String> parameters) {
        if (!onlineGateway.requiresRedirect())
            throw new IllegalStateException("Gateway hiện tại không nhận callback");
        GatewayResult result = onlineGateway.verifyCallback(parameters);
        String merchantReference = parameters.get("vnp_TxnRef");
        Payment payment = paymentRepo.findByProviderReference(onlineGateway.providerName(), merchantReference);
        if (payment == null) throw new IllegalArgumentException("Không tìm thấy giao dịch VNPAY");
        validateCallbackAmount(payment, parameters.get("vnp_Amount"));
        if (Constants.PAY_SUCCESS.equals(payment.getStatusCode()))
            return new PaymentCallbackOutcome(payment, true, true);
        if (!result.successful()) {
            paymentRepo.markFailed(payment.getPaymentId(), safeFailure(result.failureReason()));
            return new PaymentCallbackOutcome(paymentRepo.findById(payment.getPaymentId()), false, false);
        }
        boolean completed;
        try {
            completed = Constants.PAY_DEPOSIT.equals(payment.getPaymentType())
                    ? paymentRepo.completeDeposit(payment.getPaymentId(), result.providerReference())
                    : paymentRepo.completeFinalPayment(payment.getPaymentId(), result.providerReference());
        } catch (RuntimeException ex) {
            paymentRepo.markFailed(payment.getPaymentId(), safeFailure(ex.getMessage()));
            throw ex;
        }
        Payment saved = paymentRepo.findById(payment.getPaymentId());
        if (completed) sendPaymentReceipt(saved);
        return new PaymentCallbackOutcome(saved, true, !completed);
    }

    private Payment completeImmediate(Payment payment, boolean deposit) {
        GatewayResult result = "ONLINE".equals(payment.getMethodCode())
                ? onlineGateway.authorize(payment) : GatewayResult.success(null);
        if (!result.successful()) {
            paymentRepo.markFailed(payment.getPaymentId(), safeFailure(result.failureReason()));
            throw new IllegalStateException("Thanh toán không thành công: " + safeFailure(result.failureReason()));
        }
        try {
            boolean completed = deposit
                    ? paymentRepo.completeDeposit(payment.getPaymentId(), result.providerReference())
                    : paymentRepo.completeFinalPayment(payment.getPaymentId(), result.providerReference());
            Payment saved = paymentRepo.findById(payment.getPaymentId());
            if (completed) sendPaymentReceipt(saved);
            return saved;
        } catch (RuntimeException ex) {
            paymentRepo.markFailed(payment.getPaymentId(), safeFailure(ex.getMessage()));
            throw ex;
        }
    }

    private void prepareOnlinePayment(Payment payment) {
        if (!"ONLINE".equals(payment.getMethodCode())) return;
        payment.setProviderName(onlineGateway.providerName());
        if (onlineGateway.requiresRedirect()) payment.setProviderReference(merchantReference());
    }

    private String merchantReference() {
        return "HMS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void validateCallbackAmount(Payment payment, String vnpAmount) {
        try {
            BigDecimal returned = new BigDecimal(vnpAmount).movePointLeft(2);
            if (returned.compareTo(payment.getAmount()) != 0)
                throw new IllegalArgumentException("Số tiền VNPAY không khớp giao dịch");
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Số tiền VNPAY không hợp lệ");
        }
    }

    private void validateMethod(String methodCode) {
        if (!"CASH".equals(methodCode) && !"CARD".equals(methodCode)
                && !"BANK_TRANSFER".equals(methodCode) && !"ONLINE".equals(methodCode))
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
    }

    private String safeFailure(String reason) {
        String value = reason == null || reason.isBlank() ? "Gateway từ chối giao dịch" : reason.trim();
        return value.length() <= 255 ? value : value.substring(0, 255);
    }

    private void sendPaymentReceipt(Payment payment) {
        Reservation reservation = reservationRepo.findById(payment.getReservationId());
        if (reservation == null) return;
        if (Constants.PAY_DEPOSIT.equals(payment.getPaymentType())) sendDepositReceipt(reservation, payment);
        else sendInvoiceReceipt(reservation, payment);
    }

    private void sendDepositReceipt(Reservation r, Payment p) {
        Customer c = customerRepo.findById(r.getCustomerId());
        if (c == null || c.getEmail() == null || c.getEmail().isEmpty())
            return;
        Map<String, String> params = new HashMap<>();
        params.put("full_name", c.getFullName());
        params.put("booking_code", r.getBookingCode());
        params.put("amount", p.getAmount().toPlainString());
        params.put("method", p.getMethodCode());
        params.put("reference", p.getProviderReference() == null ? "-" : p.getProviderReference());
        emailService.send(Constants.EV_DEPOSIT_RECEIPT, c.getEmail(), params,
                c.getUserId(), r.getReservationId(), p.getPaymentId(), null, p.getRecordedByUserId());
    }

    private void sendInvoiceReceipt(Reservation r, Payment p) {
        Invoice inv = invoiceRepo.findByReservation(r.getReservationId());
        Customer c = customerRepo.findById(r.getCustomerId());
        if (inv == null || c == null || c.getEmail() == null || c.getEmail().isEmpty()) return;
        Map<String, String> params = new HashMap<>();
        params.put("full_name", c.getFullName());
        params.put("booking_code", r.getBookingCode());
        params.put("invoice_number", inv.getInvoiceNumber());
        params.put("total_amount", inv.getTotalAmount().toPlainString());
        params.put("paid_amount", inv.getPaidAmount().toPlainString());
        emailService.send(Constants.EV_INVOICE_AND_RECEIPT, c.getEmail(), params,
                c.getUserId(), r.getReservationId(), p.getPaymentId(), inv.getInvoiceId(), p.getRecordedByUserId());
    }
}
