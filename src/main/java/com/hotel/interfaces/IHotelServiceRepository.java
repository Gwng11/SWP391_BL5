package com.hotel.interfaces;

import com.hotel.entity.HotelService;
import java.util.List;

public interface IHotelServiceRepository {
    List<HotelService> findAllActive();
    HotelService findById(long hotelServiceId);
}
