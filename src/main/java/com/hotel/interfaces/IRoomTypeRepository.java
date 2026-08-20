package com.hotel.interfaces;

import com.hotel.entity.RoomType;
import java.util.List;

public interface IRoomTypeRepository {
    List<RoomType> findAllActive();
    List<RoomType> findAll();
    RoomType findById(long roomTypeId);
    long insert(RoomType roomType);
    void update(RoomType roomType);
    void setActive(long roomTypeId, boolean active);
    void updateAmenities(long roomTypeId, String amenitiesJson);
    void updateImages(long roomTypeId, String imagesJson);
    void updateBasePrice(long roomTypeId, java.math.BigDecimal basePrice);
}
