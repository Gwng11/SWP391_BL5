<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="page-head"><h1>Room Pricing</h1><a class="btn" href="${pageContext.request.contextPath}/manager/dashboard">Dashboard</a></div>

<div class="card">
  <form method="get" class="inline">
    <label for="type">Loại phòng</label>
    <select id="type" name="roomTypeId">
      <option value="">Tất cả loại phòng đang hoạt động</option>
      <c:forEach var="t" items="${roomTypes}"><option value="${t.roomTypeId}" ${param.roomTypeId == t.roomTypeId ? 'selected' : ''}><c:out value="${t.typeName}"/></option></c:forEach>
    </select>
    <label>Từ</label><input type="date" name="from" value="${from}">
    <label>Đến</label><input type="date" name="to" value="${to}">
    <button class="btn">Xem</button>
  </form>
</div>

<div class="card table-wrap">
  <div class="page-head"><h2>Bảng giá loại phòng</h2><span class="muted">${totalItems} loại phòng đang hoạt động · tối đa 25 dòng/trang</span></div>
  <c:choose>
    <c:when test="${empty pricingTypes}"><div class="empty">Không có loại phòng đang hoạt động.</div></c:when>
    <c:otherwise>
      <table><thead><tr><th>Mã</th><th>Loại phòng</th><th>Sức chứa</th><th>Giá cơ bản</th><th></th></tr></thead><tbody>
      <c:forEach var="t" items="${pricingTypes}"><tr>
        <td><b><c:out value="${t.typeCode}"/></b></td><td><c:out value="${t.typeName}"/></td>
        <td>${t.maxAdults} người lớn, ${t.maxChildren} trẻ em</td>
        <td><fmt:formatNumber value="${t.basePrice}" pattern="#,##0"/> đ</td>
        <td><a class="btn btn-small" href="${pageContext.request.contextPath}/manager/pricing?roomTypeId=${t.roomTypeId}&from=${from}&to=${to}&page=${currentPage}">Xem / cập nhật</a></td>
      </tr></c:forEach></tbody></table>
      <c:if test="${totalPages > 1}"><div class="pagination">
        <c:if test="${currentPage > 1}"><a class="btn btn-muted btn-small" href="?page=${currentPage-1}&from=${from}&to=${to}">‹ Trang trước</a></c:if>
        <span>Trang ${currentPage}/${totalPages}</span>
        <c:if test="${currentPage < totalPages}"><a class="btn btn-small" href="?page=${currentPage+1}&from=${from}&to=${to}">Trang sau ›</a></c:if>
      </div></c:if>
    </c:otherwise>
  </c:choose>
</div>

<c:if test="${not empty selectedType}">
<div class="grid"><div class="card col-4"><h2>Cấu hình giá</h2><p><b><c:out value="${selectedType.typeName}"/></b></p>
  <form method="post"><input type="hidden" name="roomTypeId" value="${selectedType.roomTypeId}"><label>Base price *</label><input type="number" name="basePrice" min="0" step="1000" required value="${selectedType.basePrice}" style="width:100%"><hr>
    <p class="muted">Để trống toàn bộ phần dưới nếu chỉ cập nhật base price.</p><label>Từ ngày</label><input type="date" name="from" style="width:100%"><label>Đến ngày</label><input type="date" name="to" style="width:100%"><label>Nightly price</label><input type="number" name="nightlyPrice" min="0" step="1000" style="width:100%"><label><input type="checkbox" name="stopSell"> Stop-sell trong khoảng ngày</label><div class="form-actions"><button class="btn" type="submit">Lưu giá</button></div>
  </form></div>
  <div class="card col-8 table-wrap"><div class="page-head"><h2>Giá theo ngày</h2><span class="muted">${rateTotalItems} mức giá riêng</span></div><c:choose><c:when test="${empty rates}"><div class="empty">Không có giá riêng trong khoảng đã chọn; hệ thống dùng base price.</div></c:when><c:otherwise><table><thead><tr><th>Ngày</th><th>Giá</th><th>Sales</th></tr></thead><tbody><c:forEach var="r" items="${rates}"><tr><td>${r.rateDate}</td><td><fmt:formatNumber value="${r.nightlyPrice}" pattern="#,##0"/> đ</td><td>${r.stopSell ? 'STOP-SELL' : 'OPEN'}</td></tr></c:forEach></tbody></table>
    <c:if test="${rateTotalPages > 1}"><div class="pagination">
      <c:if test="${rateCurrentPage > 1}"><a class="btn btn-muted btn-small" href="?roomTypeId=${selectedType.roomTypeId}&from=${from}&to=${to}&page=${currentPage}&ratePage=${rateCurrentPage-1}">‹ Trang trước</a></c:if>
      <span>Trang ${rateCurrentPage}/${rateTotalPages}</span>
      <c:if test="${rateCurrentPage < rateTotalPages}"><a class="btn btn-small" href="?roomTypeId=${selectedType.roomTypeId}&from=${from}&to=${to}&page=${currentPage}&ratePage=${rateCurrentPage+1}">Trang sau ›</a></c:if>
    </div></c:if>
  </c:otherwise></c:choose></div>
</div></c:if>
<%@ include file="_footer.jspf" %>
