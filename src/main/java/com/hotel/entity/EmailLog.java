package com.hotel.entity;

import java.time.LocalDateTime;

public class EmailLog {
    private long emailLogId;
    private long emailTemplateId;
    private Long recipientUserId;
    private Long reservationId;
    private Long paymentId;
    private Long invoiceId;
    private Long triggeredByUserId;
    private String recipientEmail;
    private String subjectSnapshot;
    private String bodySnapshot;
    private String payloadJson;
    private String statusCode;
    private String providerName;
    private String providerMessageId;
    private int retryCount;
    private LocalDateTime queuedAt;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
    private String lastError;

    public long getEmailLogId() { return emailLogId; }
    public void setEmailLogId(long emailLogId) { this.emailLogId = emailLogId; }
    public long getEmailTemplateId() { return emailTemplateId; }
    public void setEmailTemplateId(long emailTemplateId) { this.emailTemplateId = emailTemplateId; }
    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getTriggeredByUserId() { return triggeredByUserId; }
    public void setTriggeredByUserId(Long triggeredByUserId) { this.triggeredByUserId = triggeredByUserId; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getSubjectSnapshot() { return subjectSnapshot; }
    public void setSubjectSnapshot(String subjectSnapshot) { this.subjectSnapshot = subjectSnapshot; }
    public String getBodySnapshot() { return bodySnapshot; }
    public void setBodySnapshot(String bodySnapshot) { this.bodySnapshot = bodySnapshot; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public LocalDateTime getQueuedAt() { return queuedAt; }
    public void setQueuedAt(LocalDateTime queuedAt) { this.queuedAt = queuedAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
