<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Check-in (đơn CONFIRMED)</h2>
  <form method="get" action="${pageContext.request.contextPath}/reception/checkin">
    <input name="q" placeholder="Mã đơn / tên khách / SĐT" value="${param.q}" style="width:300px">
    <button class="btn" type="submit">Tìm</button>
  </form>
  <table style="margin-top:12px">
    <tr><th>Mã đơn</th><th>Khách</th><th>Nhận</th><th>Trả</th><th>Tổng</th><th></th></tr>
    <c:forEach var="rv" items="${reservations}">
      <tr><td>${rv.bookingCode}</td><td>${rv.customerName}</td><td>${rv.checkInDate}</td><td>${rv.checkOutDate}</td>
          <td><fmt:formatNumber value="${rv.totalAmount}"/> đ</td>
          <td><a class="btn" href="?id=${rv.reservationId}">Chọn</a></td></tr>
    </c:forEach>
  </table>
</div>
<c:if test="${r != null}">
<div class="card">
  <h3>Xác nhận check-in: ${r.bookingCode}</h3>
  <p>Khách: <b>${r.customerName}</b> — Cọc yêu cầu <fmt:formatNumber value="${r.depositRequired}"/> đ,
     đã nộp <b><fmt:formatNumber value="${depositPaid}"/> đ</b></p>
  <c:if test="${lateDays != null && lateDays > 0}">
    <div class="err">⚠ Khách đến muộn ${lateDays} ngày so với ngày nhận phòng (${r.checkInDate}).
      Vẫn check-in được, hoặc đánh dấu "Không đến" ở màn Đơn đặt phòng.</div>
  </c:if>
  <h4>Danh sách khách ở (đối chiếu giấy tờ — khách CHÍNH bắt buộc có giấy tờ)</h4>
  <form method="post" action="${pageContext.request.contextPath}/reception/checkin">
    <input type="hidden" name="id" value="${r.reservationId}">
    <input type="hidden" name="action" value="saveGuests">
    <table>
      <tr><th>Họ tên</th><th>Loại giấy tờ</th><th>Số giấy tờ</th><th>Khách chính</th></tr>
      <c:forEach var="g" items="${guests}" varStatus="st">
        <tr>
          <td><input name="gName" value="${g.fullName}" style="width:180px"></td>
          <td><select name="gDocType">
                <option value="" ${empty g.idDocumentType ? 'selected' : ''}>--</option>
                <option value="CCCD" ${g.idDocumentType == 'CCCD' ? 'selected' : ''}>CCCD</option>
                <option value="PASSPORT" ${g.idDocumentType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
              </select></td>
          <td><input name="gDocNo" value="${g.idDocumentNumber}" style="width:150px"></td>
          <td style="text-align:center"><input type="radio" name="primaryIdx" value="${st.index}"
                ${g.primaryGuest ? 'checked' : ''}></td>
        </tr>
      </c:forEach>
      <%-- 2 dòng trống để bổ sung khách mới --%>
      <tr>
        <td><input name="gName" placeholder="Thêm khách..." style="width:180px"></td>
        <td><select name="gDocType"><option value="">--</option>
              <option value="CCCD">CCCD</option><option value="PASSPORT">Hộ chiếu</option></select></td>
        <td><input name="gDocNo" style="width:150px"></td>
        <td style="text-align:center"><input type="radio" name="primaryIdx" value="${guests.size()}"
              ${empty guests ? 'checked' : ''}></td>
      </tr>
      <tr>
        <td><input name="gName" placeholder="Thêm khách..." style="width:180px"></td>
        <td><select name="gDocType"><option value="">--</option>
              <option value="CCCD">CCCD</option><option value="PASSPORT">Hộ chiếu</option></select></td>
        <td><input name="gDocNo" style="width:150px"></td>
        <td style="text-align:center"><input type="radio" name="primaryIdx" value="${guests.size() + 1}"></td>
      </tr>
    </table>
    <button class="btn" type="submit" style="margin-top:6px">💾 Lưu danh sách khách</button>
  </form>
  <h4>Tình trạng phòng sạch sẵn sàng để gán</h4>
  <table><tr><th>Loại phòng</th><th>Cần</th><th>Sẵn sàng</th><th></th></tr>
  <c:forEach var="l" items="${lines}">
    <tr>
      <td>${l.typeName}</td><td>${l.quantity}</td><td>${readyMap[l.reservationRoomId]}</td>
      <td>
        <c:choose>
          <c:when test="${readyMap[l.reservationRoomId] < l.quantity}">
            <b style="color:#c0392b">⚠ KHÔNG ĐỦ — cần dọn phòng / xử lý bảo trì trước</b>
          </c:when>
          <c:otherwise><span style="color:#1e8449">✔ Đủ</span></c:otherwise>
        </c:choose>
      </td>
    </tr>
  </c:forEach></table>
  <form method="post" action="${pageContext.request.contextPath}/reception/checkin" style="margin-top:12px">
    <input type="hidden" name="id" value="${r.reservationId}">
    <button class="btn btn-success" type="submit">✔ Check-in & chuyển sang gán phòng</button>
  </form>
  <p style="color:#636e72;font-size:13px">* Hệ thống sẽ từ chối check-in nếu không đủ phòng sạch sẵn sàng để gán.</p>
</div>
</c:if>
<%@ include file="_footer.jspf" %>
