package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.HotelService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ServiceRequest;
import com.hotel.entity.User;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IServiceRequestRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.repository.HotelServiceRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ServiceRequestRepository;
import com.hotel.repository.UserRepository;
import com.hotel.ultis.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** Nghiệp vụ F15/F16 cho yêu cầu dịch vụ khách sạn. */
public class ServiceRequestService {
    private static final String SERVICE_DEPARTMENT = "GENERAL_SERVICE";

    private final IHotelServiceRepository hotelServiceRepo;
    private final IServiceRequestRepository requestRepo;
    private final IReservationRepository reservationRepo;
    private final IUserRepository userRepo;

    public ServiceRequestService() {
        this(new HotelServiceRepository(), new ServiceRequestRepository(),
                new ReservationRepository(), new UserRepository());
    }

    public ServiceRequestService(IHotelServiceRepository hotelServiceRepo,
                                 IServiceRequestRepository requestRepo,
                                 IReservationRepository reservationRepo,
                                 IUserRepository userRepo) {
        this.hotelServiceRepo = hotelServiceRepo;
        this.requestRepo = requestRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
    }

    public List<HotelService> getCatalog(String keyword) {
        return keyword == null || keyword.isBlank()
                ? hotelServiceRepo.findAllActive()
                : hotelServiceRepo.searchActive(keyword.trim());
    }

    public HotelService getActiveService(long id) {
        HotelService service = hotelServiceRepo.findById(id);
        return service != null && service.isActive() ? service : null;
    }

    /**
     * Customer dùng kỳ ở CHECKED_IN duy nhất của chính mình. Receptionist nhận
     * reservationId từ màn hình kỳ ở; người dùng không phải chọn lại trong form dịch vụ.
     */
    public Reservation resolveCurrentStay(User actor, Customer customer, Long reservationContextId) {
        requireRequestActor(actor);
        if (Constants.ROLE_CUSTOMER.equals(actor.getRoleCode())) {
            if (customer == null) throw new IllegalStateException("Không tìm thấy hồ sơ khách hàng hiện tại");
            List<Reservation> stays = reservationRepo.findByCustomer(customer.getCustomerId()).stream()
                    .filter(r -> Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
                    .toList();
            if (stays.isEmpty()) throw new IllegalStateException("Bạn chưa có kỳ lưu trú đang hoạt động");
            if (stays.size() > 1) {
                throw new IllegalStateException("Không thể xác định duy nhất kỳ lưu trú; vui lòng liên hệ lễ tân");
            }
            return stays.get(0);
        }

        if (reservationContextId == null) {
            throw new IllegalStateException("Vui lòng mở danh mục dịch vụ từ màn hình kỳ lưu trú");
        }
        Reservation stay = reservationRepo.findById(reservationContextId);
        if (stay == null || !Constants.RES_CHECKED_IN.equals(stay.getStatusCode())) {
            throw new IllegalStateException("Kỳ lưu trú không tồn tại hoặc không còn hoạt động");
        }
        return stay;
    }

    public long createRequest(User actor, Customer customer, Long reservationContextId,
                              long hotelServiceId, BigDecimal quantity,
                              LocalDateTime requestedForAt, String notes) {
        Reservation stay = resolveCurrentStay(actor, customer, reservationContextId);
        HotelService service = getActiveService(hotelServiceId);
        if (service == null) throw new IllegalArgumentException("Dịch vụ không tồn tại hoặc đã ngừng phục vụ");
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (quantity.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("Số lượng vượt quá giới hạn cho phép");
        }
        if (requestedForAt == null) throw new IllegalArgumentException("Vui lòng chọn thời gian mong muốn");
        if (requestedForAt.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Thời gian mong muốn không được ở trong quá khứ");
        }
        String cleanNotes = notes == null ? null : notes.trim();
        if (cleanNotes != null && cleanNotes.length() > 500) {
            throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự");
        }

        ServiceRequest request = new ServiceRequest();
        request.setReservationId(stay.getReservationId());
        request.setCustomerId(stay.getCustomerId());
        request.setHotelServiceId(service.getHotelServiceId());
        request.setQuantity(quantity);
        request.setUnitPriceSnapshot(service.getUnitPrice());
        request.setTotalAmount(service.getUnitPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP));
        request.setRequestedForAt(requestedForAt);
        request.setNotes(cleanNotes == null || cleanNotes.isEmpty() ? null : cleanNotes);
        return requestRepo.insert(request);
    }

