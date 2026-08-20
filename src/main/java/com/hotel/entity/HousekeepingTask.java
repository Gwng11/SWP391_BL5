package com.hotel.entity;

import java.time.LocalDateTime;

/** F17 / UC48 - housekeeping work item and inspection result. */
public class HousekeepingTask {
    private long housekeepingTaskId;
    private long roomId;
    private Long reservationId;
    private Long assignedStaffUserId;
    private Long createdByUserId;
    private String taskType;
    private String priorityCode;
    private String statusCode;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String notes;
    private String inspectionStatus;
    private String inspectionNotes;
    private Long inspectedByUserId;
    private LocalDateTime inspectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String roomNumber;
    private String staffName;

    public long getHousekeepingTaskId() { return housekeepingTaskId; }
    public void setHousekeepingTaskId(long housekeepingTaskId) { this.housekeepingTaskId = housekeepingTaskId; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public Long getAssignedStaffUserId() { return assignedStaffUserId; }
    public void setAssignedStaffUserId(Long assignedStaffUserId) { this.assignedStaffUserId = assignedStaffUserId; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getPriorityCode() { return priorityCode; }
    public void setPriorityCode(String priorityCode) { this.priorityCode = priorityCode; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getInspectionStatus() { return inspectionStatus; }
    public void setInspectionStatus(String inspectionStatus) { this.inspectionStatus = inspectionStatus; }
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
    public Long getInspectedByUserId() { return inspectedByUserId; }
    public void setInspectedByUserId(Long inspectedByUserId) { this.inspectedByUserId = inspectedByUserId; }
    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}
