<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:520px">
  <h2>Đặt cọc đơn ${r.bookingCode}</h2>
  <p>Tổng tiền đơn: <b><fmt:formatNumber value="${r.totalAmount}"/> đ</b></p>
  <p>Cọc yêu cầu: <b><fmt:formatNumber value="${r.depositRequired}"/> đ</b> — Đã nộp: <fmt:formatNumber value="${depositPaid}"/> đ</p>
  <p>Còn phải nộp: <b style="color:#c0392b"><fmt:formatNumber value="${outstanding}"/> đ</b></p>
  <c:if test="${not onlinePaymentAvailable}">
    <div class="err">Thanh toán online chưa được cấu hình. Cần đặt
      <code>HMS_VNPAY_TMN_CODE</code> và <code>HMS_VNPAY_HASH_SECRET</code>, sau đó khởi động lại Tomcat.
    </div>
  </c:if>
  <c:if test="${onlinePaymentSimulation}">
    <div class="alert">Đang dùng Sandbox mô phỏng: thao tác này không đi qua VNPay và sẽ tự ghi nhận thành công.</div>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/deposit">
  <input type="hidden" name="reservationId" value="${r.reservationId}">
  <label>Số tiền</label>
  <c:choose>
    <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER'}">
      <p style="font-weight:700; font-size:16px; color:#2980b9; margin:4px 0 12px 0;">
        <fmt:formatNumber value="${outstanding}"/> đ
      </p>
      <input type="hidden" name="amount" value="${outstanding}">
    </c:when>
    <c:otherwise>
      <input type="number" step="0.01" name="amount" value="${outstanding}" style="width:100%" required>
    </c:otherwise>
  </c:choose>
  <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
    <label>Phương thức</label>
    <select name="method" style="width:100%">
      <option value="CASH">Tiền mặt</option>
      <option value="BANK_TRANSFER">Chuyển khoản đã xác nhận</option>
      <c:choose>
        <c:when test="${onlinePaymentAvailable}">
          <option value="ONLINE">Thanh toán qua <c:out value="${onlinePaymentDisplayName}"/></option>
        </c:when>
        <c:otherwise><option value="ONLINE" disabled>Online — chưa cấu hình</option></c:otherwise>
      </c:choose>
    </select>
  </c:if>
  <p>
    <c:choose>
      <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER' && not onlinePaymentAvailable}">
        <button class="btn btn-success" type="button" style="width:100%" disabled>Chưa cấu hình thanh toán online</button>
      </c:when>
      <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER'}">
        <button class="btn btn-success" type="submit" style="width:100%">Thanh toán qua <c:out value="${onlinePaymentDisplayName}"/></button>
      </c:when>
      <c:otherwise><button class="btn btn-success" type="submit" style="width:100%">Ghi nhận thanh toán</button></c:otherwise>
    </c:choose>
  </p>
</form>
</div>
<%@ include file="_footer.jspf" %>
