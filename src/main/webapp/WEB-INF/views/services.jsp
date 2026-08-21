<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🍽️ Dịch vụ Khách sạn</h1>
    <p>Đặt đồ ăn sáng, giặt ủi, gọi xe đưa đón sân bay, dịch vụ spa thư giãn ngay tại phòng</p>
  </div>
</div>

<c:if test="${param.ok == '1'}">
  <div class="msg" style="margin-bottom: 20px;">🎉 Gửi yêu cầu dịch vụ thành công! Hệ thống sẽ xử lý trong giây lát.</div>
</c:if>

<div class="grid">
  <!-- Left Side: Hotel Services List (col-7) -->
  <div class="card col-7">
    <div style="border-bottom: 1px solid var(--bk-border); padding-bottom: 12px; margin-bottom: 16px;">
      <h2 style="margin:0; font-size:18px;">📋 Bảng giá & Danh mục Dịch vụ</h2>
    </div>
    
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Tên dịch vụ</th>
            <th>Mô tả chi tiết</th>
            <th>Đơn vị</th>
            <th style="text-align: right;">Đơn giá</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="s" items="${catalog}">
            <tr>
              <td><strong style="color:var(--bk-navy);">${s.serviceName}</strong></td>
              <td><span style="font-size:13px; color:var(--bk-muted);">${s.description}</span></td>
              <td><span class="badge" style="background:var(--bk-blue-light); color:var(--bk-blue);">${s.unitName}</span></td>
              <td style="text-align: right; font-weight:700; color:var(--bk-text);">
                <fmt:formatNumber value="${s.unitPrice}"/> đ
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Right Side: Request Form (col-5) -->
  <div class="card col-5">
    <div style="border-bottom: 1px solid var(--bk-border); padding-bottom: 12px; margin-bottom: 16px;">
      <h2 style="margin:0; font-size:18px;">➕ Tạo yêu cầu dịch vụ</h2>
    </div>

    <c:choose>
      <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER' && empty activeReservations}">
        <div style="text-align:center; padding:30px 10px; color:#b91c1c; background:#fee2e2; border-radius:8px; font-size:13px; font-weight:600;">
          ⚠️ Bạn cần đang ở trạng thái lưu trú (đã Check-in vào phòng) mới có thể tạo yêu cầu dịch vụ trực tuyến.
        </div>
      </c:when>
      <c:otherwise>
        <form method="post" action="${pageContext.request.contextPath}/services">
          <label>🔑 Chọn đơn lưu trú / phòng của bạn *</label>
          <c:choose>
            <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER'}">
              <select name="reservationId" style="width:100%" required>
                <c:forEach var="rv" items="${activeReservations}">
                  <option value="${rv.reservationId}">${rv.bookingCode} (${rv.checkInDate} → ${rv.checkOutDate})</option>
                </c:forEach>
              </select>
            </c:when>
            <c:otherwise>
              <input type="number" name="reservationId" placeholder="Nhập ID đơn lưu trú (đơn CHECKED_IN)..." style="width:100%" required>
            </c:otherwise>
          </c:choose>

          <label style="margin-top:12px;">🍽️ Chọn dịch vụ cần gọi *</label>
          <select name="hotelServiceId" style="width:100%" required>
            <c:forEach var="s" items="${catalog}">
              <option value="${s.hotelServiceId}">${s.serviceName} — <fmt:formatNumber value="${s.unitPrice}"/> đ/${s.unitName}</option>
            </c:forEach>
          </select>

          <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-top:12px;">
            <div>
              <label>🔢 Số lượng *</label>
              <input type="number" name="quantity" value="1" step="1" min="1" style="width:100%" required>
            </div>
            <div>
              <label>📅 Giờ hẹn thực hiện</label>
              <input type="datetime-local" name="scheduledAt" style="width:100%">
            </div>
          </div>

          <label style="margin-top:12px;">💬 Ghi chú chi tiết yêu cầu</label>
          <textarea name="notes" placeholder="Nhập yêu cầu cụ thể (ví dụ: giao lúc 8h sáng, giặt quần áo sáng màu...)" rows="3" style="width:100%"></textarea>

          <div style="margin-top:16px;">
            <button class="btn btn-success" type="submit" style="width:100%; font-size:15px; padding:10px 14px;">
              ✔ Gửi yêu cầu dịch vụ
            </button>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="_footer.jspf" %>
