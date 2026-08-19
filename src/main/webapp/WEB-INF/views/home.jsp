<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>${hotel.hotelName}</h2>
  <p>${hotel.description}</p>
  <p>📍 ${hotel.address} &nbsp; ☎ ${hotel.phone} &nbsp; ✉ ${hotel.email}</p>
  <p>🕐 Nhận phòng từ <b>${hotel.checkInTime}</b> — Trả phòng trước <b>${hotel.checkOutTime}</b></p>
</div>
<div class="card">
  <h3>Các loại phòng</h3>
  <table>
    <tr><th>Loại phòng</th><th>Giường</th><th>Sức chứa</th><th>Giá cơ bản/đêm</th><th></th></tr>
    <c:forEach var="t" items="${roomTypes}">
      <tr>
        <td>${t.typeName}</td>
        <td>${t.bedType}</td>
        <td>${t.maxAdults} NL + ${t.maxChildren} TE</td>
        <td><fmt:formatNumber value="${t.basePrice}"/> đ</td>
        <td><a class="btn" href="${pageContext.request.contextPath}/rooms/detail?id=${t.roomTypeId}">Chi tiết</a></td>
      </tr>
    </c:forEach>
  </table>
</div>
<%@ include file="_footer.jspf" %>
