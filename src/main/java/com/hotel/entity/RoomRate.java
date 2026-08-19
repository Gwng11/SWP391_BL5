package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RoomRate {
    private long roomRateId;
    private long roomTypeId;
    private LocalDate rateDate;
    private BigDecimal nightlyPrice;
    private boolean stopSell;

    public long getRoomRateId() { return roomRateId; }
    public void setRoomRateId(long roomRateId) { this.roomRateId = roomRateId; }
    public long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(long roomTypeId) { this.roomTypeId = roomTypeId; }
    public LocalDate getRateDate() { return rateDate; }
    public void setRateDate(LocalDate rateDate) { this.rateDate = rateDate; }
    public BigDecimal getNightlyPrice() { return nightlyPrice; }
    public void setNightlyPrice(BigDecimal nightlyPrice) { this.nightlyPrice = nightlyPrice; }
    public boolean isStopSell() { return stopSell; }
    public void setStopSell(boolean stopSell) { this.stopSell = stopSell; }
}
