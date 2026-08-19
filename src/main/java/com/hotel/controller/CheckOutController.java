package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.FrontDeskService;
import com.hotel.service.InvoiceService;
import com.hotel.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** F13 - Check-out: xem lại chi phí, xác nhận rời đi, phòng chuyển DIRTY */
@WebServlet(urlPatterns = {"/reception/checkout"})
public class CheckOutController extends BaseController {

    private final FrontDeskService frontDeskService = new FrontDeskService();
    private final ReservationService reservationService = new ReservationService();
    private final InvoiceService invoiceService = new InvoiceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("stays", frontDeskService.getActiveStays());
        Long id = longParamOrNull(req, "id");
        if (id != null) {
            req.setAttribute("r", reservationService.getById(id));
            req.setAttribute("rooms", reservationService.getRooms(id));
            var inv = invoiceService.getByReservation(id);
            req.setAttribute("invoice", inv);
            if (inv != null) req.setAttribute("items", invoiceService.getItems(inv.getInvoiceId()));
        }
        req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        long id = longParam(req, "id");
        try {
            frontDeskService.checkOut(id, me.getUserId());
            resp.sendRedirect(req.getContextPath() + "/reception/checkout?done=1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            resp.sendRedirect(req.getContextPath() + "/reception/checkout?id=" + id + "&err="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
