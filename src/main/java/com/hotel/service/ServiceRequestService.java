package com.hotel.service;

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

/** F15 - Khách yêu cầu dịch vụ, F16 - Nhân viên xử lý yêu cầu */
public class ServiceRequestService {

    private final IHotelServiceRepository hotelServiceRepo = new HotelServiceRepository();
    private final IServiceRequestRepository requestRepo = new ServiceRequestRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();
    private final IUserRepository userRepo = new UserRepository();

    /** F15: danh mục dịch vụ đang mở */
    public List<HotelService> getCatalog() { return hotelServiceRepo.findAllActive(); }

    /** F15: tạo yêu cầu dịch vụ (khách đang ở mới được yêu cầu) */
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

    /** Nhân viên GENERAL_SERVICE đang hoạt động, ưu tiên người có ít việc nhất. */
    public List<User> getAssignableStaff() {
        return userRepo.findActiveServiceStaffWithWorkload("GENERAL_SERVICE");
    }

    /** F16: phân công sau khi xác thực đúng vai trò, bộ phận và trạng thái nhân viên. */
    public void assign(long serviceRequestId, long staffUserId) {
        User staff = userRepo.findById(staffUserId);
        if (!isActiveGeneralServiceStaff(staff))
            throw new IllegalArgumentException(
                    "Người được phân công phải là nhân viên dịch vụ (GENERAL_SERVICE) đang hoạt động");
        if (requestRepo.findById(serviceRequestId) == null)
            throw new IllegalArgumentException("Yêu cầu không tồn tại");
        requestRepo.assign(serviceRequestId, staffUserId);
    }

    /** Tự động giao cho nhân viên active đang có ít việc nhất. */
    public void assignAuto(long serviceRequestId) {
        List<User> staff = getAssignableStaff();
        if (staff.isEmpty())
            throw new IllegalStateException("Không có nhân viên dịch vụ nào đang hoạt động để phân công");
        assign(serviceRequestId, staff.get(0).getUserId());
    }

    /** Nhân viên GENERAL_SERVICE tự nhận một yêu cầu còn PENDING. */
    public void selfClaim(long serviceRequestId, User me) {
        if (!isActiveGeneralServiceStaff(me))
            throw new IllegalArgumentException(
                    "Người được phân công phải là nhân viên dịch vụ (GENERAL_SERVICE) đang hoạt động");
        ServiceRequest request = requireRequest(serviceRequestId);
        if (!Constants.SR_PENDING.equals(request.getStatusCode()))
            throw new IllegalStateException("Chỉ được tự nhận yêu cầu đang ở trạng thái PENDING");
        assign(serviceRequestId, me.getUserId());
    }

    /** F16: bắt đầu thực hiện */
    public void start(long serviceRequestId) { requestRepo.start(serviceRequestId); }

    /** Bắt đầu việc; SERVICE_STAFF chỉ thao tác việc được giao cho chính mình. */
    public void start(long serviceRequestId, User me) {
        requireOwnTaskForServiceStaff(serviceRequestId, me);
        requestRepo.start(serviceRequestId);
    }

    /** F16: hoàn tất - cộng tiền dịch vụ vào tổng của đơn (chi tiết sẽ lên hóa đơn ở F14) */
    public void complete(long serviceRequestId) {
        ServiceRequest sr = requestRepo.findById(serviceRequestId);
        if (sr == null) throw new IllegalArgumentException("Yêu cầu không tồn tại");
        requestRepo.complete(serviceRequestId);
        reservationRepo.addServiceTotal(sr.getReservationId(), sr.getTotalAmount());
    }

    /** Hoàn tất việc; SERVICE_STAFF chỉ thao tác việc được giao cho chính mình. */
    public void complete(long serviceRequestId, User me) {
        requireOwnTaskForServiceStaff(serviceRequestId, me);
        complete(serviceRequestId);
    }

    /** F16: hủy yêu cầu */
    public void cancel(long serviceRequestId, String note) { requestRepo.cancel(serviceRequestId, note); }

    private ServiceRequest requireRequest(long serviceRequestId) {
        ServiceRequest request = requestRepo.findById(serviceRequestId);
        if (request == null) throw new IllegalArgumentException("Yêu cầu không tồn tại");
        return request;
    }

    private void requireOwnTaskForServiceStaff(long serviceRequestId, User me) {
        ServiceRequest request = requireRequest(serviceRequestId);
        if (Constants.ROLE_SERVICE_STAFF.equals(me.getRoleCode())
                && (request.getAssignedStaffUserId() == null
                || request.getAssignedStaffUserId() != me.getUserId()))
            throw new IllegalStateException("Bạn chỉ được thao tác yêu cầu được phân công cho mình");
    }

    private boolean isActiveGeneralServiceStaff(User user) {
        return user != null
                && Constants.ROLE_SERVICE_STAFF.equals(user.getRoleCode())
                && "ACTIVE".equals(user.getStatusCode())
                && "GENERAL_SERVICE".equals(user.getDepartmentCode());
    }
}
