package com.hotel.interfaces;

import com.hotel.entity.HotelService;
import java.util.List;

public interface IHotelServiceRepository {
    List<HotelService> findAllActive();
    List<HotelService> searchActive(String keyword); // <-- THÊM MỚI: Tìm kiếm dịch vụ theo từ khóa
    List<HotelService> findAll();                   // <-- THÊM MỚI: Lấy tất cả (cho Quản lý)
    HotelService findById(long hotelServiceId);
    long insert(HotelService service);               // <-- THÊM MỚI: Tạo dịch vụ mới (CRUD Manager)
    void update(HotelService service);               // <-- THÊM MỚI: Sửa thông tin dịch vụ (CRUD Manager)
    void toggleActive(long hotelServiceId, boolean active); // <-- THÊM MỚI: Ẩn/Hiện dịch vụ
}