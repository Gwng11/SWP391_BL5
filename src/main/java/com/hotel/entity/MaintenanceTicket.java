package com.hotel.entity;

import java.time.LocalDateTime;

/** F19 / UC54 + UC57 - maintenance issue and its resolution state. */
public class MaintenanceTicket {
    private long maintenanceTicketId;
    private long roomId;
    private long reportedByUserId;
    private Long assignedStaffUserId;
    private String ticketCode;
    private String title;
    private String description;
    private String priorityCode;
    private String statusCode;
    private LocalDateTime reportedAt;
    private LocalDateTime startedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private String resolutionNote;
    private LocalDateTime updatedAt;
    private String roomNumber;
    private String staffName;

    public long getMaintenanceTicketId() { return maintenanceTicketId; }
    public void setMaintenanceTicketId(long maintenanceTicketId) { this.maintenanceTicketId = maintenanceTicketId; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public long getReportedByUserId() { return reportedByUserId; }
    public void setReportedByUserId(long reportedByUserId) { this.reportedByUserId = reportedByUserId; }
    public Long getAssignedStaffUserId() { return assignedStaffUserId; }
    public void setAssignedStaffUserId(Long assignedStaffUserId) { this.assignedStaffUserId = assignedStaffUserId; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriorityCode() { return priorityCode; }
    public void setPriorityCode(String priorityCode) { this.priorityCode = priorityCode; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}
