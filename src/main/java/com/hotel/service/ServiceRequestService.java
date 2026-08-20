package com.hotel.service;

import com.hotel.entity.HotelService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ServiceRequest;
import com.hotel.entity.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceRequestService {

    private final IHotelServiceRepository hotelServiceRepo = new HotelServiceRepository();
    private final IServiceRequestRepository requestRepo = new ServiceRequestRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final IRoomRepository roomRepo = new RoomRepository();

    // ==========================================
    // CÁC HÀM CŨ GIỮ NGUYÊN
    // ==========================================

    public List<HotelService> getCatalog() {
        return hotelServiceRepo.findAllActive();
    }

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

    public void createHousekeepingTask(long reservationId, long customerId, String roomNumber) {
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
        sr.setUnitPriceSnapshot(BigDecimal.ZERO);
        sr.setTotalAmount(BigDecimal.ZERO);
        sr.setNotes("[TỰ ĐỘNG] Dọn dẹp & vệ sinh Phòng " + roomNumber + " sau khi Check-out");

        requestRepo.insert(sr);
    }

    public List<ServiceRequest> getByReservation(long reservationId) {
        return requestRepo.findByReservation(reservationId);
    }

    public List<ServiceRequest> getWorkQueue(String status) {
        return requestRepo.findWorkQueue(status);
    }

    public ServiceRequest getById(long id) {
        return requestRepo.findById(id);
    }


    public void assign(long serviceRequestId, long staffUserId) {
        requestRepo.assign(serviceRequestId, staffUserId);
    }

    public void start(long serviceRequestId) {
        requestRepo.start(serviceRequestId);
    }

    public void complete(long serviceRequestId) {
        ServiceRequest sr = requestRepo.findById(serviceRequestId);
        if (sr == null) throw new IllegalArgumentException("Yêu cầu không tồn tại");

        requestRepo.complete(serviceRequestId);
        if (sr.getTotalAmount() != null && sr.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            reservationRepo.addServiceTotal(sr.getReservationId(), sr.getTotalAmount());
        }

        if (sr.getNotes() != null && sr.getNotes().contains("Dọn dẹp & vệ sinh Phòng")) {
            try {
                String roomNum = sr.getNotes().replaceAll(".*Phòng\\s+([A-Za-z0-9-]+).*", "$1").trim();
                // Tìm và cập nhật phòng sang CLEAN
            } catch (Exception ignored) {}
        }
    }

    public void cancel(long serviceRequestId, String note) {
        requestRepo.cancel(serviceRequestId, note);
    }

    // ==========================================
    // CÁC HÀM BỔ SUNG ĐỂ SỬA LỖI CONTROLLER
    // ==========================================

    /** Thiếu ở ServiceController dòng 25 */
    public List<HotelService> searchCatalog(String keyword) {
        List<HotelService> allActive = hotelServiceRepo.findAllActive();
        if (keyword == null || keyword.trim().isEmpty()) {
            return allActive;
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return allActive.stream()
                .filter(s -> s.getServiceName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /** Thiếu ở ServiceDetailController dòng 27 */
    public HotelService getServiceById(long id) {
        return hotelServiceRepo.findById(id);
    }

    /** Thiếu ở ServiceTaskController dòng 34 */
    public List<User> getAssignableStaff() {
        // Tạm thời trả về list rỗng nếu chưa có StaffRepository
        return new ArrayList<>();
    }

    /** Thiếu ở ServiceTaskController dòng 50 */
    public void assignAuto(long serviceRequestId) {
        // Logic tự động gán nhân viên (có thể để trống tạm thời)
    }

    /** Thiếu ở ServiceTaskController dòng 52 */
    public void selfClaim(long serviceRequestId, User user) {
        if (user != null) {
            assign(serviceRequestId, user.getUserId());
        }
    }

    /** Thiếu ở ServiceTaskController dòng 54 (Lỗi khác tham số) */
    public void start(long serviceRequestId, User user) {
        // Gọi lại hàm start cũ
        start(serviceRequestId);
    }

    /** Thiếu ở ServiceTaskController dòng 56 (Lỗi khác tham số) */
    public void complete(long serviceRequestId, User user) {
        // Gọi lại hàm complete cũ
        complete(serviceRequestId);
    }
}