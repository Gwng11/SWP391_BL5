package com.hotel.controller;

import com.hotel.entity.HotelService;
import com.hotel.service.HotelServiceManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Quản lý danh mục dịch vụ dành cho Manager (CRUD + Ảnh image_url + Bật/Tắt + Tìm kiếm) */
@WebServlet(urlPatterns = {"/manager/services"})
public class ManagerServiceController extends BaseController {

    private final HotelServiceManagementService hotelServiceService = new HotelServiceManagementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("q");
        List<HotelService> services = hotelServiceService.search(keyword);

        // 3. Đưa biến ra JSP
        req.setAttribute("services", services);
        req.setAttribute("keyword", keyword);

        Long editId = longParamOrNull(req, "id");
        if (editId != null) {
            req.setAttribute("editService", hotelServiceService.findById(editId));
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
                hotelServiceService.create(s);
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
                hotelServiceService.update(s);
                resp.sendRedirect(req.getContextPath() + "/manager/services?ok=updated");

            } else if ("toggle".equals(action)) {
                long id = longParam(req, "hotelServiceId");
                boolean active = Boolean.parseBoolean(req.getParameter("active"));
                hotelServiceService.setActive(id, active);
                resp.sendRedirect(req.getContextPath() + "/manager/services?ok=toggled");
            } else if ("delete".equals(action)) {
                hotelServiceService.deleteIfSafe(longParam(req, "hotelServiceId"));
                resp.sendRedirect(req.getContextPath() + "/manager/services?ok=deleted");
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/manager/services?err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
