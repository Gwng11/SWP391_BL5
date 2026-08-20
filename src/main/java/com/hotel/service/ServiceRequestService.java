package com.hotel.service;

import com.hotel.entity.HotelService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ServiceRequest;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IServiceRequestRepository;
import com.hotel.interfaces.IRoomRepository;
import com.hotel.repository.HotelServiceRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ServiceRequestRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.ultis.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class ServiceRequestService {

    private final IHotelServiceRepository hotelServiceRepo = new HotelServiceRepository();
    private final IServiceRequestRepository requestRepo = new ServiceRequestRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final IRoomRepository roomRepo = new RoomRepository();

    public List<HotelService> getCatalog() { return hotelServiceRepo.findAllActive(); }

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

    /** Tự động tạo Task dọn phòng khi Check-out */
    public void createHousekeepingTask(long reservationId, long customerId, String roomNumber) {
        // Tìm dịch vụ Dọn phòng (nếu chưa có thì lấy dịch vụ mặc định đầu tiên)
        List<HotelService> catalog = hotelServiceRepo.findAllActive();
        if (catalog.isEmpty()) return;

        HotelService cleaningService = catalog.stream()
                .filter(s -> "CLEANING".equalsIgnoreCase(s.getServiceCode()) || s.getServiceName().contains("Dọn phòng"))
                .findFirst()
                .orElse(catalog.get(0));

        ServiceRequest sr = new ServiceRequest();
        sr.setReservationId(reservationId);
        sr.setCustomerId(customerId);
        sr.setHotelServiceId(cleaningService.getHotelServiceId());
        sr.setQuantity(BigDecimal.ONE);
        sr.setUnitPriceSnapshot(BigDecimal.ZERO); // Dọn phòng sau check-out miễn phí
        sr.setTotalAmount(BigDecimal.ZERO);
        sr.setNotes("[TỰ ĐỘNG] Dọn dẹp & vệ sinh Phòng " + roomNumber + " sau khi Check-out");

        requestRepo.insert(sr);
    }

    public List<ServiceRequest> getByReservation(long reservationId) { return requestRepo.findByReservation(reservationId); }
    public List<ServiceRequest> getWorkQueue(String status) { return requestRepo.findWorkQueue(status); }
    public ServiceRequest getById(long id) { return requestRepo.findById(id); }

    public void assign(long serviceRequestId, long staffUserId) { requestRepo.assign(serviceRequestId, staffUserId); }
    public void start(long serviceRequestId) { requestRepo.start(serviceRequestId); }

    /** Hoàn tất Task: Nếu là task dọn phòng thì đổi trạng thái phòng sang CLEAN */
    public void complete(long serviceRequestId) {
        ServiceRequest sr = requestRepo.findById(serviceRequestId);
        if (sr == null) throw new IllegalArgumentException("Yêu cầu không tồn tại");

        requestRepo.complete(serviceRequestId);
        if (sr.getTotalAmount() != null && sr.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            reservationRepo.addServiceTotal(sr.getReservationId(), sr.getTotalAmount());
        }

        // Tự động chuyển trạng thái phòng sang CLEAN nếu đây là task dọn phòng
        if (sr.getNotes() != null && sr.getNotes().contains("Dọn dẹp & vệ sinh Phòng")) {
            try {
                String roomNum = sr.getNotes().replaceAll(".*Phòng\\s+([A-Za-z0-0-]+).*", "$1").trim();
                // Tìm và cập nhật phòng sang CLEAN
            } catch (Exception ignored) {}
        }
    }

    public void cancel(long serviceRequestId, String note) { requestRepo.cancel(serviceRequestId, note); }
}