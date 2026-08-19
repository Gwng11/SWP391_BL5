<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Tìm phòng trống</h2>
  <form method="get" action="${pageContext.request.contextPath}/rooms" style="display:flex;gap:10px;align-items:end;flex-wrap:wrap">
    <div><label>Nhận phòng</label><input type="date" name="checkIn" value="${param.checkIn}" required></div>
    <div><label>Trả phòng</label><input type="date" name="checkOut" value="${param.checkOut}" required></div>
    <div><label>Người lớn</label><input type="number" name="adults" min="1" value="${empty param.adults ? 1 : param.adults}"></div>
    <div><label>Trẻ em</label><input type="number" name="children" min="0" value="${empty param.children ? 0 : param.children}"></div>
    <button class="btn" type="submit">Tìm kiếm</button>
  </form>
</div>
<c:if test="${results != null}">
<div class="card">
  <h3>Kết quả (${results.size()} loại phòng còn trống)</h3>
  <table>
    <tr><th>Loại phòng</th><th>Còn trống</th><th>Giá TB/đêm</th><th>Tổng ${results[0].nights} đêm/phòng</th><th></th></tr>
    <c:forEach var="a" items="${results}">
      <tr>
        <td>${a.roomType.typeName}</td>
        <td>${a.availableRooms} phòng</td>
        <td><fmt:formatNumber value="${a.nightlyAvgPrice}"/> đ</td>
        <td><fmt:formatNumber value="${a.totalPricePerRoom}"/> đ</td>
        <td>
          <a class="btn" href="${pageContext.request.contextPath}/rooms/detail?id=${a.roomType.roomTypeId}">Chi tiết</a>
          <a class="btn btn-success" href="${pageContext.request.contextPath}/booking?roomTypeId=${a.roomType.roomTypeId}&checkIn=${param.checkIn}&checkOut=${param.checkOut}&adults=${param.adults}&children=${param.children}">Đặt ngay</a>
        </td>
      </tr>
    </c:forEach>
  </table>
</div>
</c:if>
<%@ include file="_footer.jspf" %>
