package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Direct period totals for UC66; no synthetic KPI or mock value. */
public class ManagementReport {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int bookedRoomNights;
    private int reservations;
    private int cancellations;
    private BigDecimal reservationRevenue;
    private BigDecimal successfulPayments;
    private int paymentTransactions;
    private int serviceRequests;
    private int completedServices;

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public int getBookedRoomNights() { return bookedRoomNights; }
    public void setBookedRoomNights(int bookedRoomNights) { this.bookedRoomNights = bookedRoomNights; }
    public int getReservations() { return reservations; }
    public void setReservations(int reservations) { this.reservations = reservations; }
    public int getCancellations() { return cancellations; }
    public void setCancellations(int cancellations) { this.cancellations = cancellations; }
    public BigDecimal getReservationRevenue() { return reservationRevenue; }
    public void setReservationRevenue(BigDecimal reservationRevenue) { this.reservationRevenue = reservationRevenue; }
    public BigDecimal getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(BigDecimal successfulPayments) { this.successfulPayments = successfulPayments; }
    public int getPaymentTransactions() { return paymentTransactions; }
    public void setPaymentTransactions(int paymentTransactions) { this.paymentTransactions = paymentTransactions; }
    public int getServiceRequests() { return serviceRequests; }
    public void setServiceRequests(int serviceRequests) { this.serviceRequests = serviceRequests; }
    public int getCompletedServices() { return completedServices; }
    public void setCompletedServices(int completedServices) { this.completedServices = completedServices; }
}
