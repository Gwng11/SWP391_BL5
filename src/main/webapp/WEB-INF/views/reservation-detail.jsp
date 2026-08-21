<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Notification messages -->
<c:if test="${param.created == '1'}">
  <div class="msg" style="margin-bottom: 20px;">🎉 Đặt phòng thành công! Mã đơn của bạn là: <b>${r.bookingCode}</b>. Vui lòng thanh toán đặt cọc để xác nhận giữ chỗ.</div>
</c:if>
<c:if test="${param.paid == '1'}">
  <div class="msg" style="margin-bottom: 20px;">🎉 Ghi nhận thanh toán thành công!</div>
</c:if>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📄 Chi tiết Đơn đặt phòng: ${r.bookingCode}</h1>
    <p>Thời gian lưu trú: <b>${r.checkInDate}</b> → <b>${r.checkOutDate}</b> (${r.adultCount} NL + ${r.childCount} TE)</p>
  </div>
  <div>
    <span class="badge status-${r.statusCode}" style="font-size:14px; padding:6px 16px;">${r.statusCode}</span>
  </div>
</div>

<div class="grid">
  <!-- Left Side: Room Details, Guests, and Payments (col-8) -->
  <div class="col-8">
    
    <!-- 1. Room details card -->
    <div class="card">
      <h2 style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px; font-size:18px;">
        🏨 Chi tiết đặt phòng & Chi phí phòng
      </h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Loại phòng</th>
              <th>Số lượng</th>
              <th>Số đêm</th>
              <th style="text-align: right;">Đơn giá/đêm</th>
              <th style="text-align: right;">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="rr" items="${rooms}">
              <tr>
                <td><strong>${rr.typeName}</strong></td>
                <td><strong>${rr.quantity}</strong> phòng</td>
                <td>${rr.numberOfNights} đêm</td>
                <td style="text-align: right;"><fmt:formatNumber value="${rr.nightlyPriceSnapshot}"/> đ</td>
                <td style="text-align: right; font-weight:700; color:var(--bk-blue);"><fmt:formatNumber value="${rr.lineTotal}"/> đ</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>

      <div style="margin-top: 16px; padding: 14px; background: var(--bk-blue-light); border-radius: 8px; font-size:14px; color: var(--bk-navy);">
        <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
          <span>Tiền phòng (chưa thuế):</span>
          <strong><fmt:formatNumber value="${r.roomSubtotal}"/> đ</strong>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
          <span>Phí dịch vụ phát sinh:</span>
          <strong><fmt:formatNumber value="${r.serviceTotal}"/> đ</strong>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
          <span>Thuế giá trị gia tăng (10% VAT):</span>
          <strong><fmt:formatNumber value="${r.taxAmount}"/> đ</strong>
        </div>
        <div style="display:flex; justify-content:space-between; font-size:16px; font-weight:700; border-top:1px dashed rgba(0,53,128,0.2); padding-top:8px; margin-top:8px;">
          <span>Tổng cộng toàn bộ chi phí:</span>
          <strong style="color:var(--bk-blue);"><fmt:formatNumber value="${r.totalAmount}"/> đ</strong>
        </div>
      </div>
    </div>

    <!-- 2. Guests list card -->
    <c:if test="${not empty guests}">
      <div class="card">
        <h2 style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px; font-size:18px;">
          👥 Danh sách Khách lưu trú thực tế
        </h2>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Họ & tên khách hàng</th>
                <th>Giấy tờ tùy thân</th>
                <th style="text-align:center;">Vai trò</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="g" items="${guests}">
                <tr>
                  <td><strong><c:out value="${g.fullName}"/></strong></td>
                  <td>
                    <span class="badge" style="background:var(--bk-blue-light); color:var(--bk-blue);">
                      ${g.idDocumentType} <c:out value="${g.idDocumentNumber}"/>
                    </span>
                  </td>
                  <td style="text-align:center;">
                    <c:choose>
                      <c:when test="${g.primaryGuest}">
                        <span style="color:#d97706; font-weight:700;">★ Khách chính</span>
                      </c:when>
                      <c:otherwise>Khách phụ</c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </c:if>

    <!-- 3. Payments list card -->
    <c:if test="${not empty payments}">
      <div class="card">
        <h2 style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px; font-size:18px;">
          💳 Lịch sử giao dịch & Thanh toán
        </h2>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Phân loại</th>
                <th>Phương thức</th>
                <th style="text-align: right;">Số tiền giao dịch</th>
                <th>Trạng thái</th>
                <th>Thời gian</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="p" items="${payments}">
                <tr>
                  <td><strong>${p.paymentType}</strong></td>
                  <td><span class="badge" style="font-size:11px; padding:2px 6px;">${p.methodCode}</span></td>
                  <td style="text-align: right; font-weight:700; color:var(--bk-blue);"><fmt:formatNumber value="${p.amount}"/> đ</td>
                  <td>
                    <span class="badge status-${p.statusCode}">${p.statusCode}</span>
                  </td>
                  <td><small class="muted">${p.paidAt}</small></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </c:if>
  </div>

  <!-- Right Side: Pricing / Deposit Summary & Actions (col-4) -->
  <div class="col-4">
    <!-- Payment / Deposit card -->
    <div class="card" style="border: 2px solid var(--bk-gold-border); background: var(--bk-gold-light);">
      <h3 style="margin-top:0; color:#92400e; font-size:16px; font-weight:700; border-bottom:1px solid rgba(254,187,2,0.3); padding-bottom:8px; margin-bottom:12px;">
        💰 Thông tin cọc & Thanh toán
      </h3>
      <div style="font-size:13px; color:#78350f; margin-bottom:12px; display:flex; justify-content:space-between;">
        <span>Yêu cầu đặt cọc:</span>
        <strong><fmt:formatNumber value="${r.depositRequired}"/> đ</strong>
      </div>
      <div style="font-size:13px; color:#78350f; margin-bottom:16px; display:flex; justify-content:space-between; border-bottom:1px dashed rgba(254,187,2,0.3); padding-bottom:10px;">
        <span>Đã cọc thành công:</span>
        <strong style="color:#047857;"><fmt:formatNumber value="${depositPaid}"/> đ</strong>
      </div>

      <c:if test="${(r.statusCode == 'PENDING' || r.statusCode == 'CONFIRMED') && depositPaid < r.depositRequired}">
        <a class="btn btn-success" href="${pageContext.request.contextPath}/deposit?reservationId=${r.reservationId}" style="width:100%; text-align:center; padding:10px; font-weight:700; display:block;">
          💳 Đặt cọc ngay
        </a>
      </c:if>
    </div>

    <!-- Administrative actions card -->
    <c:if test="${r.statusCode == 'PENDING' || r.statusCode == 'CONFIRMED'}">
      <div class="card">
        <!-- A. Change stay dates -->
        <h3 style="font-size:15px; font-weight:700; color:var(--bk-navy); margin-top:0; border-bottom:1px solid var(--bk-border); padding-bottom:6px; margin-bottom:12px;">
          📅 Đổi ngày lưu trú
        </h3>
        <form method="post" action="${pageContext.request.contextPath}/reservation">
          <input type="hidden" name="id" value="${r.reservationId}">
          <input type="hidden" name="action" value="updateDates">
          
          <label>Ngày nhận mới</label>
          <input type="date" name="checkIn" value="${r.checkInDate}" required style="width:100%">
          
          <label style="margin-top:10px;">Ngày trả mới</label>
          <input type="date" name="checkOut" value="${r.checkOutDate}" required style="width:100%">
          
          <button class="btn btn-muted btn-small" type="submit" style="width:100%; margin-top:12px;">
            🔄 Cập nhật ngày
          </button>
        </form>

        <!-- B. Cancel Booking -->
        <h3 style="font-size:15px; font-weight:700; color:#b91c1c; margin-top:24px; border-bottom:1px solid var(--bk-border); padding-bottom:6px; margin-bottom:12px;">
          🚫 Yêu cầu hủy đơn đặt phòng
        </h3>
        <c:if test="${depositPaid > 0}">
          <p style="color:#b91c1c; font-size:12px; line-height:1.4; margin-top:0; margin-bottom:12px;">
            ⚠️ Đơn này đã đặt cọc <strong><fmt:formatNumber value="${depositPaid}"/> đ</strong>. Theo chính sách của khách sạn, tiền cọc sẽ <strong>KHÔNG ĐƯỢC HOÀN LẠI</strong> khi hủy đơn.
          </p>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/reservation" onsubmit="return confirm('Xác nhận HỦY ĐƠN ĐẶT PHÒNG?${depositPaid > 0 ? ' Tiền cọc đã nộp sẽ không được hoàn trả!' : ''}')">
          <input type="hidden" name="id" value="${r.reservationId}">
          <input type="hidden" name="action" value="cancel">
          
          <input name="reason" placeholder="Nhập lý do hủy đơn hàng..." maxlength="255" style="width:100%" required>
          
          <button class="btn btn-danger btn-small" type="submit" style="width:100%; margin-top:10px;">
            🚫 Hủy đơn phòng
          </button>
        </form>
      </div>
    </c:if>
  </div>
</div>

<%@ include file="_footer.jspf" %>
