package com.hotel.entity;

import java.math.BigDecimal;

public class RoomAvailability {
    private RoomType roomType;
    private int availableRooms;
    private int nights;
    private BigDecimal nightlyAvgPrice;
    private BigDecimal totalPricePerRoom;

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }
    public BigDecimal getNightlyAvgPrice() { return nightlyAvgPrice; }
    public void setNightlyAvgPrice(BigDecimal nightlyAvgPrice) { this.nightlyAvgPrice = nightlyAvgPrice; }
    public BigDecimal getTotalPricePerRoom() { return totalPricePerRoom; }
    public void setTotalPricePerRoom(BigDecimal totalPricePerRoom) { this.totalPricePerRoom = totalPricePerRoom; }
}
