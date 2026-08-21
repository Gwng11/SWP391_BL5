<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🔍 Tìm kiếm Phòng trống</h1>
    <p>Tìm và chọn phòng khách sạn phù hợp nhất cho kỳ nghỉ của bạn</p>
  </div>
</div>

<!-- Search & Filter Box -->
<div class="bk-search-box">
  <form method="get" action="${pageContext.request.contextPath}/rooms" class="bk-search-form">
    <div style="flex: 1; min-width: 150px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">📅 Nhận phòng</label>
      <input type="date" name="checkIn" value="${checkIn}" required style="width:100%">
    </div>
    <div style="flex: 1; min-width: 150px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">📅 Trả phòng</label>
      <input type="date" name="checkOut" value="${checkOut}" required style="width:100%">
    </div>
    <div style="width: 120px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">👥 Người lớn</label>
      <input type="number" name="adults" min="1" value="${adults}" style="width:100%">
    </div>
    <div style="width: 120px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🧒 Trẻ em</label>
      <input type="number" name="children" min="0" value="${children}" style="width:100%">
    </div>
    <div style="margin-top: auto;">
      <button class="btn" type="submit" style="height: 38px;">🔍 Tìm kiếm phòng</button>
    </div>
  </form>
</div>

<!-- Results Card -->
<div class="card">
  <div class="page-head" style="margin-bottom: 20px; border-bottom: 1px solid var(--bk-border); padding-bottom: 12px;">
    <h2 style="margin: 0; font-size: 18px;">📋 Kết quả (${totalResults} loại phòng còn trống)</h2>
    <span class="muted" style="font-size: 13px;">Hiển thị tối đa 25 dòng/trang</span>
  </div>

  <c:choose>
    <c:when test="${empty results}">
      <div class="empty" style="text-align:center; padding:40px 20px; color:var(--bk-muted); font-size: 15px;">
        📭 Không có phòng phù hợp trong khoảng ngày đã chọn. Vui lòng thử lại với ngày khác.
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Loại phòng</th>
              <th>Trạng thái tồn</th>
              <th style="text-align: right;">Giá TB/đêm</th>
              <th style="text-align: right;">Tổng (${nights} đêm)</th>
              <th style="text-align: right;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="a" items="${results}">
              <tr>
                <td>
                  <div style="font-weight:600; font-size:15px; color:var(--bk-navy);">${a.roomType.typeName}</div>
                  <small class="muted">Mã: ${a.roomType.typeCode}</small>
                </td>
                <td>
                  <span class="badge status-ACTIVE" style="background:#def7ec; color:#03543f;">Còn trống ${a.availableRooms} phòng</span>
                </td>
                <td style="text-align: right; font-weight: 600; color: var(--bk-text);">
                  <fmt:formatNumber value="${a.nightlyAvgPrice}"/> đ
                </td>
                <td style="text-align: right; font-weight: 700; color: var(--bk-blue); font-size: 15px;">
                  <fmt:formatNumber value="${a.totalPricePerRoom}"/> đ
                </td>
                <td style="text-align: right; white-space: nowrap;">
                  <a class="btn btn-muted btn-small" href="${pageContext.request.contextPath}/rooms/detail?id=${a.roomType.roomTypeId}">ℹ Chi tiết</a>
                  <a class="btn btn-success btn-small" href="${pageContext.request.contextPath}/booking?roomTypeId=${a.roomType.roomTypeId}&checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}">⚡ Đặt ngay</a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>

      <c:if test="${totalPages > 1}">
        <div class="pagination" style="display:flex; justify-content:center; align-items:center; gap:16px; margin-top:24px;">
          <c:if test="${currentPage > 1}">
            <a class="btn btn-muted btn-small" href="?checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}&page=${currentPage-1}">‹ Trang trước</a>
          </c:if>
          <span style="font-weight: 600; color: var(--bk-muted);">Trang ${currentPage} / ${totalPages}</span>
          <c:if test="${currentPage < totalPages}">
            <a class="btn btn-muted btn-small" href="?checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}&page=${currentPage+1}">Trang sau ›</a>
          </c:if>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="_footer.jspf" %>
