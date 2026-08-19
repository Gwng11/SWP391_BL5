<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Quản lý đơn đặt phòng</h2>
  <form method="get" action="${pageContext.request.contextPath}/reception/reservations"
        style="display:flex;gap:8px;flex-wrap:wrap">
    <input name="q" value="${q}" placeholder="Mã đơn / tên khách / SĐT" style="width:280px">
    <select name="status">
      <option value="">-- tất cả trạng thái --</option>
      <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>PENDING (chờ cọc)</option>
      <option value="CONFIRMED" ${statusFilter == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
      <option value="CHECKED_IN" ${statusFilter == 'CHECKED_IN' ? 'selected' : ''}>CHECKED_IN</option>
      <option value="CHECKED_OUT" ${statusFilter == 'CHECKED_OUT' ? 'selected' : ''}>CHECKED_OUT</option>
      <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
      <option value="NO_SHOW" ${statusFilter == 'NO_SHOW' ? 'selected' : ''}>NO_SHOW</option>
    </select>
    <button class="btn" type="submit">Tìm kiếm</button>
  </form>
  <table style="margin-top:12px">
    <tr><th>Mã đơn</th><th>Khách</th><th>Nhận</th><th>Trả</th><th>Tổng tiền</th><th>Cọc Y/C</th><th>Trạng thái</th><th>Hành động</th></tr>
    <c:forEach var="rv" items="${reservations}">
      <tr>
        <td>${rv.bookingCode}</td>
        <td>${rv.customerName}</td>
        <td>${rv.checkInDate}</td><td>${rv.checkOutDate}</td>
        <td><fmt:formatNumber value="${rv.totalAmount}"/> đ</td>
        <td><fmt:formatNumber value="${rv.depositRequired}"/> đ</td>
        <td><span class="badge">${rv.statusCode}</span></td>
        <td>
          <a class="btn" style="padding:3px 8px;font-size:12px"
             href="${pageContext.request.contextPath}/reservation?id=${rv.reservationId}">Xem</a>
          <c:if test="${rv.statusCode == 'PENDING'}">
            <a class="btn btn-success" style="padding:3px 8px;font-size:12px"
               href="${pageContext.request.contextPath}/deposit?reservationId=${rv.reservationId}">💰 Thu cọc</a>
          </c:if>
          <c:if test="${rv.statusCode == 'CONFIRMED'}">
            <a class="btn btn-success" style="padding:3px 8px;font-size:12px"
               href="${pageContext.request.contextPath}/reception/checkin?id=${rv.reservationId}">Check-in</a>
            <form method="post" style="display:inline"
                  action="${pageContext.request.contextPath}/reception/reservations"
                  onsubmit="return confirm('Xác nhận khách KHÔNG ĐẾN? Đơn sẽ chuyển NO_SHOW và trả lại tồn phòng.')">
              <input type="hidden" name="id" value="${rv.reservationId}">
              <input type="hidden" name="action" value="noshow">
              <button class="btn btn-danger" style="padding:3px 8px;font-size:12px" type="submit">Không đến</button>
            </form>
          </c:if>
          <c:if test="${rv.statusCode == 'CHECKED_IN'}">
            <a class="btn" style="padding:3px 8px;font-size:12px"
               href="${pageContext.request.contextPath}/reception/assign?reservationId=${rv.reservationId}">Phòng</a>
            <a class="btn" style="padding:3px 8px;font-size:12px"
               href="${pageContext.request.contextPath}/reception/invoice?reservationId=${rv.reservationId}">Hóa đơn</a>
          </c:if>
        </td>
      </tr>
    </c:forEach>
  </table>
  <p style="color:#636e72;font-size:13px">Hiển thị tối đa 100 đơn mới nhất — dùng ô tìm kiếm để thu hẹp.</p>
</div>
<%@ include file="_footer.jspf" %>
