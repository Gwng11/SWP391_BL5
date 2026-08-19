package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private long paymentId;
    private long reservationId;
    private Long invoiceId;
    private Long recordedByUserId;
    private String paymentType;
    private String methodCode;
    private BigDecimal amount;
    private String currencyCode;
    private String statusCode;
    private String providerName;
    private String providerReference;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public long getPaymentId() { return paymentId; }
    public void setPaymentId(long paymentId) { this.paymentId = paymentId; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(Long recordedByUserId) { this.recordedByUserId = recordedByUserId; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public String getMethodCode() { return methodCode; }
    public void setMethodCode(String methodCode) { this.methodCode = methodCode; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
