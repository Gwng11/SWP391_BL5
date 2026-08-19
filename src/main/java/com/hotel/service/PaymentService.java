package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.Payment;
import com.hotel.entity.Reservation;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IPaymentRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** F08 - Đặt cọc (tiền mặt / online giả lập cổng thanh toán) */
public class PaymentService {

    private final IPaymentRepository paymentRepo = new PaymentRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final ICustomerRepository customerRepo = new CustomerRepository();
    private final EmailService emailService = new EmailService();

    public List<Payment> getByReservation(long reservationId) { return paymentRepo.findByReservation(reservationId); }

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
     * methodCode: CASH (lễ tân thu) hoặc ONLINE (giả lập cổng thanh toán trả về thành công).
     * Đủ cọc → đơn chuyển CONFIRMED + gửi email biên nhận.
     */
    public Payment payDeposit(long reservationId, BigDecimal amount, String methodCode, Long recordedByUserId) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_PENDING.equals(r.getStatusCode()) && !Constants.RES_CONFIRMED.equals(r.getStatusCode()))
            throw new IllegalStateException("Đơn không ở trạng thái nhận đặt cọc");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền không hợp lệ");
        // V9: không cho thu vượt phần còn phải thu của cả đơn (tính mọi khoản SUCCESS đã nhận)
        BigDecimal remaining = r.getTotalAmount().subtract(getTotalPaid(reservationId));
        if (amount.compareTo(remaining) > 0)
            throw new IllegalArgumentException("Số tiền vượt phần còn phải thu của đơn (tối đa "
                    + remaining.toPlainString() + " đ)");

        Payment p = new Payment();
        p.setReservationId(reservationId);
        p.setRecordedByUserId(recordedByUserId);
        p.setPaymentType(Constants.PAY_DEPOSIT);
        p.setMethodCode(methodCode);
        p.setAmount(amount);
        p.setStatusCode(Constants.PAY_SUCCESS);
        if ("ONLINE".equals(methodCode)) {
            // TODO: tích hợp cổng thật (VNPay/MoMo). Hiện giả lập giao dịch thành công.
            p.setProviderName("SANDBOX_GATEWAY");
            p.setProviderReference("TXN-" + UUID.randomUUID());
        }
        long paymentId = paymentRepo.insert(p);
        p.setPaymentId(paymentId);

        // Đủ cọc → CONFIRMED
        if (Constants.RES_PENDING.equals(r.getStatusCode())
                && getDepositPaid(reservationId).compareTo(r.getDepositRequired()) >= 0) {
            reservationRepo.updateStatus(reservationId, Constants.RES_CONFIRMED);
        }
        sendReceipt(r, p);
        return p;
    }

    private void sendReceipt(Reservation r, Payment p) {
        Customer c = customerRepo.findById(r.getCustomerId());
        if (c == null || c.getEmail() == null || c.getEmail().isEmpty()) return;
        Map<String, String> params = new HashMap<>();
        params.put("full_name", c.getFullName());
        params.put("booking_code", r.getBookingCode());
        params.put("amount", p.getAmount().toPlainString());
        params.put("method", p.getMethodCode());
        params.put("reference", p.getProviderReference() == null ? "-" : p.getProviderReference());
        emailService.send(Constants.EV_DEPOSIT_RECEIPT, c.getEmail(), params,
                c.getUserId(), r.getReservationId(), p.getPaymentId(), null, p.getRecordedByUserId());
    }
}
