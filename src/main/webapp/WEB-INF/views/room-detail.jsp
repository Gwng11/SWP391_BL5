<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>${roomType.typeName} <span class="badge">${roomType.typeCode}</span></h2>
  <p>${roomType.description}</p>
  <table style="max-width:520px">
    <tr><th>Giường</th><td>${roomType.bedType}</td></tr>
    <tr><th>Diện tích</th><td>${roomType.roomSizeM2} m²</td></tr>
    <tr><th>Sức chứa</th><td>${roomType.maxAdults} người lớn + ${roomType.maxChildren} trẻ em</td></tr>
    <tr><th>Giá cơ bản</th><td><fmt:formatNumber value="${roomType.basePrice}"/> đ/đêm</td></tr>
    <tr><th>Tiện nghi</th><td>${roomType.amenitiesJson}</td></tr>
  </table>
  <p style="margin-top:14px">
    <a class="btn btn-success" href="${pageContext.request.contextPath}/booking?roomTypeId=${roomType.roomTypeId}">Đặt phòng này</a>
    <a class="btn" href="${pageContext.request.contextPath}/rooms">← Quay lại</a>
  </p>
</div>
<%@ include file="_footer.jspf" %>
