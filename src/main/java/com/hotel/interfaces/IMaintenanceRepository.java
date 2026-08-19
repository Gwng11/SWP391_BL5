package com.hotel.interfaces;

import com.hotel.entity.MaintenanceTicket;
import java.util.List;

public interface IMaintenanceRepository {
    List<MaintenanceTicket> findAll(String statusCode, String priorityCode, Long roomId, Long staffUserId);
    MaintenanceTicket findById(long ticketId);
    long insert(MaintenanceTicket ticket);
    void assign(long ticketId, String priorityCode, long staffUserId);
    void start(long ticketId, long staffUserId);
    void resolve(long ticketId, long staffUserId, String resolutionNote);
    void reopen(long ticketId, Long staffUserId);
    void close(long ticketId);
}
