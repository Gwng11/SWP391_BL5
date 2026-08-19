package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private long reservationId;
    private long customerId;
    private Long createdByUserId;
    private Long checkedInByUserId;
    private Long checkedOutByUserId;
    private String bookingCode;
    private String sourceCode;
    private String statusCode;
    private LocalDateTime bookedAt;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime actualCheckInAt;
    private LocalDateTime actualCheckOutAt;
    private int adultCount;
    private int childCount;
    private BigDecimal roomSubtotal;
    private BigDecimal serviceTotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositRequired;
    private String specialRequests;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String customerName;
    private String customerEmail;

    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public Long getCheckedInByUserId() { return checkedInByUserId; }
    public void setCheckedInByUserId(Long checkedInByUserId) { this.checkedInByUserId = checkedInByUserId; }
    public Long getCheckedOutByUserId() { return checkedOutByUserId; }
    public void setCheckedOutByUserId(Long checkedOutByUserId) { this.checkedOutByUserId = checkedOutByUserId; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public LocalDateTime getActualCheckInAt() { return actualCheckInAt; }
    public void setActualCheckInAt(LocalDateTime actualCheckInAt) { this.actualCheckInAt = actualCheckInAt; }
    public LocalDateTime getActualCheckOutAt() { return actualCheckOutAt; }
    public void setActualCheckOutAt(LocalDateTime actualCheckOutAt) { this.actualCheckOutAt = actualCheckOutAt; }
    public int getAdultCount() { return adultCount; }
    public void setAdultCount(int adultCount) { this.adultCount = adultCount; }
    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }
    public BigDecimal getRoomSubtotal() { return roomSubtotal; }
    public void setRoomSubtotal(BigDecimal roomSubtotal) { this.roomSubtotal = roomSubtotal; }
    public BigDecimal getServiceTotal() { return serviceTotal; }
    public void setServiceTotal(BigDecimal serviceTotal) { this.serviceTotal = serviceTotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDepositRequired() { return depositRequired; }
    public void setDepositRequired(BigDecimal depositRequired) { this.depositRequired = depositRequired; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}
