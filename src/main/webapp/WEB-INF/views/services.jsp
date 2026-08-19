<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<c:if test="${param.ok == '1'}"><div class="msg">Đã gửi yêu cầu dịch vụ!</div></c:if>
<div class="card">
  <h2>Dịch vụ khách sạn</h2>
  <table>
    <tr><th>Dịch vụ</th><th>Mô tả</th><th>Đơn vị</th><th>Đơn giá</th></tr>
    <c:forEach var="s" items="${catalog}">
      <tr><td><b>${s.serviceName}</b></td><td>${s.description}</td><td>${s.unitName}</td>
          <td><fmt:formatNumber value="${s.unitPrice}"/> đ</td></tr>
    </c:forEach>
  </table>
</div>
<div class="card" style="max-width:560px">
  <h3>Tạo yêu cầu dịch vụ</h3>
  <c:choose>
    <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER' && empty activeReservations}">
      <p>Bạn cần đang lưu trú (đã check-in) để yêu cầu dịch vụ.</p>
    </c:when>
    <c:otherwise>
      <form method="post" action="${pageContext.request.contextPath}/services">
        <label>Đơn lưu trú</label>
        <c:choose>
          <c:when test="${sessionScope.currentUser.roleCode == 'CUSTOMER'}">
            <select name="reservationId" style="width:100%">
              <c:forEach var="rv" items="${activeReservations}">
                <option value="${rv.reservationId}">${rv.bookingCode} (${rv.checkInDate} → ${rv.checkOutDate})</option>
              </c:forEach>
            </select>
          </c:when>
          <c:otherwise>
            <input type="number" name="reservationId" placeholder="ID đơn CHECKED_IN" style="width:100%" required>
          </c:otherwise>
        </c:choose>
        <label>Dịch vụ</label>
        <select name="hotelServiceId" style="width:100%">
          <c:forEach var="s" items="${catalog}">
            <option value="${s.hotelServiceId}">${s.serviceName} — <fmt:formatNumber value="${s.unitPrice}"/> đ/${s.unitName}</option>
          </c:forEach>
        </select>
        <label>Số lượng</label><input type="number" name="quantity" value="1" step="0.01" min="0.01" style="width:100%" required>
        <label>Thời gian mong muốn</label><input type="datetime-local" name="scheduledAt" style="width:100%">
        <label>Ghi chú</label><textarea name="notes" rows="2" style="width:100%"></textarea>
        <p><button class="btn btn-success" type="submit">Gửi yêu cầu</button></p>
      </form>
    </c:otherwise>
  </c:choose>
</div>
<%@ include file="_footer.jspf" %>
