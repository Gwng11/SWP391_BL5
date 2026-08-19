<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<c:if test="${param.created == '1'}"><div class="msg">Đặt phòng thành công! Mã đơn: <b>${r.bookingCode}</b>. Vui lòng đặt cọc để xác nhận.</div></c:if>
<c:if test="${param.paid == '1'}"><div class="msg">Thanh toán thành công!</div></c:if>
<div class="card">
  <h2>Đơn ${r.bookingCode} <span class="badge">${r.statusCode}</span></h2>
  <p>Khách: <b>${r.customerName}</b> — ${r.checkInDate} → ${r.checkOutDate} — ${r.adultCount} NL + ${r.childCount} TE</p>
  <table>
    <tr><th>Loại phòng</th><th>SL</th><th>Đêm</th><th>Giá/đêm</th><th>Thành tiền</th></tr>
    <c:forEach var="rr" items="${rooms}">
      <tr><td>${rr.typeName}</td><td>${rr.quantity}</td><td>${rr.numberOfNights}</td>
          <td><fmt:formatNumber value="${rr.nightlyPriceSnapshot}"/> đ</td>
          <td><fmt:formatNumber value="${rr.lineTotal}"/> đ</td></tr>
    </c:forEach>
  </table>
  <p>Tiền phòng: <fmt:formatNumber value="${r.roomSubtotal}"/> đ — Dịch vụ: <fmt:formatNumber value="${r.serviceTotal}"/> đ
     — Thuế: <fmt:formatNumber value="${r.taxAmount}"/> đ — <b>Tổng: <fmt:formatNumber value="${r.totalAmount}"/> đ</b></p>
  <p>Cọc yêu cầu: <fmt:formatNumber value="${r.depositRequired}"/> đ — Đã cọc: <fmt:formatNumber value="${depositPaid}"/> đ
    <c:if test="${(r.statusCode == 'PENDING' || r.statusCode == 'CONFIRMED') && depositPaid < r.depositRequired}">
      <a class="btn btn-success" href="${pageContext.request.contextPath}/deposit?reservationId=${r.reservationId}">Đặt cọc ngay</a>
    </c:if>
  </p>
</div>
<c:if test="${not empty guests}">
<div class="card"><h3>Khách ở</h3>
  <table><tr><th>Họ tên</th><th>Giấy tờ</th><th>Chính</th></tr>
  <c:forEach var="g" items="${guests}">
    <tr><td>${g.fullName}</td><td>${g.idDocumentType} ${g.idDocumentNumber}</td><td>${g.primaryGuest ? '✔' : ''}</td></tr>
  </c:forEach></table>
</div>
</c:if>
<c:if test="${not empty payments}">
<div class="card"><h3>Thanh toán</h3>
  <table><tr><th>Loại</th><th>Phương thức</th><th>Số tiền</th><th>Trạng thái</th><th>Thời gian</th></tr>
  <c:forEach var="p" items="${payments}">
    <tr><td>${p.paymentType}</td><td>${p.methodCode}</td><td><fmt:formatNumber value="${p.amount}"/> đ</td>
        <td><span class="badge">${p.statusCode}</span></td><td>${p.paidAt}</td></tr>
  </c:forEach></table>
</div>
</c:if>
<c:if test="${r.statusCode == 'PENDING' || r.statusCode == 'CONFIRMED'}">
<div class="card">
  <h3>Đổi ngày ở</h3>
  <form method="post" action="${pageContext.request.contextPath}/reservation" style="display:flex;gap:10px;align-items:end;flex-wrap:wrap">
    <input type="hidden" name="id" value="${r.reservationId}">
    <input type="hidden" name="action" value="updateDates">
    <div><label>Nhận phòng mới</label><input type="date" name="checkIn" value="${r.checkInDate}" required></div>
    <div><label>Trả phòng mới</label><input type="date" name="checkOut" value="${r.checkOutDate}" required></div>
    <button class="btn" type="submit">Cập nhật</button>
  </form>
  <h3 style="margin-top:20px">Hủy đơn</h3>
  <c:if test="${depositPaid > 0}">
    <p style="color:#c0392b">⚠ Đơn đã đặt cọc <b><fmt:formatNumber value="${depositPaid}"/> đ</b> —
       theo chính sách khách sạn, tiền cọc <b>KHÔNG được hoàn lại</b> khi hủy đơn.</p>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/reservation"
        onsubmit="return confirm('Xác nhận hủy đơn?${depositPaid > 0 ? ' Tiền cọc sẽ KHÔNG được hoàn lại!' : ''}')">
    <input type="hidden" name="id" value="${r.reservationId}">
    <input type="hidden" name="action" value="cancel">
    <input name="reason" placeholder="Lý do hủy" maxlength="255" style="width:300px">
    <button class="btn btn-danger" type="submit">Hủy đơn</button>
  </form>
</div>
</c:if>
<%@ include file="_footer.jspf" %>
