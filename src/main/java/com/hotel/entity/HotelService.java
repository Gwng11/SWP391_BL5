package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HotelService {
    private long hotelServiceId;
    private String serviceCode;
    private String serviceName;
    private String description;
    private String unitName;
    private BigDecimal unitPrice;
    private String imageUrl; // <-- THÊM MỚI: Cột lưu URL hình ảnh
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public long getHotelServiceId() { return hotelServiceId; }
    public void setHotelServiceId(long hotelServiceId) { this.hotelServiceId = hotelServiceId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    // GETTER & SETTER MỚI CHO IMAGE_URL
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}