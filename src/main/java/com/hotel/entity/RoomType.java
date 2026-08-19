package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomType {
    private long roomTypeId;
    private String typeCode;
    private String typeName;
    private String description;
    private int maxAdults;
    private int maxChildren;
    private String bedType;
    private BigDecimal roomSizeM2;
    private BigDecimal basePrice;
    private String amenitiesJson;
    private String imagesJson;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(long roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getMaxAdults() { return maxAdults; }
    public void setMaxAdults(int maxAdults) { this.maxAdults = maxAdults; }
    public int getMaxChildren() { return maxChildren; }
    public void setMaxChildren(int maxChildren) { this.maxChildren = maxChildren; }
    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }
    public BigDecimal getRoomSizeM2() { return roomSizeM2; }
    public void setRoomSizeM2(BigDecimal roomSizeM2) { this.roomSizeM2 = roomSizeM2; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public String getAmenitiesJson() { return amenitiesJson; }
    public void setAmenitiesJson(String amenitiesJson) { this.amenitiesJson = amenitiesJson; }
    public String getImagesJson() { return imagesJson; }
    public void setImagesJson(String imagesJson) { this.imagesJson = imagesJson; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
