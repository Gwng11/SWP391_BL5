<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🛌 Khách hàng Đang Lưu trú (Stays)</h1>
    <p>Quản lý các đoàn khách đang nghỉ tại khách sạn, gia hạn ngày trả phòng, thêm phụ thu dịch vụ hoặc xem hóa đơn</p>
  </div>
</div>

<div class="card col-12 table-wrap">
  <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
    <h2 style="margin:0; font-size:18px;">📋 Danh sách Khách đang lưu trú</h2>
    <span class="muted" style="font-size:12px;">Tổng số: ${stays.size()} đoàn khách</span>
  </div>

  <table>
    <thead>
      <tr>
        <th>Mã đơn</th>
        <th>Khách hàng</th>
        <th>Ngày nhận</th>
        <th>Trả dự kiến</th>
        <th>Gán phòng</th>
        <th style="text-align: right;">Giá trị</th>
        <th style="text-align: right;">Thao tác quản lý dịch vụ / lưu trú</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="rv" items="${stays}">
        <tr>
          <td>
            <strong style="color:var(--bk-navy); font-family:monospace; font-size:14px;">${rv.bookingCode}</strong>
          </td>
          <td>
            <strong style="font-size:14px;"><c:out value="${rv.customerName}"/></strong>
          </td>
          <td>${rv.checkInDate}</td>
          <td>
            <span>${rv.checkOutDate}</span>
            <c:if test="${overdueDays[rv.reservationId] != null}">
              <br><span class="badge" style="background:#fee2e2; color:#b91c1c; font-size:11px; margin-top:4px; display:inline-block;">⚠️ Quá hạn ${overdueDays[rv.reservationId]} ngày</span>
            </c:if>
          </td>
          <td>
            <div style="font-weight:600;">${roomProgress[rv.reservationId]}</div>
            <c:if test="${roomMissing[rv.reservationId]}">
              <a class="btn btn-danger btn-small" style="padding:2px 6px; font-size:11px; margin-top:4px; display:inline-block;" href="${pageContext.request.contextPath}/reception/assign?reservationId=${rv.reservationId}">⚠️ Chưa gán đủ phòng</a>
            </c:if>
          </td>
          <td style="text-align: right; font-weight:700; color:var(--bk-blue);">
            <fmt:formatNumber value="${rv.totalAmount}"/> đ
          </td>
          <td style="text-align: right;">
            <!-- Vertical actions stack -->
            <div style="display:flex; flex-direction:column; gap:8px; align-items:flex-end;">
              <!-- 1. Extend Checkout Form -->
              <form method="post" style="display:inline-flex; gap:4px; align-items:center;" action="${pageContext.request.contextPath}/reception/stays">
                <input type="hidden" name="reservationId" value="${rv.reservationId}">
                <input type="hidden" name="action" value="extend">
                <span style="font-size:12px; color:var(--bk-muted);">📅 Trả phòng:</span>
                <input type="date" name="newCheckOut" required style="padding:4px; font-size:12px; height:28px;">
                <button class="btn btn-small btn-muted" type="submit" style="padding:3px 8px;">Đổi ngày</button>
              </form>
              
              <!-- 2. Extra Charge Form -->
              <form method="post" style="display:inline-flex; gap:4px; align-items:center;" action="${pageContext.request.contextPath}/reception/stays">
                <input type="hidden" name="reservationId" value="${rv.reservationId}">
                <input type="hidden" name="action" value="extra">
                <input name="description" placeholder="Phụ thu (mô tả)" maxlength="255" style="width:130px; padding:4px; font-size:12px; height:28px;" required>
                <input type="number" name="quantity" value="1" step="1" style="width:50px; padding:4px; font-size:12px; height:28px;" required>
                <input type="number" name="unitPrice" placeholder="Đơn giá" step="1000" style="width:90px; padding:4px; font-size:12px; height:28px;" required>
                <button class="btn btn-small btn-muted" type="submit" style="padding:3px 8px;">+ Phụ thu</button>
              </form>
              
              <!-- 3. Add Notes Form -->
              <form method="post" style="display:inline-flex; gap:4px; align-items:center;" action="${pageContext.request.contextPath}/reception/stays">
                <input type="hidden" name="reservationId" value="${rv.reservationId}">
                <input type="hidden" name="action" value="note">
                <input name="note" placeholder="Ghi chú thêm..." maxlength="500" style="width:230px; padding:4px; font-size:12px; height:28px;" required>
                <button class="btn btn-small btn-muted" type="submit" style="padding:3px 8px;">Ghi chú</button>
              </form>

              <!-- 4. Quick Links -->
              <div style="display:flex; gap:6px; margin-top:4px;">
                <a class="btn btn-small btn-muted" href="${pageContext.request.contextPath}/reception/assign?reservationId=${rv.reservationId}">🔑 Xếp phòng</a>
                <a class="btn btn-small btn-success" href="${pageContext.request.contextPath}/reception/invoice?reservationId=${rv.reservationId}">💳 Hóa đơn / Trả phòng</a>
              </div>
            </div>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<%@ include file="_footer.jspf" %>
