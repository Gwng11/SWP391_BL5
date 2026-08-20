package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** F15 - Danh mục dịch vụ phía Khách hàng (Catalog & Search) */
@WebServlet(urlPatterns = {"/services"})
public class ServiceController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("q");
        req.setAttribute("catalog", serviceRequestService.searchCatalog(keyword));
        req.setAttribute("keyword", keyword);

        Customer c = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
        if (c != null) {
            List<Reservation> active = reservationService.getByCustomer(c.getCustomerId()).stream()
                    .filter(r -> Constants.RES_CHECKED_IN.equals(r.getStatusCode()))
                    .collect(java.util.stream.Collectors.toList());
            req.setAttribute("activeReservations", active);
        }
        req.getRequestDispatcher("/WEB-INF/views/services.jsp").forward(req, resp);
    }
}
