package com.hotel.controller;

import com.hotel.entity.EmailTemplate;
import com.hotel.service.AdminService;
import com.hotel.ultis.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/admin/templates" })
public class AdminTemplateController extends BaseController {

    private final AdminService adminService;

    public AdminTemplateController() {
        this(new AdminService());
    }

    public AdminTemplateController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<EmailTemplate> templates = adminService.listTemplates();
        req.setAttribute("templates", templates);

        String editIdStr = req.getParameter("edit");
        if (editIdStr != null && !editIdStr.isEmpty()) {
            try {
                long editId = Long.parseLong(editIdStr);
                EmailTemplate editTemplate = adminService.getTemplate(editId);
                req.setAttribute("editTemplate", editTemplate);
            } catch (Exception ignored) {}
        }

        req.setAttribute("events", List.of(
            Constants.EV_ACCOUNT_VERIFICATION,
            Constants.EV_PASSWORD_RESET,
            Constants.EV_RESERVATION_CONFIRMED,
            Constants.EV_RESERVATION_UPDATED,
            Constants.EV_RESERVATION_CANCELLED,
            Constants.EV_DEPOSIT_RECEIPT,
            Constants.EV_INVOICE_AND_RECEIPT
        ));

        req.getRequestDispatcher("/WEB-INF/views/admin-templates.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                long emailTemplateId = longParam(req, "emailTemplateId");
                adminService.deleteTemplate(emailTemplateId);
                redirect(req, resp, "/admin/templates", "msg", "Xóa mẫu email thành công!");
                return;
            }

            long emailTemplateId = intParam(req, "emailTemplateId", 0);
            String templateCode = req.getParameter("templateCode");
            String templateName = req.getParameter("templateName");
            String eventCode = req.getParameter("eventCode");
            String subjectTemplate = req.getParameter("subjectTemplate");
            String bodyHtml = req.getParameter("bodyHtml");
            boolean active = Boolean.parseBoolean(req.getParameter("active"));

            EmailTemplate t = new EmailTemplate();
            t.setEmailTemplateId(emailTemplateId);
            t.setTemplateCode(templateCode);
            t.setTemplateName(templateName);
            t.setEventCode(eventCode);
            t.setSubjectTemplate(subjectTemplate);
            t.setBodyHtml(bodyHtml);
            t.setBodyText(""); // simple text not used or keep blank
            t.setActive(active);

            adminService.saveTemplate(t);
            redirect(req, resp, "/admin/templates", "msg", "Lưu mẫu email thành công!");
        } catch (IllegalArgumentException e) {
            req.setAttribute("err", e.getMessage());
            doGet(req, resp);
        } catch (Exception e) {
            req.setAttribute("err", Constants.MSG_SYSTEM_ERROR);
            doGet(req, resp);
        }
    }
}
