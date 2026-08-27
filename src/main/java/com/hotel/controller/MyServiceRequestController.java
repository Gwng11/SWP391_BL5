package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.ServiceRequest;
import com.hotel.entity.User;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/** Màn hình Customer theo dõi chi phí và tiến độ các yêu cầu dịch vụ của chính mình. */
@WebServlet(urlPatterns = {"/my-service-requests"})
public class MyServiceRequestController extends BaseController {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            Constants.SR_PENDING, Constants.SR_ASSIGNED, Constants.SR_IN_PROGRESS,
            Constants.SR_COMPLETED, Constants.SR_CANCELLED);

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = currentUser(req);
        if (me == null || !Constants.ROLE_CUSTOMER.equals(me.getRoleCode())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Customer customer = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
        try {
            List<ServiceRequest> allRequests = serviceRequestService.getCustomerRequests(me, customer);
            String status = req.getParameter("status");
            String statusFilter = normalizeStatusFilter(status);
            List<ServiceRequest> visibleRequests = statusFilter == null ? allRequests : allRequests.stream()
                    .filter(r -> statusFilter.equals(r.getStatusCode()))
                    .toList();

            BigDecimal completedAmount = allRequests.stream()
                    .filter(r -> Constants.SR_COMPLETED.equals(r.getStatusCode()))
                    .map(ServiceRequest::getTotalAmount)
                    .filter(value -> value != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long activeCount = allRequests.stream()
                    .filter(r -> Constants.SR_PENDING.equals(r.getStatusCode())
                            || Constants.SR_ASSIGNED.equals(r.getStatusCode())
                            || Constants.SR_IN_PROGRESS.equals(r.getStatusCode()))
                    .count();
            long completedCount = allRequests.stream()
                    .filter(r -> Constants.SR_COMPLETED.equals(r.getStatusCode()))
                    .count();

            req.setAttribute("requests", visibleRequests);
            req.setAttribute("totalRequestCount", allRequests.size());
            req.setAttribute("activeRequestCount", activeCount);
            req.setAttribute("completedRequestCount", completedCount);
            req.setAttribute("completedAmount", completedAmount);
            req.setAttribute("statusFilter", statusFilter);
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("err", e.getMessage());
            req.setAttribute("requests", List.of());
        }
        req.getRequestDispatcher("/WEB-INF/views/my-service-requests.jsp").forward(req, resp);
    }

    static String normalizeStatusFilter(String status) {
        return status != null && ALLOWED_STATUSES.contains(status) ? status : null;
    }
}
