package com.hotel.entity;

import java.math.BigDecimal;

public class ReservationRoom {
    private long reservationRoomId;
    private long reservationId;
    private long roomTypeId;
    private int quantity;
    private int adultCount;
    private int childCount;
    private BigDecimal nightlyPriceSnapshot;
    private int numberOfNights;
    private BigDecimal lineTotal;
    private String notes;
    private String typeName;

    public long getReservationRoomId() { return reservationRoomId; }
    public void setReservationRoomId(long reservationRoomId) { this.reservationRoomId = reservationRoomId; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(long roomTypeId) { this.roomTypeId = roomTypeId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAdultCount() { return adultCount; }
    public void setAdultCount(int adultCount) { this.adultCount = adultCount; }
    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }
    public BigDecimal getNightlyPriceSnapshot() { return nightlyPriceSnapshot; }
    public void setNightlyPriceSnapshot(BigDecimal nightlyPriceSnapshot) { this.nightlyPriceSnapshot = nightlyPriceSnapshot; }
    public int getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(int numberOfNights) { this.numberOfNights = numberOfNights; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}
