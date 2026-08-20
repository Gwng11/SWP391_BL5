<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<c:if test="${param.done == '1'}"><div class="msg">Check-out thành công! Phòng đã chuyển trạng thái chờ dọn.</div></c:if>
<div class="card">
  <h2>Check-out</h2>
  <table>
    <tr><th>Mã đơn</th><th>Khách</th><th>Trả dự kiến</th><th></th></tr>
    <c:forEach var="rv" items="${stays}">
      <tr><td>${rv.bookingCode}</td><td><c:out value="${rv.customerName}"/></td><td>${rv.checkOutDate}</td>
          <td><a class="btn" href="?id=${rv.reservationId}">Xem chi phí</a></td></tr>
    </c:forEach>
  </table>
</div>
<c:if test="${r != null}">
<div class="card">
  <h3>Đơn ${r.bookingCode} — <c:out value="${r.customerName}"/></h3>
  <c:choose>
    <c:when test="${invoice == null || invoice.statusCode == 'DRAFT'}">
      <p class="err">Chưa phát hành hóa đơn cuối.
        <a class="btn" href="${pageContext.request.contextPath}/reception/invoice?reservationId=${r.reservationId}">→ Sang F14 phát hành & thanh toán</a></p>
    </c:when>
    <c:otherwise>
      <p>Hóa đơn ${invoice.invoiceNumber} — <span class="badge">${invoice.statusCode}</span>
         — Tổng <fmt:formatNumber value="${invoice.totalAmount}"/> đ, đã trả <fmt:formatNumber value="${invoice.paidAmount}"/> đ</p>
      <table><tr><th>Mục</th><th>Loại</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>
      <c:forEach var="i" items="${items}">
        <tr><td><c:out value="${i.description}"/></td><td>${i.itemType}</td><td>${i.quantity}</td>
            <td><fmt:formatNumber value="${i.unitPrice}"/> đ</td><td><fmt:formatNumber value="${i.amount}"/> đ</td></tr>
      </c:forEach></table>
      <form method="post" action="${pageContext.request.contextPath}/reception/checkout" style="margin-top:12px"
            onsubmit="return confirm('Xác nhận khách rời đi?')">
        <input type="hidden" name="id" value="${r.reservationId}">
        <button class="btn btn-danger" type="submit">✔ Xác nhận Check-out</button>
      </form>
    </c:otherwise>
  </c:choose>
</div>
</c:if>
<%@ include file="_footer.jspf" %>
