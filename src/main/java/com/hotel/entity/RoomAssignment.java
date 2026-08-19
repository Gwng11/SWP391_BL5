package com.hotel.entity;

import java.time.LocalDateTime;

public class RoomAssignment {
    private long roomAssignmentId;
    private long reservationRoomId;
    private long roomId;
    private Long assignedByUserId;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private String unassignedReason;
    private boolean current;
    private String roomNumber;
    private String typeName;

    public long getRoomAssignmentId() { return roomAssignmentId; }
    public void setRoomAssignmentId(long roomAssignmentId) { this.roomAssignmentId = roomAssignmentId; }
    public long getReservationRoomId() { return reservationRoomId; }
    public void setReservationRoomId(long reservationRoomId) { this.reservationRoomId = reservationRoomId; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public Long getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(Long assignedByUserId) { this.assignedByUserId = assignedByUserId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getUnassignedAt() { return unassignedAt; }
    public void setUnassignedAt(LocalDateTime unassignedAt) { this.unassignedAt = unassignedAt; }
    public String getUnassignedReason() { return unassignedReason; }
    public void setUnassignedReason(String unassignedReason) { this.unassignedReason = unassignedReason; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}
