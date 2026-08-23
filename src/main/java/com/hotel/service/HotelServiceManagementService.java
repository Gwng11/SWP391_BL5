package com.hotel.service;

import com.hotel.entity.HotelService;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.repository.HotelServiceRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Quản lý danh mục dịch vụ khách sạn dành cho Manager. */
public class HotelServiceManagementService {
    private final IHotelServiceRepository repository;

    public HotelServiceManagementService() { this(new HotelServiceRepository()); }

    public HotelServiceManagementService(IHotelServiceRepository repository) {
        this.repository = repository;
    }

    public List<HotelService> findAll() { return repository.findAll(); }
    public HotelService findById(long id) { return repository.findById(id); }

    public long create(HotelService service) {
        normalizeAndValidate(service);
        ensureCodeUnique(service.getServiceCode(), null);
        service.setActive(true);
        return repository.insert(service);
    }

    public void update(HotelService service) {
        if (repository.findById(service.getHotelServiceId()) == null) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại");
        }
        normalizeAndValidate(service);
        ensureCodeUnique(service.getServiceCode(), service.getHotelServiceId());
        repository.update(service);
    }

    public void setActive(long id, boolean active) {
        if (repository.findById(id) == null) throw new IllegalArgumentException("Dịch vụ không tồn tại");
        repository.toggleActive(id, active);
    }

    public void deleteIfSafe(long id) {
        HotelService service = repository.findById(id);
        if (service == null) throw new IllegalArgumentException("Dịch vụ không tồn tại");
        if (repository.countServiceRequests(id) > 0) {
            throw new IllegalStateException("Dịch vụ đã có lịch sử yêu cầu; hãy ngừng phục vụ thay vì xóa");
        }
        repository.delete(id);
    }

    private void normalizeAndValidate(HotelService service) {
        if (service == null) throw new IllegalArgumentException("Thiếu thông tin dịch vụ");
        service.setServiceCode(trim(service.getServiceCode()).toUpperCase(Locale.ROOT));
        service.setServiceName(trim(service.getServiceName()));
        service.setUnitName(trim(service.getUnitName()));
        service.setDescription(nullableTrim(service.getDescription()));
        service.setImageUrl(nullableTrim(service.getImageUrl()));
        if (!service.getServiceCode().matches("[A-Z0-9_-]{2,20}")) {
            throw new IllegalArgumentException("Mã dịch vụ gồm 2-20 ký tự A-Z, số, '_' hoặc '-'");
        }
        if (service.getServiceName().isEmpty() || service.getServiceName().length() > 100) {
            throw new IllegalArgumentException("Tên dịch vụ phải có từ 1 đến 100 ký tự");
        }
        if (service.getUnitName().isEmpty() || service.getUnitName().length() > 30) {
            throw new IllegalArgumentException("Đơn vị tính phải có từ 1 đến 30 ký tự");
        }
        BigDecimal price = service.getUnitPrice();
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Đơn giá không được âm");
        if (service.getImageUrl() != null && service.getImageUrl().length() > 255) {
            throw new IllegalArgumentException("URL hình ảnh không được vượt quá 255 ký tự");
        }
    }

    private void ensureCodeUnique(String code, Long excludingId) {
        boolean duplicate = repository.findAll().stream().anyMatch(s ->
                s.getServiceCode().equalsIgnoreCase(code)
                        && (excludingId == null || s.getHotelServiceId() != excludingId));
        if (duplicate) throw new IllegalArgumentException("Mã dịch vụ đã tồn tại");
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
    private String nullableTrim(String value) {
        String result = trim(value);
        return result.isEmpty() ? null : result;
    }
}
