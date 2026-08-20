package com.hotel.controller;

import com.hotel.entity.HotelService;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.repository.HotelServiceRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Quản lý danh mục dịch vụ dành cho Manager (CRUD + Ảnh image_url + Bật/Tắt) */
@WebServlet(urlPatterns = {"/manager/services"})
public class ManagerServiceController extends BaseController {

    private final IHotelServiceRepository hotelServiceRepo = new HotelServiceRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<HotelService> services = hotelServiceRepo.findAll();
        req.setAttribute("services", services);

        Long editId = longParamOrNull(req, "id");
        if (editId != null) {
            req.setAttribute("editService", hotelServiceRepo.findById(editId));
        }

        req.getRequestDispatcher("/WEB-INF/views/service-management.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                HotelService s = new HotelService();
                s.setServiceCode(req.getParameter("serviceCode"));
                s.setServiceName(req.getParameter("serviceName"));
                s.setDescription(req.getParameter("description"));
                s.setUnitName(req.getParameter("unitName"));
                s.setUnitPrice(decimalParam(req, "unitPrice"));
                s.setImageUrl(req.getParameter("imageUrl"));
                hotelServiceRepo.insert(s);
                resp.sendRedirect(req.getContextPath() + "/manager/services?ok=created");

            } else if ("update".equals(action)) {
                HotelService s = new HotelService();
                s.setHotelServiceId(longParam(req, "hotelServiceId"));
                s.setServiceCode(req.getParameter("serviceCode"));
                s.setServiceName(req.getParameter("serviceName"));
                s.setDescription(req.getParameter("description"));
                s.setUnitName(req.getParameter("unitName"));
                s.setUnitPrice(decimalParam(req, "unitPrice"));
                s.setImageUrl(req.getParameter("imageUrl"));
                hotelServiceRepo.update(s);
                resp.sendRedirect(req.getContextPath() + "/manager/services?ok=updated");

            } else if ("toggle".equals(action)) {
                long id = longParam(req, "hotelServiceId");
                boolean active = Boolean.parseBoolean(req.getParameter("active"));
                hotelServiceRepo.toggleActive(id, active);
                resp.sendRedirect(req.getContextPath() + "/manager/services?ok=toggled");
            }
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/manager/services?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}