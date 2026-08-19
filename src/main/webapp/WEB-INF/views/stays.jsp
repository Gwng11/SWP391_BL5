<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Khách đang lưu trú</h2>
  <table>
    <tr><th>Mã đơn</th><th>Khách</th><th>Nhận</th><th>Trả dự kiến</th><th>Phòng</th><th>Tổng tiền</th><th>Thao tác</th></tr>
    <c:forEach var="rv" items="${stays}">
      <tr>
        <td>${rv.bookingCode}</td><td>${rv.customerName}</td>
        <td>${rv.checkInDate}</td>
        <td>${rv.checkOutDate}
          <c:if test="${overdueDays[rv.reservationId] != null}">
            <br><span class="badge" style="background:#fdecea;color:#c0392b">⚠ quá hạn ${overdueDays[rv.reservationId]} ngày</span>
          </c:if>
        </td>
        <td>
          ${roomProgress[rv.reservationId]}
          <c:if test="${roomMissing[rv.reservationId]}">
            <a class="btn btn-danger" style="padding:3px 8px;font-size:12px"
               href="${pageContext.request.contextPath}/reception/assign?reservationId=${rv.reservationId}">⚠ Chưa gán đủ</a>
          </c:if>
        </td>
        <td><fmt:formatNumber value="${rv.totalAmount}"/> đ</td>
        <td>
          <form method="post" style="display:inline-flex;gap:4px" action="${pageContext.request.contextPath}/reception/stays">
            <input type="hidden" name="reservationId" value="${rv.reservationId}">
            <input type="hidden" name="action" value="extend">
            <input type="date" name="newCheckOut" required>
            <button class="btn" type="submit">Đổi ngày trả</button>
          </form>
          <form method="post" style="display:inline-flex;gap:4px" action="${pageContext.request.contextPath}/reception/stays">
            <input type="hidden" name="reservationId" value="${rv.reservationId}">
            <input type="hidden" name="action" value="extra">
            <input name="description" placeholder="Phụ thu (mô tả)" maxlength="255" style="width:130px" required>
            <input type="number" name="quantity" value="1" step="0.01" style="width:60px" required>
            <input type="number" name="unitPrice" placeholder="Đơn giá" step="0.01" style="width:90px" required>
            <button class="btn" type="submit">+ Phụ thu</button>
          </form>
          <form method="post" style="display:inline-flex;gap:4px" action="${pageContext.request.contextPath}/reception/stays">
            <input type="hidden" name="reservationId" value="${rv.reservationId}">
            <input type="hidden" name="action" value="note">
            <input name="note" placeholder="Ghi chú" maxlength="500" style="width:140px" required>
            <button class="btn" type="submit">Ghi chú</button>
          </form>
          <a class="btn" href="${pageContext.request.contextPath}/reception/assign?reservationId=${rv.reservationId}">Phòng</a>
          <a class="btn btn-success" href="${pageContext.request.contextPath}/reception/invoice?reservationId=${rv.reservationId}">Hóa đơn</a>
        </td>
      </tr>
    </c:forEach>
  </table>
</div>
<%@ include file="_footer.jspf" %>
