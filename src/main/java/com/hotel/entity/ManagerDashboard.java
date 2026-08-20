package com.hotel.entity;

import java.math.BigDecimal;

/** Auditable live values for UC65; occupancy is a room count, not an invented percentage. */
public class ManagerDashboard {
    private int occupiedRooms;
    private int operationalRooms;
    private int arrivals;
    private int departures;
    private int newReservations;
    private BigDecimal revenue;
    private int pendingHousekeepingTasks;
    private int unresolvedMaintenanceIssues;

    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }
    public int getOperationalRooms() { return operationalRooms; }
    public void setOperationalRooms(int operationalRooms) { this.operationalRooms = operationalRooms; }
    public int getArrivals() { return arrivals; }
    public void setArrivals(int arrivals) { this.arrivals = arrivals; }
    public int getDepartures() { return departures; }
    public void setDepartures(int departures) { this.departures = departures; }
    public int getNewReservations() { return newReservations; }
    public void setNewReservations(int newReservations) { this.newReservations = newReservations; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public int getPendingHousekeepingTasks() { return pendingHousekeepingTasks; }
    public void setPendingHousekeepingTasks(int pendingHousekeepingTasks) { this.pendingHousekeepingTasks = pendingHousekeepingTasks; }
    public int getUnresolvedMaintenanceIssues() { return unresolvedMaintenanceIssues; }
    public void setUnresolvedMaintenanceIssues(int unresolvedMaintenanceIssues) { this.unresolvedMaintenanceIssues = unresolvedMaintenanceIssues; }
}
