<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
  .services-container { max-width: 1200px; margin: 0 auto; padding: 32px 16px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8f9fa; min-height: 100vh; }
  .services-banner { background: #003b95; color: white; padding: 28px 32px; border-radius: 12px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }

  /* Search Bar */
  .search-bar { background: white; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.04); display: flex; gap: 12px; border: 2px solid #febb02; }
  .search-bar input { flex: 1; padding: 10px 14px; border: 1px solid #ccc; border-radius: 4px; outline: none; font-size: 14px; }

  /* Card Grid (Booking.com style) */
  .service-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; margin-bottom: 32px; }
  .service-card { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,0.08); display: flex; flex-direction: column; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #e7e7e7; }
  .service-card:hover { transform: translateY(-4px); box-shadow: 0 6px 16px rgba(0,0,0,0.12); }
  .service-img { width: 100%; height: 160px; object-fit: cover; background: #e0e0e0; }
  .service-body { padding: 16px; flex: 1; display: flex; flex-direction: column; }
  .service-title { font-size: 18px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px 0; }
  .service-desc { font-size: 13px; color: #666; margin: 0 0 16px 0; flex: 1; line-height: 1.4; }
  .service-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f0f0f0; padding-top: 12px; margin-top: auto; }
  .service-price { font-size: 16px; font-weight: 700; color: #003b95; }

  /* Request Section */
  .request-section { background: white; border-radius: 8px; padding: 24px; box-shadow: 0 2px 6px rgba(0,0,0,0.08); max-width: 650px; margin: 0 auto; }
  .form-group { margin-bottom: 16px; }
  .form-group label { display: block; font-weight: 600; margin-bottom: 6px; font-size: 14px; color: #333; }
  .form-control { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; outline: none; }
  .btn-custom { background: #006ce4; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-block; text-align: center; }
  .btn-custom:hover { background: #0056b3; }
  .msg-success { background: #d4edda; color: #155724; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-weight: 500; }
</style>

<div class="services-container">
  <c:if test="${param.ok == '1'}"><div class="msg-success">✨ Đã gửi yêu cầu dịch vụ thành công!</div></c:if>

  <div class="services-banner">
    <h2 style="margin: 0 0 6px 0;">🏨 Khám phá Dịch vụ Khách sạn</h2>
    <p style="margin: 0; color: #add3ff; font-size: 14px;">Trải nghiệm các tiện ích đẳng cấp trong suốt thời gian lưu trú của bạn.</p>
  </div>

  <!-- Form tìm kiếm chuẩn GET (Sửa lỗi 405) -->
  <form method="get" action="${pageContext.request.contextPath}/services" class="search-bar">
    <input type="text" name="q" value="${keyword}" placeholder="🔍 Tìm kiếm tên dịch vụ...">
    <button class="btn-custom" type="submit">Tìm kiếm</button>
  </form>

  <!-- Danh sách dịch vụ dạng Thẻ (Booking.com style) -->
  <div class="service-grid">
    <c:forEach var="s" items="${catalog}">
      <div class="service-card">
        <c:choose>
          <c:when test="${not empty s.imageUrl}">
            <img src="${s.imageUrl}" alt="${s.serviceName}" class="service-img">
          </c:when>
          <c:otherwise>
            <div class="service-img" style="display:flex; align-items:center; justify-content:center; color:#888; font-size:13px; font-weight:500;">Chưa có hình ảnh</div>
          </c:otherwise>
        </c:choose>
        <div class="service-body">
          <h3 class="service-title">${s.serviceName}</h3>
          <p class="service-desc">${empty s.description ? 'Dịch vụ chất lượng cao đáp ứng mọi nhu cầu của quý khách hàng.' : s.description}</p>
          <div class="service-footer">
            <div>
              <span style="font-size:11px; color:#777; display:block;">Đơn giá</span>
              <span class="service-price"><fmt:formatNumber value="${s.unitPrice}"/> đ <sub style="font-weight:normal; font-size:11px;">/ ${s.unitName}</sub></span>
            </div>
            <a href="${pageContext.request.contextPath}/service-detail?id=${s.hotelServiceId}" class="btn-custom" style="padding: 6px 12px; font-size: 13px;">Chi tiết</a>
          </div>
        </div>
      </div>
    </c:forEach>
  </div>

  <!-- Khung tạo yêu cầu dịch vụ (Giữ nguyên toàn bộ logic cũ) -->
  <div class="request-section">
    <h3 style="margin-top: 0; color: #003b95; font-size: 18px; border-bottom: 2px solid #f0f0f0; padding-bottom: 12px;">⚡ Tạo yêu cầu dịch vụ</h3>
    <c:choose>
    <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER' && empty activeReservations}">
      <p style="color: #d4111e; font-weight: 500;">Bạn cần đang lưu trú (đã check-in) để có thể yêu cầu dịch vụ.</p>
    </c:when>
    <c:otherwise>
    <form method="post" action="${pageContext.request.contextPath}/services">
      <div class="form-group">
        <label>Đơn lưu trú</label>
        <c:choose>
        <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER'}">
          <select name="reservationId" class="form-control">
            <c:forEach var="rv" items="${activeReservations}">
              <option value="${rv.reservationId}">${rv.bookingCode} (${rv.checkInDate} → ${rv.checkOutDate})</option>
            </c:forEach>
          </select>
        </c:when>
        <c:otherwise>
          <input type="number" name="reservationId" placeholder="Nhập ID đơn CHECKED_IN" class="form-control" required>
        </c:otherwise>
        </select>
        </c:otherwise>
        </c:choose>
      </div>

      <div class="form-group">
        <label>Dịch vụ</label>
        <select name="hotelServiceId" class="form-control">
          <c:forEach var="s" items="${catalog}">
            <option value="${s.hotelServiceId}">${s.serviceName} — <fmt:formatNumber value="${s.unitPrice}"/> đ / ${s.unitName}</option>
          </c:forEach>
        </select>
      </div>

      <div class="form-group">
        <label>Số lượng</label>
        <input type="number" name="quantity" value="1" step="0.01" min="0.01" class="form-control" required>
      </div>

      <div class="form-group">
        <label>Thời gian mong muốn</label>
        <input type="datetime-local" name="scheduledAt" class="form-control">
      </div>

      <div class="form-group">
        <label>Ghi chú</label>
        <textarea name="notes" rows="2" class="form-control"></textarea>
      </div>

      <button class="btn-custom" type="submit" style="background-color: #008234; width: 100%;">Gửi yêu cầu ngay</button>
    </form>
    </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="_footer.jspf" %>
