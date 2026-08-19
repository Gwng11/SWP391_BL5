<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Gán phòng — Đơn ${r.bookingCode} <span class="badge">${r.statusCode}</span></h2>
  <c:forEach var="rr" items="${rooms}">
    <h4>${rr.typeName} — cần ${rr.quantity} phòng</h4>
    <form method="post" action="${pageContext.request.contextPath}/reception/assign">
      <input type="hidden" name="reservationId" value="${r.reservationId}">
      <input type="hidden" name="reservationRoomId" value="${rr.reservationRoomId}">
      <select name="roomId" required>
        <option value="">-- chọn phòng trống & sạch --</option>
        <c:forEach var="rm" items="${assignableMap[rr.reservationRoomId]}">
          <option value="${rm.roomId}">Phòng ${rm.roomNumber} (tầng ${rm.floorNumber}, ${rm.cleaningStatus})</option>
        </c:forEach>
      </select>
      <button class="btn" type="submit">Gán phòng</button>
    </form>
  </c:forEach>
</div>
<div class="card">
  <h3>Phòng đang gán</h3>
  <table><tr><th>Phòng</th><th>Loại</th><th>Gán lúc</th><th>Đổi phòng</th></tr>
  <c:forEach var="a" items="${assignments}">
    <tr>
      <td><b>${a.roomNumber}</b></td><td>${a.typeName}</td><td>${a.assignedAt}</td>
      <td>
        <form method="post" action="${pageContext.request.contextPath}/reception/assign" style="display:flex;gap:6px">
          <input type="hidden" name="reservationId" value="${r.reservationId}">
          <input type="hidden" name="action" value="change">
          <input type="hidden" name="assignmentId" value="${a.roomAssignmentId}">
          <select name="newRoomId" required>
            <option value="">-- chọn phòng mới --</option>
            <c:forEach var="nr" items="${assignableMap[a.reservationRoomId]}">
              <option value="${nr.roomId}">Phòng ${nr.roomNumber} (tầng ${nr.floorNumber}, ${nr.cleaningStatus})</option>
            </c:forEach>
          </select>
          <input name="reason" placeholder="Lý do" maxlength="255" style="width:140px">
          <button class="btn" type="submit">Đổi</button>
        </form>
      </td>
    </tr>
  </c:forEach></table>
</div>
<div class="card">
  <h3>Lịch sử gán phòng</h3>
  <table><tr><th>Phòng</th><th>Gán lúc</th><th>Trả lúc</th><th>Lý do</th></tr>
  <c:forEach var="h" items="${history}">
    <tr><td>${h.roomNumber}</td><td>${h.assignedAt}</td><td>${h.unassignedAt}</td><td>${h.unassignedReason}</td></tr>
  </c:forEach></table>
</div>
<%@ include file="_footer.jspf" %>
