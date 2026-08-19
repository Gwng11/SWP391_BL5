package com.hotel.interfaces;

import com.hotel.entity.RoomType;
import java.util.List;

public interface IRoomTypeRepository {
    List<RoomType> findAllActive();
    RoomType findById(long roomTypeId);
}
