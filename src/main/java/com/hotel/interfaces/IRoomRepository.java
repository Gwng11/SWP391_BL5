package com.hotel.interfaces;

import com.hotel.entity.Room;
import java.util.List;

public interface IRoomRepository {
    /** Tổng số phòng vật lý có thể bán của 1 loại (đang active, không OUT_OF_SERVICE) */
    int countSellableByType(long roomTypeId);
    /** F11: phòng sạch, trống, chưa gán cho ai - sẵn sàng để assign */
    List<Room> findAssignableRooms(long roomTypeId);
    Room findById(long roomId);
    void updateStatus(long roomId, String operationalStatus, String cleaningStatus);
}
