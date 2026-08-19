package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceRequest {
    private long serviceRequestId;
    private long reservationId;
    private long customerId;
    private long hotelServiceId;
    private Long assignedStaffUserId;
    private BigDecimal quantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal totalAmount;
    private String statusCode;
    private LocalDateTime requestedAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String notes;
    private String serviceName;
    private String unitName;
    private String bookingCode;
    private String staffName;

    public long getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(long serviceRequestId) { this.serviceRequestId = serviceRequestId; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public long getHotelServiceId() { return hotelServiceId; }
    public void setHotelServiceId(long hotelServiceId) { this.hotelServiceId = hotelServiceId; }
    public Long getAssignedStaffUserId() { return assignedStaffUserId; }
    public void setAssignedStaffUserId(Long assignedStaffUserId) { this.assignedStaffUserId = assignedStaffUserId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) { this.unitPriceSnapshot = unitPriceSnapshot; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}
