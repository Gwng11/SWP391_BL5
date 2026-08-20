<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="page-head"><h1>Quản lý phòng vật lý</h1><a class="btn" href="${pageContext.request.contextPath}/manager/dashboard">Dashboard</a></div>
<div class="card">
  <form method="get" class="inline" aria-label="Lọc phòng">
    <select name="roomTypeId"><option value="">Tất cả loại phòng</option><c:forEach var="t" items="${roomTypes}"><option value="${t.roomTypeId}" ${param.roomTypeId == t.roomTypeId ? 'selected' : ''}><c:out value="${t.typeName}"/></option></c:forEach></select>
    <input type="number" name="floor" value="${param.floor}" placeholder="Tầng">
    <select name="status"><option value="">Tất cả trạng thái</option><c:forEach var="s" items="AVAILABLE,OCCUPIED,MAINTENANCE,BLOCKED,OUT_OF_SERVICE,INACTIVE"><option value="${s}" ${param.status == s ? 'selected' : ''}>${s}</option></c:forEach></select>
    <button class="btn" type="submit">Lọc</button><a class="btn btn-muted" href="${pageContext.request.contextPath}/manager/rooms">Xóa lọc</a>
  </form>
</div>
<div class="grid">
  <div class="card col-8 table-wrap">
    <h2>Danh sách phòng</h2>
    <c:choose><c:when test="${empty rooms}"><div class="empty">Không có phòng phù hợp.</div></c:when><c:otherwise>
      <table><thead><tr><th>Phòng</th><th>Tầng</th><th>Loại</th><th>Vận hành</th><th>Cleaning</th><th>Thao tác</th></tr></thead><tbody>
      <c:forEach var="r" items="${rooms}"><tr><td><b><c:out value="${r.roomNumber}"/></b></td><td>${r.floorNumber}</td><td><c:out value="${r.typeName}"/></td><td><span class="badge status-${r.operationalStatus}">${r.operationalStatus}</span></td><td><span class="badge status-${r.cleaningStatus}">${r.cleaningStatus}</span></td><td>
        <a class="btn btn-small" href="?edit=${r.roomId}">Sửa</a>
        <c:if test="${r.operationalStatus != 'OCCUPIED'}"><form method="post" class="inline"><input type="hidden" name="action" value="status"><input type="hidden" name="roomId" value="${r.roomId}"><select name="status"><option>AVAILABLE</option><option>BLOCKED</option><option>OUT_OF_SERVICE</option><option>INACTIVE</option></select><button class="btn btn-small" type="submit">Đổi</button></form></c:if>
      </td></tr></c:forEach></tbody></table>
    </c:otherwise></c:choose>
  </div>
  <div class="card col-4"><h2>${empty editRoom ? 'Tạo phòng' : 'Cập nhật phòng'}</h2>
    <form method="post"><c:if test="${not empty editRoom}"><input type="hidden" name="roomId" value="${editRoom.roomId}"></c:if>
      <label>Số phòng *</label><input name="roomNumber" required maxlength="20" value="${editRoom.roomNumber}" style="width:100%">
      <label>Loại phòng *</label><select name="roomTypeId" required style="width:100%"><c:forEach var="t" items="${roomTypes}"><option value="${t.roomTypeId}" ${editRoom.roomTypeId == t.roomTypeId ? 'selected' : ''}><c:out value="${t.typeName}"/></option></c:forEach></select>
      <label>Tầng</label><input type="number" name="floorNumber" value="${editRoom.floorNumber}" style="width:100%">
      <label>Ghi chú</label><textarea name="notes" maxlength="500" rows="4" style="width:100%"><c:out value="${editRoom.notes}"/></textarea>
      <div class="form-actions"><button class="btn" type="submit">Lưu</button><c:if test="${not empty editRoom}"><a href="${pageContext.request.contextPath}/manager/rooms">Hủy</a></c:if></div>
    </form>
  </div>
</div>
<%@ include file="_footer.jspf" %>
