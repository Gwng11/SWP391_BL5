package com.hotel.service;

import com.hotel.entity.HotelService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ServiceRequest;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IServiceRequestRepository;
import com.hotel.repository.HotelServiceRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ServiceRequestRepository;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class ServiceRequestService {

    private final IHotelServiceRepository hotelServiceRepo = new HotelServiceRepository();
    private final IServiceRequestRepository requestRepo = new ServiceRequestRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();

    /** F15: Danh mục dịch vụ đang mở (có hỗ trợ tìm kiếm) */
    public List<HotelService> getCatalog() { return hotelServiceRepo.findAllActive(); }

    public List<HotelService> searchCatalog(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return hotelServiceRepo.findAllActive();
        }
        return hotelServiceRepo.searchActive(keyword.trim());
    }

    public HotelService getServiceById(long id) {
        return hotelServiceRepo.findById(id);
    }

    /** F15: Tạo yêu cầu dịch vụ */
    public long createRequest(long reservationId, long hotelServiceId, BigDecimal quantity,
                              LocalDateTime scheduledAt, String notes) {
        Reservation r = reservationRepo.findById(reservationId);
        if (r == null) throw new IllegalArgumentException("Đơn không tồn tại");
        if (!Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
            throw new IllegalStateException("Chỉ yêu cầu dịch vụ khi đang lưu trú");
        HotelService s = hotelServiceRepo.findById(hotelServiceId);
        if (s == null || !s.isActive()) throw new IllegalArgumentException("Dịch vụ không tồn tại");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ");

        ServiceRequest sr = new ServiceRequest();
        sr.setReservationId(reservationId);
        sr.setCustomerId(r.getCustomerId());
        sr.setHotelServiceId(hotelServiceId);
        sr.setQuantity(quantity);
        sr.setUnitPriceSnapshot(s.getUnitPrice());
        sr.setTotalAmount(s.getUnitPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP));
        sr.setScheduledAt(scheduledAt);
        sr.setNotes(notes);
        return requestRepo.insert(sr);
    }

    public List<ServiceRequest> getByReservation(long reservationId) { return requestRepo.findByReservation(reservationId); }
    public List<ServiceRequest> getWorkQueue(String status) { return requestRepo.findWorkQueue(status); }
    public ServiceRequest getById(long id) { return requestRepo.findById(id); }

    public void assign(long serviceRequestId, long staffUserId) { requestRepo.assign(serviceRequestId, staffUserId); }
    public void start(long serviceRequestId) { requestRepo.start(serviceRequestId); }

    public void complete(long serviceRequestId) {
        ServiceRequest sr = requestRepo.findById(serviceRequestId);
        if (sr == null) throw new IllegalArgumentException("Yêu cầu không tồn tại");
        requestRepo.complete(serviceRequestId);
        reservationRepo.addServiceTotal(sr.getReservationId(), sr.getTotalAmount());
    }

    public void cancel(long serviceRequestId, String note) { requestRepo.cancel(serviceRequestId, note); }
}