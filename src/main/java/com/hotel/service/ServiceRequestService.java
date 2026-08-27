package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.HotelService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ServiceRequest;
import com.hotel.entity.User;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.interfaces.IHotelProfileRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IServiceRequestRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.repository.HotelServiceRepository;
import com.hotel.repository.HotelProfileRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.ServiceRequestRepository;
import com.hotel.repository.UserRepository;
import com.hotel.ultis.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Nghiệp vụ F15/F16 cho yêu cầu dịch vụ khách sạn. */
public class ServiceRequestService {
    private static final String SERVICE_DEPARTMENT = "GENERAL_SERVICE";

    private final IHotelServiceRepository hotelServiceRepo;
    private final IHotelProfileRepository hotelProfileRepo;
    private final IServiceRequestRepository requestRepo;
    private final IReservationRepository reservationRepo;
    private final IUserRepository userRepo;

    public ServiceRequestService() {
        this(new HotelServiceRepository(), new ServiceRequestRepository(),
                new ReservationRepository(), new UserRepository(), new HotelProfileRepository());
    }

    public ServiceRequestService(IHotelServiceRepository hotelServiceRepo,
                                 IServiceRequestRepository requestRepo,
                                 IReservationRepository reservationRepo,
                                 IUserRepository userRepo,
                                 IHotelProfileRepository hotelProfileRepo) {
        this.hotelServiceRepo = hotelServiceRepo;
        this.requestRepo = requestRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
        this.hotelProfileRepo = hotelProfileRepo;
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
     * Customer dùng kỳ ở CHECKED_IN hiện tại; nếu chưa check-in thì dùng đơn
     * CONFIRMED duy nhất của chính mình. Receptionist nhận reservationId từ màn
     * hình kỳ ở; người dùng không phải chọn lại trong form dịch vụ.
     */
    public Reservation resolveCurrentStay(User actor, Customer customer, Long reservationContextId) {
        requireRequestActor(actor);
        if (Constants.ROLE_CUSTOMER.equals(actor.getRoleCode())) {
            if (customer == null) throw new IllegalStateException("Không tìm thấy hồ sơ khách hàng hiện tại");
            if (reservationContextId != null) {
                Reservation selected = reservationRepo.findById(reservationContextId);
                if (selected == null || selected.getCustomerId() != customer.getCustomerId()) {
                    throw new IllegalStateException("Bạn không có quyền đặt dịch vụ cho đơn này");
                }
                if (!isServiceEligibleReservation(selected)) {
                    throw new IllegalStateException("Đơn không còn trong thời gian có thể đặt dịch vụ");
                }
                return selected;
            }
            List<Reservation> eligible = reservationRepo.findByCustomer(customer.getCustomerId()).stream()
                    .filter(this::isServiceEligibleReservation)
                    .toList();
            List<Reservation> activeStays = eligible.stream()
                    .filter(r -> Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
                    .toList();
            if (activeStays.size() == 1) return activeStays.get(0);
            if (activeStays.size() > 1) {
                throw new IllegalStateException("Không thể xác định duy nhất kỳ lưu trú đang hoạt động; vui lòng liên hệ lễ tân");
            }
            List<Reservation> confirmed = eligible.stream()
                    .filter(r -> Constants.RES_CONFIRMED.equals(r.getStatusCode()))
                    .toList();
            if (confirmed.isEmpty()) {
                throw new IllegalStateException("Bạn chưa có đơn đã xác nhận hoặc kỳ lưu trú đang hoạt động");
            }
            if (confirmed.size() > 1) {
                throw new IllegalStateException("Bạn có nhiều đơn đã xác nhận; vui lòng liên hệ lễ tân để yêu cầu dịch vụ cho đúng đơn");
            }
            return confirmed.get(0);
        }

        if (reservationContextId == null) {
            throw new IllegalStateException("Vui lòng mở danh mục dịch vụ từ màn hình kỳ lưu trú");
        }
        Reservation stay = reservationRepo.findById(reservationContextId);
        if (stay == null || !isServiceEligibleReservation(stay)) {
            throw new IllegalStateException("Đơn đặt phòng không tồn tại, chưa xác nhận hoặc không còn hoạt động");
        }
        return stay;
    }

    private boolean isServiceEligibleReservation(Reservation reservation) {
        return reservation != null
                && (Constants.RES_CONFIRMED.equals(reservation.getStatusCode())
                    || Constants.RES_CHECKED_IN.equals(reservation.getStatusCode()))
                && reservation.getCheckInDate() != null
                && reservation.getCheckOutDate() != null
                && LocalDateTime.now().isBefore(serviceWindowEnd(reservation));
    }

    public boolean canRequestService(Reservation reservation) {
        return isServiceEligibleReservation(reservation);
    }

    public LocalDateTime serviceWindowStart(Reservation reservation) {
        if (reservation == null || reservation.getCheckInDate() == null) {
            throw new IllegalStateException("Thiếu ngày nhận phòng của đơn");
        }
        var profile = hotelProfileRepo.getProfile();
        LocalTime checkInTime = profile != null && profile.getCheckInTime() != null
                ? profile.getCheckInTime() : LocalTime.of(14, 0);
        return reservation.getCheckInDate().atTime(checkInTime);
    }

    public LocalDateTime serviceWindowEnd(Reservation reservation) {
        if (reservation == null || reservation.getCheckOutDate() == null) {
            throw new IllegalStateException("Thiếu ngày trả phòng của đơn");
        }
        var profile = hotelProfileRepo.getProfile();
        LocalTime checkOutTime = profile != null && profile.getCheckOutTime() != null
                ? profile.getCheckOutTime() : LocalTime.NOON;
        return reservation.getCheckOutDate().atTime(checkOutTime);
    }

    public long createRequest(User actor, Customer customer, Long reservationContextId,
                              long hotelServiceId, BigDecimal quantity,
                              LocalDateTime requestedForAt, String notes) {
        requireRequestActor(actor);
        if (reservationContextId == null) {
            throw new IllegalStateException("Vui lòng chọn đúng kỳ lưu trú từ màn hình Đơn của tôi trước khi đặt dịch vụ");
        }
        Reservation stay = resolveCurrentStay(actor, customer, reservationContextId);
        HotelService service = getActiveService(hotelServiceId);
        if (service == null) throw new IllegalArgumentException("Dịch vụ không tồn tại hoặc đã ngừng phục vụ");
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (quantity.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("Số lượng vượt quá giới hạn cho phép");
        }
        validateRequestedTime(stay, requestedForAt);
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

    /** Danh sách yêu cầu thuộc đúng hồ sơ Customer đang đăng nhập. */
    public List<ServiceRequest> getCustomerRequests(User actor, Customer customer) {
        if (actor == null || !Constants.ROLE_CUSTOMER.equals(actor.getRoleCode())) {
            throw new IllegalStateException("Chỉ khách hàng được xem lịch sử dịch vụ của mình");
        }
        if (customer == null) {
            throw new IllegalStateException("Không tìm thấy hồ sơ khách hàng hiện tại");
        }
        return requestRepo.findByCustomer(customer.getCustomerId());
    }

    public List<User> getAssignableStaff() {
        return userRepo.findActiveServiceStaffWithWorkload(SERVICE_DEPARTMENT);
    }

    public void claim(long serviceRequestId, User actor) {
        requireServiceStaff(actor);
        requestRepo.claim(serviceRequestId, actor.getUserId());
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
        ServiceRequest request = requestRepo.findById(serviceRequestId);
        if (request == null) throw new IllegalArgumentException("Yêu cầu dịch vụ không tồn tại");
        Reservation stay = reservationRepo.findById(request.getReservationId());
        if (stay == null) throw new IllegalStateException("Không tìm thấy đơn lưu trú của yêu cầu");
        validateRequestedTime(stay, requestedForAt);
        requestRepo.reschedule(serviceRequestId, requestedForAt, note);
    }

    private void validateRequestedTime(Reservation stay, LocalDateTime requestedForAt) {
        if (requestedForAt == null) throw new IllegalArgumentException("Vui lòng chọn thời gian mong muốn");
        if (requestedForAt.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Thời gian mong muốn không được ở trong quá khứ");
        }
        LocalDateTime serviceWindowStart = serviceWindowStart(stay);
        LocalDateTime serviceWindowEnd = serviceWindowEnd(stay);
        if (requestedForAt.isBefore(serviceWindowStart) || !requestedForAt.isBefore(serviceWindowEnd)) {
            throw new IllegalArgumentException("Thời gian dịch vụ phải nằm trong thời gian lưu trú từ "
                    + serviceWindowStart + " đến trước " + serviceWindowEnd);
        }
    }

    public List<ServiceRequest> getByReservation(long reservationId) {
        return requestRepo.findByReservation(reservationId);
    }

    public void requireReadyForFinalInvoice(long reservationId) {
        boolean active = requestRepo.findByReservation(reservationId).stream()
                .anyMatch(r -> "PENDING".equals(r.getStatusCode())
                        || "ASSIGNED".equals(r.getStatusCode())
                        || "IN_PROGRESS".equals(r.getStatusCode()));
        if (active) {
            throw new IllegalStateException(
                    "Còn yêu cầu dịch vụ chưa hoàn tất hoặc chưa hủy; chưa thể phát hành hóa đơn cuối");
        }
    }

    public void requireReadyForCheckout(long reservationId) {
        requireReadyForFinalInvoice(reservationId);
        if (!requestRepo.findCompletedNotInvoiced(reservationId).isEmpty()) {
            throw new IllegalStateException(
                    "Còn dịch vụ đã hoàn tất chưa được đưa vào hóa đơn; chưa thể check-out");
        }
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
