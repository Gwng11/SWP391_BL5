<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Tìm phòng trống</h2>
  <form method="get" action="${pageContext.request.contextPath}/rooms" style="display:flex;gap:10px;align-items:end;flex-wrap:wrap">
    <div><label>Nhận phòng</label><input type="date" name="checkIn" value="${checkIn}" required></div>
    <div><label>Trả phòng</label><input type="date" name="checkOut" value="${checkOut}" required></div>
    <div><label>Người lớn</label><input type="number" name="adults" min="1" value="${adults}"></div>
    <div><label>Trẻ em</label><input type="number" name="children" min="0" value="${children}"></div>
    <button class="btn" type="submit">Tìm kiếm</button>
  </form>
</div>
<div class="card">
  <div class="page-head"><h3>Kết quả (${totalResults} loại phòng còn trống)</h3><span class="muted">Tối đa 25 dòng/trang</span></div>
  <c:choose><c:when test="${empty results}"><div class="empty">Không có phòng phù hợp trong khoảng ngày đã chọn.</div></c:when><c:otherwise>
  <table>
    <tr><th>Loại phòng</th><th>Còn trống</th><th>Giá TB/đêm</th><th>Tổng ${nights} đêm/phòng</th><th></th></tr>
    <c:forEach var="a" items="${results}">
      <tr>
        <td>${a.roomType.typeName}</td>
        <td>${a.availableRooms} phòng</td>
        <td><fmt:formatNumber value="${a.nightlyAvgPrice}"/> đ</td>
        <td><fmt:formatNumber value="${a.totalPricePerRoom}"/> đ</td>
        <td>
          <a class="btn" href="${pageContext.request.contextPath}/rooms/detail?id=${a.roomType.roomTypeId}">Chi tiết</a>
          <a class="btn btn-success" href="${pageContext.request.contextPath}/booking?roomTypeId=${a.roomType.roomTypeId}&checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}">Đặt ngay</a>
        </td>
      </tr>
    </c:forEach>
  </table>
  <c:if test="${totalPages > 1}"><div class="pagination">
    <c:if test="${currentPage > 1}"><a class="btn btn-muted btn-small" href="?checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}&page=${currentPage-1}">‹ Trang trước</a></c:if>
    <span>Trang ${currentPage}/${totalPages}</span>
    <c:if test="${currentPage < totalPages}"><a class="btn btn-small" href="?checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}&page=${currentPage+1}">Trang sau ›</a></c:if>
  </div></c:if>
  </c:otherwise></c:choose>
</div>
<%@ include file="_footer.jspf" %>
