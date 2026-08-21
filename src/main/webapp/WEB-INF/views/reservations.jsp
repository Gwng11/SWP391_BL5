<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📅 Quản lý Đơn Đặt phòng</h1>
    <p>Tìm kiếm đơn đặt phòng, thực hiện thủ tục nhận phòng (Check-in), hủy phòng hoặc ghi nhận đóng cọc</p>
  </div>
</div>

<!-- Search & Filter Box -->
<div class="bk-search-box">
  <form method="get" action="${pageContext.request.contextPath}/reception/reservations" class="bk-search-form">
    <div style="flex: 1; min-width: 250px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🔍 Từ khóa tìm kiếm</label>
      <input name="q" value="<c:out value='${q}'/>" placeholder="Tìm theo mã đơn, tên khách, số điện thoại..." style="width:100%">
    </div>
    <div style="width: 220px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">⚡ Trạng thái đơn</label>
      <select name="status" style="width:100%">
        <option value="">-- Tất cả trạng thái --</option>
        <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>PENDING (Chờ cọc)</option>
        <option value="CONFIRMED" ${statusFilter == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED (Đã xác nhận)</option>
        <option value="CHECKED_IN" ${statusFilter == 'CHECKED_IN' ? 'selected' : ''}>CHECKED_IN (Đang lưu trú)</option>
        <option value="CHECKED_OUT" ${statusFilter == 'CHECKED_OUT' ? 'selected' : ''}>CHECKED_OUT (Đã trả phòng)</option>
        <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>CANCELLED (Đã hủy)</option>
        <option value="NO_SHOW" ${statusFilter == 'NO_SHOW' ? 'selected' : ''}>NO_SHOW (Khách không đến)</option>
      </select>
    </div>
    <div style="margin-top: auto; display:flex; gap:8px;">
      <button class="btn" type="submit" style="height: 38px;">🔍 Tìm kiếm</button>
      <a href="${pageContext.request.contextPath}/reception/reservations" class="btn btn-muted" style="height:38px; display:inline-flex; align-items:center;">🔄 Reset</a>
    </div>
  </form>
</div>

<!-- List of Bookings Card -->
<div class="card">
  <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
    <h2 style="margin:0; font-size:18px;">📋 Danh sách Đơn đặt phòng (${reservations.size()} kết quả)</h2>
    <span class="muted" style="font-size:12px;">Mới cập nhật gần nhất</span>
  </div>

  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>Mã đơn</th>
          <th>Khách hàng</th>
          <th>Ngày nhận</th>
          <th>Ngày trả</th>
          <th style="text-align: right;">Tổng tiền</th>
          <th style="text-align: right;">Cọc yêu cầu</th>
          <th>Trạng thái</th>
          <th style="text-align: right;">Hành động</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="rv" items="${reservations}">
          <tr>
            <td>
              <strong style="color:var(--bk-navy); font-family:monospace; font-size:14px;">${rv.bookingCode}</strong>
            </td>
            <td>
              <strong style="font-size:14px;"><c:out value="${rv.customerName}"/></strong>
            </td>
            <td><strong>${rv.checkInDate}</strong></td>
            <td><strong>${rv.checkOutDate}</strong></td>
            <td style="text-align: right; font-weight:700; color:var(--bk-blue);">
              <fmt:formatNumber value="${rv.totalAmount}"/> đ
            </td>
            <td style="text-align: right; font-weight:600; color:var(--bk-text);">
              <fmt:formatNumber value="${rv.depositRequired}"/> đ
            </td>
            <td>
              <span class="badge status-${rv.statusCode}">${rv.statusCode}</span>
            </td>
            <td style="text-align: right; white-space:nowrap;">
              <div style="display:inline-flex; gap:6px;">
                <a class="btn btn-muted btn-small" href="${pageContext.request.contextPath}/reservation?id=${rv.reservationId}">✏️ Xem</a>
                
                <c:if test="${rv.statusCode == 'PENDING'}">
                  <a class="btn btn-success btn-small" href="${pageContext.request.contextPath}/deposit?reservationId=${rv.reservationId}">💰 Thu cọc</a>
                </c:if>
                
                <c:if test="${rv.statusCode == 'CONFIRMED'}">
                  <a class="btn btn-success btn-small" href="${pageContext.request.contextPath}/reception/checkin?id=${rv.reservationId}">📥 Check-in</a>
                  <form method="post" style="display:inline" action="${pageContext.request.contextPath}/reception/reservations" onsubmit="return confirm('Xác nhận khách KHÔNG ĐẾN? Đơn sẽ chuyển trạng thái sang NO_SHOW và trả lại phòng trống.')">
                    <input type="hidden" name="id" value="${rv.reservationId}">
                    <input type="hidden" name="action" value="noshow">
                    <button class="btn btn-danger btn-small" type="submit">🚫 Không đến</button>
                  </form>
                </c:if>
                
                <c:if test="${rv.statusCode == 'CHECKED_IN'}">
                  <a class="btn btn-muted btn-small" href="${pageContext.request.contextPath}/reception/assign?reservationId=${rv.reservationId}">🔑 Xếp phòng</a>
                  <a class="btn btn-gold btn-small" href="${pageContext.request.contextPath}/reception/invoice?reservationId=${rv.reservationId}">📄 Hóa đơn</a>
                </c:if>
              </div>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<%@ include file="_footer.jspf" %>
