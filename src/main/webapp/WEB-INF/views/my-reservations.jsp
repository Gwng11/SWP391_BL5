<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Đơn đặt phòng của tôi</h2>
  <table>
    <tr><th>Mã đơn</th><th>Nhận</th><th>Trả</th><th>Tổng tiền</th><th>Cọc yêu cầu</th><th>Trạng thái</th><th></th></tr>
    <c:forEach var="r" items="${reservations}">
      <tr>
        <td>${r.bookingCode}</td>
        <td>${r.checkInDate}</td><td>${r.checkOutDate}</td>
        <td><fmt:formatNumber value="${r.totalAmount}"/> đ</td>
        <td><fmt:formatNumber value="${r.depositRequired}"/> đ</td>
        <td><span class="badge">${r.statusCode}</span></td>
        <td><a class="btn" href="${pageContext.request.contextPath}/reservation?id=${r.reservationId}">Xem</a></td>
      </tr>
    </c:forEach>
  </table>
</div>
<%@ include file="_footer.jspf" %>
