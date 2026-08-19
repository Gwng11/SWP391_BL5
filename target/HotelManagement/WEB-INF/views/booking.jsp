<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:640px">
  <h2>Đặt phòng: ${roomType.typeName}</h2>
  <p>Giá cơ bản: <b><fmt:formatNumber value="${roomType.basePrice}"/> đ/đêm</b>
     — Sức chứa: ${roomType.maxAdults} NL + ${roomType.maxChildren} TE / phòng</p>
  <form method="post" action="${pageContext.request.contextPath}/booking">
    <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
    <label>Nhận phòng</label><input type="date" name="checkIn" value="${param.checkIn}" required style="width:100%">
    <label>Trả phòng</label><input type="date" name="checkOut" value="${param.checkOut}" required style="width:100%">
    <label>Số phòng</label><input type="number" name="quantity" min="1" value="1" style="width:100%">
    <label>Người lớn</label><input type="number" name="adults" min="1" value="${empty param.adults ? 1 : param.adults}" style="width:100%">
    <label>Trẻ em</label><input type="number" name="children" min="0" value="${empty param.children ? 0 : param.children}" style="width:100%">
    <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
      <label>Mã khách hàng (customer_id — tra ở mục Khách hàng)</label>
      <input type="number" name="customerId" required style="width:100%">
    </c:if>
    <label>Tên khách ở chính</label><input name="primaryGuestName" style="width:100%">
    <label>Yêu cầu đặc biệt</label><textarea name="specialRequests" style="width:100%" rows="3"></textarea>
    <p>Tạm tính = giá theo ngày × số đêm × số phòng + 10% thuế. Cọc 30% để xác nhận đơn.</p>
    <button class="btn btn-success" type="submit">Xác nhận đặt phòng</button>
  </form>
</div>
<%@ include file="_footer.jspf" %>
