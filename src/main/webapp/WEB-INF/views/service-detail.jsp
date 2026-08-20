<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
    * { box-sizing: border-box; }
    body { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9; color: #333; }
    .lux-wrapper { display: flex; min-height: 100vh; }
    .lux-sidebar { width: 260px; background-color: #0b1b42; color: #fff; display: flex; flex-direction: column; }
    .lux-brand { font-size: 1.5rem; font-weight: bold; padding: 24px; color: #fff; letter-spacing: 0.5px; }
    .lux-nav { list-style: none; padding: 0; margin: 0; }
    .lux-nav li a { display: block; padding: 14px 24px; color: #9ba4b5; text-decoration: none; font-weight: 500; }
    .lux-nav li.active a { color: #0b1b42; background-color: #e5b945; border-radius: 0 20px 20px 0; margin-right: 20px; font-weight: 600; }
    .lux-main { flex: 1; display: flex; flex-direction: column; }
    .lux-topbar { background: #fff; padding: 16px 32px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .lux-content { padding: 32px; flex: 1; }

    .lux-detail-container { background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); display: flex; gap: 32px; }
    .lux-detail-img { width: 40%; height: 320px; object-fit: cover; border-radius: 8px; background: #eee; }
    .lux-detail-info { flex: 1; }
    .lux-detail-title { font-size: 1.8rem; color: #0b1b42; margin: 0 0 12px 0; }
    .lux-detail-price { font-size: 1.4rem; font-weight: bold; color: #10b981; margin-bottom: 16px; }
    .lux-detail-desc { color: #555; line-height: 1.6; margin-bottom: 24px; }

    .lux-form-group { margin-bottom: 16px; }
    .lux-form-group label { display: block; font-weight: 600; margin-bottom: 6px; color: #444; }
    .lux-form-control { width: 100%; padding: 10px; border-radius: 6px; border: 1px solid #ccc; font-size: 0.95rem; }
    .lux-btn-submit { background: #0b1b42; color: #fff; border: none; padding: 12px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 1rem; }
    .lux-btn-submit:hover { background: #1a2f63; }
    .lux-alert-error { background: #fee2e2; color: #991b1b; padding: 12px; border-radius: 6px; margin-bottom: 16px; }
</style>

<div class="lux-wrapper">
    <aside class="lux-sidebar">
        <div class="lux-brand">LuxeStay HMS</div>
        <ul class="lux-nav">
            <li><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
            <li class="active"><a href="${pageContext.request.contextPath}/services">Dịch vụ Khách sạn</a></li>
        </ul>
    </aside>

    <main class="lux-main">
        <header class="lux-topbar">
            <h2>Chi tiết dịch vụ</h2>
            <a href="${pageContext.request.contextPath}/services" style="color: #0b1b42; text-decoration: none; font-weight: 600;">&larr; Quay lại danh sách</a>
        </header>

        <div class="lux-content">
            <c:if test="${not empty param.err}">
                <div class="lux-alert-error">${param.err}</div>
            </c:if>

            <div class="lux-detail-container">
                <c:choose>
                    <c:when test="${not empty service.imageUrl}">
                        <img src="${service.imageUrl}" alt="${service.serviceName}" class="lux-detail-img">
                    </c:when>
                    <c:otherwise>
                        <div class="lux-detail-img" style="display:flex; align-items:center; justify-content:center; color:#aaa;">No Image Available</div>
                    </c:otherwise>
                </c:choose>

                <div class="lux-detail-info">
                    <h1 class="lux-detail-title">${service.serviceName}</h1>
                    <div class="lux-detail-price"><fmt:formatNumber value="${service.unitPrice}"/> đ / ${service.unitName}</div>
                    <p class="lux-detail-desc">${service.description}</p>

                    <!-- Form Đặt dịch vụ -->
                    <form method="post" action="${pageContext.request.contextPath}/service-detail">
                        <input type="hidden" name="hotelServiceId" value="${service.hotelServiceId}">

                        <div class="lux-form-group">
                            <label>Chọn đơn đặt phòng đang lưu trú:</label>
                            <select name="reservationId" class="lux-form-control" required>
                                <c:choose>
                                    <c:when test="${empty activeReservations}">
                                        <option value="">-- Bạn không có phòng nào đang CHECKED_IN --</option>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="r" items="${activeReservations}">
                                            <option value="${r.reservationId}">Đơn #${r.bookingCode} (Mã phòng: ${r.reservationId})</option>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </select>
                        </div>

                        <div class="lux-form-group">
                            <label>Số lượng (${service.unitName}):</label>
                            <input type="number" name="quantity" value="1" min="1" step="1" class="lux-form-control" required>
                        </div>

                        <div class="lux-form-group">
                            <label>Thời gian hẹn (không bắt buộc):</label>
                            <input type="datetime-local" name="scheduledAt" class="lux-form-control">
                        </div>

                        <div class="lux-form-group">
                            <label>Ghi chú thêm:</label>
                            <textarea name="notes" rows="3" class="lux-form-control" placeholder="Ví dụ: Giao lên phòng trước 8h tối..."></textarea>
                        </div>

                        <button type="submit" class="lux-btn-submit" <c:if test="${empty activeReservations}">disabled style="opacity: 0.5;"</c:if>>
                            Gửi yêu cầu dịch vụ
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<%@ include file="_footer.jspf" %>