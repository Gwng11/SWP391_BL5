package com.hotel.service;

import com.hotel.entity.HotelProfile;
import com.hotel.interfaces.IHotelProfileRepository;
import com.hotel.repository.HotelProfileRepository;

/** F01 - Xem thông tin khách sạn */
public class HotelInfoService {
    private final IHotelProfileRepository profileRepo = new HotelProfileRepository();

    public HotelProfile getProfile() {
        return profileRepo.getProfile();
    }
}
