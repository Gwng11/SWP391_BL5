package com.hotel.service;

import com.hotel.entity.Invoice;
import com.hotel.entity.Payment;
import com.hotel.entity.Reservation;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IInvoiceRepository;
import com.hotel.interfaces.IPaymentRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.payment.GatewayResult;
import com.hotel.payment.PaymentGateway;
import com.hotel.payment.PaymentCallbackOutcome;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private IPaymentRepository paymentRepo;
    private IReservationRepository reservationRepo;
    private IInvoiceRepository invoiceRepo;
    private ICustomerRepository customerRepo;
    private EmailService emailService;
    private PaymentGateway gateway;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        paymentRepo = mock(IPaymentRepository.class);
        reservationRepo = mock(IReservationRepository.class);
        invoiceRepo = mock(IInvoiceRepository.class);
        customerRepo = mock(ICustomerRepository.class);
        emailService = mock(EmailService.class);
        gateway = mock(PaymentGateway.class);
        when(gateway.providerName()).thenReturn("SANDBOX_GATEWAY");
        service = new PaymentService(paymentRepo, reservationRepo, invoiceRepo,
                customerRepo, emailService, gateway);
    }

    @Test
    void currentBusinessRulesUseTwentyPercentDepositAndNoInventedTax() {
        assertEquals(0, Constants.DEPOSIT_RATE.compareTo(new BigDecimal("0.20")));
        assertEquals(0, Constants.TAX_RATE.compareTo(BigDecimal.ZERO));
    }

    @Test
    void onlineDepositIsPendingBeforeAtomicCompletion() {
        Reservation reservation = reservation(Constants.RES_PENDING, "1000000", "200000");
        when(reservationRepo.findById(10)).thenReturn(reservation);
        when(paymentRepo.sumSuccess(10, null)).thenReturn(BigDecimal.ZERO);
        when(paymentRepo.insert(any())).thenReturn(77L);
        when(gateway.authorize(any())).thenReturn(GatewayResult.success("GW-77"));
        when(paymentRepo.completeDeposit(77, "GW-77")).thenReturn(true);
        Payment completed = payment(77, Constants.PAY_SUCCESS, "200000");
        when(paymentRepo.findById(77)).thenReturn(completed);

        Payment result = service.payDeposit(10, new BigDecimal("200000"), "ONLINE", null);

        assertSame(completed, result);
        verify(paymentRepo).insert(argThat(p -> Constants.PAY_PENDING.equals(p.getStatusCode())
                && "SANDBOX_GATEWAY".equals(p.getProviderName())));
        verify(paymentRepo).completeDeposit(77, "GW-77");
        verify(reservationRepo, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void rejectedGatewayMarksPendingPaymentFailed() {
        Reservation reservation = reservation(Constants.RES_PENDING, "1000000", "200000");
        when(reservationRepo.findById(10)).thenReturn(reservation);
        when(paymentRepo.sumSuccess(10, null)).thenReturn(BigDecimal.ZERO);
        when(paymentRepo.insert(any())).thenReturn(78L);
        when(gateway.authorize(any())).thenReturn(GatewayResult.failed("Từ chối"));

        assertThrows(IllegalStateException.class,
                () -> service.payDeposit(10, new BigDecimal("200000"), "ONLINE", null));

        verify(paymentRepo).markFailed(78, "Từ chối");
        verify(paymentRepo, never()).completeDeposit(anyLong(), any());
    }

    @Test
    void finalPaymentUsesInvoiceOutstandingAndAtomicCompletion() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(9);
        invoice.setReservationId(10);
        invoice.setStatusCode(Constants.INV_PARTIALLY_PAID);
        invoice.setTotalAmount(new BigDecimal("900000"));
        invoice.setPaidAmount(new BigDecimal("200000"));
        when(invoiceRepo.findByReservation(10)).thenReturn(invoice);
        when(paymentRepo.insert(any())).thenReturn(79L);
        when(gateway.authorize(any())).thenReturn(GatewayResult.success("GW-79"));
        when(paymentRepo.completeFinalPayment(79, "GW-79")).thenReturn(true);
        Payment completed = payment(79, Constants.PAY_SUCCESS, "700000");
        when(paymentRepo.findById(79)).thenReturn(completed);

        Payment result = service.payFinalInvoice(10, "ONLINE", null);

        assertSame(completed, result);
        verify(paymentRepo).insert(argThat(p -> p.getInvoiceId() == 9
                && new BigDecimal("700000").compareTo(p.getAmount()) == 0
                && Constants.PAY_PENDING.equals(p.getStatusCode())));
        verify(paymentRepo).completeFinalPayment(79, "GW-79");
    }

    @Test
    void unsupportedMethodIsRejectedBeforePersistence() {
        assertThrows(IllegalArgumentException.class,
                () -> service.payFinalInvoice(10, "CRYPTO", null));
        verifyNoInteractions(invoiceRepo, paymentRepo, gateway);
    }

    @Test
    void verifiedCallbackCompletesPendingDepositExactlyOnce() {
        when(gateway.requiresRedirect()).thenReturn(true);
        when(gateway.providerName()).thenReturn("VNPAY");
        when(gateway.verifyCallback(anyMap())).thenReturn(GatewayResult.success("VNP-900"));
        Payment pending = payment(80, Constants.PAY_PENDING, "200000");
        pending.setReservationId(10);
        pending.setPaymentType(Constants.PAY_DEPOSIT);
        when(paymentRepo.findByProviderReference("VNPAY", "HMS-80")).thenReturn(pending);
        when(paymentRepo.completeDeposit(80, "VNP-900")).thenReturn(true);
        Payment completed = payment(80, Constants.PAY_SUCCESS, "200000");
        completed.setReservationId(10);
        completed.setPaymentType(Constants.PAY_DEPOSIT);
        when(paymentRepo.findById(80)).thenReturn(completed);

        PaymentCallbackOutcome outcome = service.handleGatewayCallback(java.util.Map.of(
                "vnp_TxnRef", "HMS-80", "vnp_Amount", "20000000"));

        assertTrue(outcome.successful());
        assertFalse(outcome.alreadyProcessed());
        verify(paymentRepo, times(1)).completeDeposit(80, "VNP-900");
    }

    private Reservation reservation(String status, String total, String deposit) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(10);
        reservation.setStatusCode(status);
        reservation.setTotalAmount(new BigDecimal(total));
        reservation.setDepositRequired(new BigDecimal(deposit));
        return reservation;
    }

    private Payment payment(long id, String status, String amount) {
        Payment payment = new Payment();
        payment.setPaymentId(id);
        payment.setStatusCode(status);
        payment.setAmount(new BigDecimal(amount));
        return payment;
    }
}
