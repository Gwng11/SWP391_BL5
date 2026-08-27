package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.service.ServiceRequestService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F15 - Danh mục dịch vụ phía Khách hàng (Catalog & Search) */
@WebServlet(urlPatterns = {"/services"})
public class ServiceController extends BaseController {

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("q");
        var me = currentUser(req);
        if (!(Constants.ROLE_CUSTOMER.equals(me.getRoleCode())
                || Constants.ROLE_RECEPTIONIST.equals(me.getRoleCode()))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        req.setAttribute("catalog", serviceRequestService.getCatalog(keyword));
        req.setAttribute("keyword", keyword);
        Long reservationId = longParamOrNull(req, "reservationId");
        if (Constants.ROLE_CUSTOMER.equals(me.getRoleCode()) && reservationId == null) {
            req.setAttribute("browseOnly", true);
            req.getRequestDispatcher("/WEB-INF/views/services.jsp").forward(req, resp);
            return;
        }
        try {
            Customer customer = (Customer) req.getSession().getAttribute(Constants.SESSION_CUSTOMER);
            req.setAttribute("currentStay", serviceRequestService.resolveCurrentStay(me, customer, reservationId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("stayError", e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/services.jsp").forward(req, resp);
    }
}
