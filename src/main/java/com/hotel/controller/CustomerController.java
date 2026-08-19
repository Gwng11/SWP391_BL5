package com.hotel.controller;

import com.hotel.entity.Customer;
import com.hotel.entity.User;
import com.hotel.service.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F09 - Lễ tân quản lý khách hàng: tìm kiếm, tạo walk-in, cập nhật */
@WebServlet(urlPatterns = {"/reception/customers"})
public class CustomerController extends BaseController {

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String kw = req.getParameter("q");
        if (kw != null) req.setAttribute("customers", customerService.search(kw));
        Long editId = longParamOrNull(req, "edit");
        if (editId != null) req.setAttribute("editing", customerService.getById(editId));
        req.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        try {
            Customer c = new Customer();
            Long id = longParamOrNull(req, "customerId");
            c.setFullName(req.getParameter("fullName"));
            c.setEmail(emptyToNull(req.getParameter("email")));
            c.setPhone(emptyToNull(req.getParameter("phone")));
            c.setDateOfBirth(dateParam(req, "dateOfBirth"));
            c.setIdDocumentType(emptyToNull(req.getParameter("idDocumentType")));
            c.setIdDocumentNumber(emptyToNull(req.getParameter("idDocumentNumber")));
            c.setNationality(emptyToNull(req.getParameter("nationality")));
            c.setAddress(emptyToNull(req.getParameter("address")));
            if (id == null) {
                customerService.createWalkIn(c, me.getUserId());
            } else {
                Customer existing = customerService.getById(id);
                c.setCustomerId(id);
                c.setStatusCode(existing.getStatusCode());
                customerService.update(c);
            }
            resp.sendRedirect(req.getContextPath() + "/reception/customers?q="
                    + java.net.URLEncoder.encode(c.getFullName(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("err", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(req, resp);
        }
    }

    private String emptyToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
