package com.hotel.interfaces;

import com.hotel.entity.ServiceRequest;
import java.util.List;

public interface IServiceRequestRepository {
    long insert(ServiceRequest sr);
    ServiceRequest findById(long serviceRequestId);
    List<ServiceRequest> findByReservation(long reservationId);
    /** F16: hàng đợi công việc; statusCode = null → tất cả trạng thái chưa hủy */
    List<ServiceRequest> findWorkQueue(String statusCode);
    void assign(long serviceRequestId, long staffUserId);
    void start(long serviceRequestId);
    void complete(long serviceRequestId);
    void cancel(long serviceRequestId, String note);
    /** F14: các yêu cầu COMPLETED chưa được đưa vào hóa đơn */
    List<ServiceRequest> findCompletedNotInvoiced(long reservationId);
}
