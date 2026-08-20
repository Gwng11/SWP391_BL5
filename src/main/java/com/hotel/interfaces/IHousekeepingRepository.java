package com.hotel.interfaces;

import com.hotel.entity.HousekeepingTask;
import java.util.List;

public interface IHousekeepingRepository {
    List<HousekeepingTask> findAll(String statusCode, Long roomId, Long staffUserId);
    HousekeepingTask findById(long taskId);
    boolean hasActiveTask(long roomId);
    long insert(HousekeepingTask task);
    void assign(long taskId, long staffUserId);
    void start(long taskId, long staffUserId);
    void completeCleaning(long taskId, long staffUserId, String notes);
    void inspect(long taskId, long staffUserId, boolean passed, String notes, String maintenanceTicketCode);
}
