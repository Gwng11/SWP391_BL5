<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Check-in (đơn CONFIRMED)</h2>
  <form method="get" action="${pageContext.request.contextPath}/reception/checkin">
    <input name="q" placeholder="Mã đơn / tên khách / SĐT" value="${param.q}" style="width:300px">
    <button class="btn" type="submit">Tìm</button>
  </form>
  <table style="margin-top:12px">
    <tr><th>Mã đơn</th><th>Khách</th><th>Nhận</th><th>Trả</th><th>Tổng</th><th></th></tr>
    <c:forEach var="rv" items="${reservations}">
      <tr><td>${rv.bookingCode}</td><td>${rv.customerName}</td><td>${rv.checkInDate}</td><td>${rv.checkOutDate}</td>
          <td><fmt:formatNumber value="${rv.totalAmount}"/> đ</td>
          <td><a class="btn" href="?id=${rv.reservationId}">Chọn</a></td></tr>
    </c:forEach>
  </table>
</div>
<c:if test="${r != null}">
<div class="card">
  <h3>Xác nhận check-in: ${r.bookingCode}</h3>
  <p>Khách: <b>${r.customerName}</b> — Cọc yêu cầu <fmt:formatNumber value="${r.depositRequired}"/> đ,
     đã nộp <b><fmt:formatNumber value="${depositPaid}"/> đ</b></p>
  <h4>Danh sách khách ở (đối chiếu giấy tờ)</h4>
  <table><tr><th>Họ tên</th><th>Giấy tờ</th></tr>
  <c:forEach var="g" items="${guests}">
    <tr><td>${g.fullName}</td><td>${g.idDocumentType} ${g.idDocumentNumber}</td></tr>
  </c:forEach></table>
  <form method="post" action="${pageContext.request.contextPath}/reception/checkin" style="margin-top:12px">
    <input type="hidden" name="id" value="${r.reservationId}">
    <button class="btn btn-success" type="submit">✔ Check-in & chuyển sang gán phòng</button>
  </form>
</div>
</c:if>
<%@ include file="_footer.jspf" %>
