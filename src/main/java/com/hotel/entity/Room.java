package com.hotel.entity;

import java.time.LocalDateTime;

public class Room {
    private long roomId;
    private long roomTypeId;
    private String roomNumber;
    private Integer floorNumber;
    private String operationalStatus;
    private String cleaningStatus;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String typeName;

    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(long roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }
    public String getCleaningStatus() { return cleaningStatus; }
    public void setCleaningStatus(String cleaningStatus) { this.cleaningStatus = cleaningStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}
