<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🏨 Đơn đặt phòng của tôi</h1>
    <p>Theo dõi tiến độ đơn đặt phòng, thanh toán cọc và thông tin lưu trú của bạn</p>
  </div>
</div>

<div class="card">
  <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:20px;">
    <h2 style="margin:0; font-size:18px;">📋 Lịch sử đặt phòng</h2>
    <span class="muted" style="font-size:13px;">Hiển thị danh sách các đơn đã đặt</span>
  </div>

  <c:choose>
    <c:when test="${empty reservations}">
      <div style="text-align:center; padding:40px 20px; color:var(--bk-muted); font-size: 15px;">
        📭 Bạn chưa có đơn đặt phòng nào trên hệ thống. 
        <div style="margin-top:12px;">
          <a class="btn btn-gold btn-small" href="${pageContext.request.contextPath}/rooms">🔍 Tìm phòng ngay</a>
        </div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Mã đơn hàng</th>
              <th>Ngày nhận phòng</th>
              <th>Ngày trả phòng</th>
              <th style="text-align: right;">Tổng giá trị</th>
              <th style="text-align: right;">Cọc tối thiểu</th>
              <th>Trạng thái đơn</th>
              <th style="text-align: right;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="r" items="${reservations}">
              <tr>
                <td>
                  <strong style="color:var(--bk-navy); font-family:monospace; font-size:14px;">${r.bookingCode}</strong>
                </td>
                <td><strong>${r.checkInDate}</strong></td>
                <td><strong>${r.checkOutDate}</strong></td>
                <td style="text-align: right; font-weight: 700; color: var(--bk-blue);">
                  <fmt:formatNumber value="${r.totalAmount}"/> đ
                </td>
                <td style="text-align: right; font-weight: 600; color: var(--bk-text);">
                  <fmt:formatNumber value="${r.depositRequired}"/> đ
                </td>
                <td>
                  <span class="badge status-${r.statusCode}">${r.statusCode}</span>
                </td>
                <td style="text-align: right;">
                  <a class="btn btn-muted btn-small" href="${pageContext.request.contextPath}/reservation?id=${r.reservationId}">✏️ Chi tiết</a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="_footer.jspf" %>
