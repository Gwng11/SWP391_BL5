<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:520px">
  <h2>Đặt cọc đơn ${r.bookingCode}</h2>
  <p>Tổng tiền đơn: <b><fmt:formatNumber value="${r.totalAmount}"/> đ</b></p>
  <p>Cọc yêu cầu: <b><fmt:formatNumber value="${r.depositRequired}"/> đ</b> — Đã nộp: <fmt:formatNumber value="${depositPaid}"/> đ</p>
  <p>Còn phải nộp: <b style="color:#c0392b"><fmt:formatNumber value="${outstanding}"/> đ</b></p>
  <form method="post" action="${pageContext.request.contextPath}/deposit">
    <input type="hidden" name="reservationId" value="${r.reservationId}">
    <label>Số tiền</label><input type="number" step="0.01" name="amount" value="${outstanding}" style="width:100%" required>
    <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
      <label>Phương thức</label>
      <select name="method" style="width:100%">
        <option value="CASH">Tiền mặt</option>
        <option value="ONLINE">Online (giả lập)</option>
      </select>
    </c:if>
    <p><button class="btn btn-success" type="submit" style="width:100%">Thanh toán</button></p>
  </form>
</div>
<%@ include file="_footer.jspf" %>
