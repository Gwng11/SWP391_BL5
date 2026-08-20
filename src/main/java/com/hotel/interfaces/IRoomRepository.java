package com.hotel.interfaces;

import com.hotel.entity.Room;
import java.util.List;

public interface IRoomRepository {
    /** Tổng số phòng vật lý đang AVAILABLE của một loại phòng active. */
    int countSellableByType(long roomTypeId);
    /** F11: phòng sạch, trống, chưa gán cho ai - sẵn sàng để assign */
    List<Room> findAssignableRooms(long roomTypeId);
    List<Room> findAll(Long roomTypeId, Integer floorNumber, String operationalStatus);
    List<Room> findAllAssignableRooms();
    Room findById(long roomId);
    long insert(Room room);
    void update(Room room);
    void updateOperationalStatus(long roomId, String operationalStatus, boolean active);
    void updateStatus(long roomId, String operationalStatus, String cleaningStatus);
}
