<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:640px">
  <h2>Đặt phòng: ${roomType.typeName}</h2>
  <p>Giá cơ bản: <b><fmt:formatNumber value="${roomType.basePrice}"/> đ/đêm</b>
     — Sức chứa: ${roomType.maxAdults} NL + ${roomType.maxChildren} TE / phòng</p>
  <c:set var="bookingCtx" value="roomTypeId=${roomType.roomTypeId}&checkIn=${param.checkIn}&checkOut=${param.checkOut}&adults=${param.adults}&children=${param.children}"/>
  <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
    <div style="background:#f8f9fa;border:1px solid #e0e0e0;border-radius:6px;padding:14px;margin-bottom:16px">
      <h3 style="margin:0 0 8px">👤 Khách hàng</h3>
      <c:choose>
        <%-- ĐÃ CHỌN KHÁCH --%>
        <c:when test="${selectedCustomer != null}">
          <p style="margin:6px 0">
            ✔ <b>${selectedCustomer.fullName}</b> — ${selectedCustomer.customerCode}
            <c:if test="${not empty selectedCustomer.phone}"> — 📞 ${selectedCustomer.phone}</c:if>
            <c:if test="${not empty selectedCustomer.idDocumentNumber}"> — ${selectedCustomer.idDocumentType} ${selectedCustomer.idDocumentNumber}</c:if>
            &nbsp;<a class="btn" style="padding:3px 10px;font-size:12px"
                 href="${pageContext.request.contextPath}/booking?${bookingCtx}">Đổi khách khác</a>
          </p>
        </c:when>
        <%-- CHƯA CHỌN: tìm kiếm --%>
        <c:otherwise>
          <form method="get" action="${pageContext.request.contextPath}/booking" style="display:flex;gap:8px;flex-wrap:wrap">
            <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
            <input type="hidden" name="checkIn" value="${param.checkIn}">
            <input type="hidden" name="checkOut" value="${param.checkOut}">
            <input type="hidden" name="adults" value="${param.adults}">
            <input type="hidden" name="children" value="${param.children}">
            <input name="q" value="${param.q}" placeholder="SĐT / số CCCD / tên / mã KH" style="width:300px" required>
            <button class="btn" type="submit">🔍 Tìm khách</button>
          </form>
          <c:if test="${customerResults != null}">
            <c:choose>
              <c:when test="${not empty customerResults}">
                <table style="margin-top:10px">
                  <tr><th>Mã KH</th><th>Họ tên</th><th>SĐT</th><th>Giấy tờ</th><th></th></tr>
                  <c:forEach var="c" items="${customerResults}">
                    <tr>
                      <td>${c.customerCode}</td><td>${c.fullName}</td><td>${c.phone}</td>
                      <td>${c.idDocumentType} ${c.idDocumentNumber}</td>
                      <td><a class="btn btn-success" style="padding:3px 10px;font-size:12px"
                             href="${pageContext.request.contextPath}/booking?${bookingCtx}&customerId=${c.customerId}">Chọn</a></td>
                    </tr>
                  </c:forEach>
                </table>
              </c:when>
              <c:otherwise>
                <p style="color:#c0392b;margin:8px 0 4px">Không tìm thấy khách nào khớp "${param.q}".</p>
              </c:otherwise>
            </c:choose>
            <%-- Tạo nhanh hồ sơ mới ngay tại chỗ --%>
            <details style="margin-top:8px" ${empty customerResults ? 'open' : ''}>
              <summary style="cursor:pointer;color:#2980b9">+ Tạo nhanh hồ sơ khách mới</summary>
              <form method="post" action="${pageContext.request.contextPath}/booking"
                    style="display:flex;gap:8px;flex-wrap:wrap;align-items:end;margin-top:8px">
                <input type="hidden" name="action" value="createCustomer">
                <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
                <input type="hidden" name="checkIn" value="${param.checkIn}">
                <input type="hidden" name="checkOut" value="${param.checkOut}">
                <input type="hidden" name="adults" value="${param.adults}">
                <input type="hidden" name="children" value="${param.children}">
                <div><label>Họ tên *</label><input name="newFullName" maxlength="150" required></div>
                <div><label>SĐT</label><input name="newPhone" value="${param.q}" maxlength="30"></div>
                <div><label>Loại giấy tờ</label>
                  <select name="newDocType"><option value="">-- không --</option>
                    <option value="CCCD">CCCD</option><option value="PASSPORT">Hộ chiếu</option></select></div>
                <div><label>Số giấy tờ</label><input name="newDocNumber" maxlength="50"></div>
                <button class="btn btn-success" type="submit">Tạo & chọn khách này</button>
              </form>
            </details>
          </c:if>
          <p style="color:#c0392b;font-size:13px;margin:8px 0 0">⚠ Chưa chọn khách — đơn sẽ không tạo được cho tới khi chọn khách.</p>
        </c:otherwise>
      </c:choose>
    </div>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/booking">
    <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
    <label>Nhận phòng</label><input type="date" name="checkIn" value="${param.checkIn}" required style="width:100%">
    <label>Trả phòng</label><input type="date" name="checkOut" value="${param.checkOut}" required style="width:100%">
    <label>Số phòng</label><input type="number" name="quantity" min="1" value="1" style="width:100%">
    <label>Người lớn</label><input type="number" name="adults" min="1" value="${empty param.adults ? 1 : param.adults}" style="width:100%">
    <label>Trẻ em</label><input type="number" name="children" min="0" value="${empty param.children ? 0 : param.children}" style="width:100%">
    <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
      <input type="hidden" name="customerId" value="${selectedCustomer.customerId}">
    </c:if>
    <label>Tên khách ở chính</label>
    <input name="primaryGuestName" value="${selectedCustomer.fullName}" maxlength="150" style="width:100%">
    <label>Yêu cầu đặc biệt</label><textarea name="specialRequests" style="width:100%" rows="3"></textarea>
    <p>Tạm tính = giá theo ngày × số đêm × số phòng + 10% thuế. Cọc 30% để xác nhận đơn.</p>
    <button class="btn btn-success" type="submit">Xác nhận đặt phòng</button>
  </form>
</div>
<%@ include file="_footer.jspf" %>