    public List<ServiceRequest> getRequestsFor(User actor, String status) {
        if (actor == null) throw new IllegalStateException("Vui lòng đăng nhập");
        if (Constants.ROLE_RECEPTIONIST.equals(actor.getRoleCode())) {
            return requestRepo.findWorkQueue(status);
        }
        if (Constants.ROLE_SERVICE_STAFF.equals(actor.getRoleCode())) {
            return requestRepo.findAssignedToStaff(actor.getUserId(), status);
        }
        throw new IllegalStateException("Bạn không có quyền xem yêu cầu dịch vụ");
    }

    public List<User> getAssignableStaff() {
        return userRepo.findActiveServiceStaffWithWorkload(SERVICE_DEPARTMENT);
    }

    public void assign(long serviceRequestId, long staffUserId) {
        boolean eligible = getAssignableStaff().stream().anyMatch(u -> u.getUserId() == staffUserId);
        if (!eligible) throw new IllegalArgumentException("Nhân viên không thuộc bộ phận dịch vụ hoặc không hoạt động");
        requestRepo.assign(serviceRequestId, staffUserId);
    }

    public void assignAuto(long serviceRequestId) {
        List<User> staff = getAssignableStaff();
        if (staff.isEmpty()) throw new IllegalStateException("Không có nhân viên dịch vụ đang hoạt động");
        requestRepo.assign(serviceRequestId, staff.get(0).getUserId());
    }

    public void start(long serviceRequestId, User actor) {
        requireServiceStaff(actor);
        requestRepo.start(serviceRequestId, actor.getUserId());
    }

    public void complete(long serviceRequestId, User actor) {
        requireServiceStaff(actor);
        requestRepo.completeAndAddCharge(serviceRequestId, actor.getUserId());
    }

    public void reportUnable(long serviceRequestId, User actor, String reason) {
        requireServiceStaff(actor);
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Vui lòng nhập lý do không thể thực hiện");
        if (reason.trim().length() > 400) throw new IllegalArgumentException("Lý do không được vượt quá 400 ký tự");
        requestRepo.reportUnable(serviceRequestId, actor.getUserId(), reason.trim());
    }

    public void cancel(long serviceRequestId, String note) {
        requestRepo.cancel(serviceRequestId, note);
    }

    public void reschedule(long serviceRequestId, LocalDateTime requestedForAt, String note) {
        if (requestedForAt == null || requestedForAt.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Thời gian phục vụ mới không hợp lệ");
        }
        requestRepo.reschedule(serviceRequestId, requestedForAt, note);
    }

    public List<ServiceRequest> getByReservation(long reservationId) {
        return requestRepo.findByReservation(reservationId);
    }

    /** Giữ tương thích tạm thời với luồng checkout hiện hữu. */
    public void createHousekeepingTask(long reservationId, long customerId, String roomNumber) {
        List<HotelService> catalog = hotelServiceRepo.findAllActive();
        if (catalog.isEmpty()) return;
        HotelService service = catalog.stream()
                .filter(s -> "CLEANING".equalsIgnoreCase(s.getServiceCode()))
                .findFirst().orElse(catalog.get(0));
        ServiceRequest request = new ServiceRequest();
        request.setReservationId(reservationId);
        request.setCustomerId(customerId);
        request.setHotelServiceId(service.getHotelServiceId());
        request.setQuantity(BigDecimal.ONE);
        request.setUnitPriceSnapshot(BigDecimal.ZERO);
        request.setTotalAmount(BigDecimal.ZERO);
        request.setRequestedForAt(LocalDateTime.now().plusMinutes(1));
        request.setNotes("[TỰ ĐỘNG] Dọn phòng " + roomNumber + " sau check-out");
        requestRepo.insert(request);
    }

    private void requireRequestActor(User actor) {
        if (actor == null || !(Constants.ROLE_CUSTOMER.equals(actor.getRoleCode())
                || Constants.ROLE_RECEPTIONIST.equals(actor.getRoleCode()))) {
            throw new IllegalStateException("Chỉ khách hàng hoặc lễ tân được yêu cầu dịch vụ");
        }
    }

    private void requireServiceStaff(User actor) {
        if (actor == null || !Constants.ROLE_SERVICE_STAFF.equals(actor.getRoleCode())) {
            throw new IllegalStateException("Chỉ nhân viên dịch vụ được xử lý yêu cầu đã phân công");
        }
    }
}
