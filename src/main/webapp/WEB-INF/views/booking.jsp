<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🛎 Đặt phòng: ${roomType.typeName}</h1>
    <p>Điền thông tin đặt phòng để hệ thống tính giá và giữ phòng cho bạn</p>
  </div>
  <div>
    <a class="btn btn-muted" href="${pageContext.request.contextPath}/rooms">← Chọn phòng khác</a>
  </div>
</div>

<c:set var="bookingCtx" value="roomTypeId=${roomType.roomTypeId}&checkIn=${param.checkIn}&checkOut=${param.checkOut}&adults=${param.adults}&children=${param.children}"/>

<div class="grid">
  <!-- Left Side: Form & Customer Selection (col-8) -->
  <div class="col-8">
    <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
      <!-- Receptionist Mode: Select Customer Box -->
      <div class="card" style="border: 2px dashed var(--bk-blue); background: var(--bk-blue-light); margin-bottom: 20px;">
        <h3 style="margin-top:0; color:var(--bk-navy); font-size:16px; font-weight:700; display:flex; align-items:center; gap:8px;">
          👤 Hồ sơ Khách hàng đặt phòng
        </h3>
        
        <c:choose>
          <%-- CASE A: Selected Customer Profile --%>
          <c:when test="${selectedCustomer != null}">
            <div style="background:#ffffff; border:1px solid var(--bk-border); border-radius:8px; padding:16px; display:flex; justify-content:space-between; align-items:center;">
              <div>
                <div style="font-weight:700; color:var(--bk-navy); font-size:15px;"><c:out value="${selectedCustomer.fullName}"/> (${selectedCustomer.customerCode})</div>
                <div style="font-size:13px; color:var(--bk-muted); margin-top:4px; display:flex; gap:12px;">
                  <c:if test="${not empty selectedCustomer.phone}"><span>📞 <c:out value="${selectedCustomer.phone}"/></span></c:if>
                  <c:if test="${not empty selectedCustomer.idDocumentNumber}"><span>🆔 ${selectedCustomer.idDocumentType}: <c:out value="${selectedCustomer.idDocumentNumber}"/></span></c:if>
                </div>
              </div>
              <a class="btn btn-small btn-muted" href="${pageContext.request.contextPath}/booking?${bookingCtx}">Đổi khách khác</a>
            </div>
          </c:when>

          <%-- CASE B: Find / Search Customer --%>
          <c:otherwise>
            <p style="font-size:13px; color:var(--bk-navy); margin-top:0; margin-bottom:12px;">
              Tìm kiếm khách hàng cũ trong hệ thống bằng tên, SĐT hoặc giấy tờ tùy thân.
            </p>
            <form method="get" action="${pageContext.request.contextPath}/booking" style="display:flex; gap:8px;">
              <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
              <input type="hidden" name="checkIn" value="${param.checkIn}">
              <input type="hidden" name="checkOut" value="${param.checkOut}">
              <input type="hidden" name="adults" value="${param.adults}">
              <input type="hidden" name="children" value="${param.children}">
              <input name="q" value="<c:out value='${param.q}'/>" placeholder="Nhập SĐT, số CCCD, Hộ chiếu, Tên hoặc Mã KH..." style="flex:1;" required>
              <button class="btn" type="submit">🔍 Tìm kiếm</button>
            </form>

            <c:if test="${customerResults != null}">
              <c:choose>
                <c:when test="${not empty customerResults}">
                  <div class="table-wrap" style="margin-top:14px; background:#ffffff; border-radius:6px; border: 1px solid var(--bk-border);">
                    <table>
                      <thead>
                        <tr>
                          <th>Mã KH</th>
                          <th>Họ tên</th>
                          <th>Liên hệ</th>
                          <th>Giấy tờ</th>
                          <th style="text-align: right;">Thao tác</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach var="c" items="${customerResults}">
                          <tr>
                            <td style="font-family:monospace; font-size:13px;">${c.customerCode}</td>
                            <td><strong><c:out value="${c.fullName}"/></strong></td>
                            <td><c:out value="${c.phone}"/></td>
                            <td>${c.idDocumentType} <c:out value="${c.idDocumentNumber}"/></td>
                            <td style="text-align: right;">
                              <a class="btn btn-success btn-small" style="padding:4px 10px;" href="${pageContext.request.contextPath}/booking?${bookingCtx}&customerId=${c.customerId}">Chọn khách</a>
                            </td>
                          </tr>
                        </c:forEach>
                      </tbody>
                    </table>
                  </div>
                </c:when>
                <c:otherwise>
                  <div style="background:#fee2e2; color:#991b1b; padding:10px; border-radius:6px; margin-top:10px; font-size:13px;">
                    ⚠ Không tìm thấy khách hàng nào khớp với từ khóa "<b><c:out value="${param.q}"/></b>".
                  </div>
                </c:otherwise>
              </c:choose>

              <%-- Quick Create Form --%>
              <details style="margin-top:12px; background:#ffffff; border:1px solid var(--bk-border); padding:12px; border-radius:6px;" ${empty customerResults ? 'open' : ''}>
                <summary style="cursor:pointer; font-weight:600; color:var(--bk-blue);">➕ Tạo nhanh hồ sơ khách hàng mới</summary>
                <form method="post" action="${pageContext.request.contextPath}/booking" style="margin-top:12px;">
                  <input type="hidden" name="action" value="createCustomer">
                  <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
                  <input type="hidden" name="checkIn" value="${param.checkIn}">
                  <input type="hidden" name="checkOut" value="${param.checkOut}">
                  <input type="hidden" name="adults" value="${param.adults}">
                  <input type="hidden" name="children" value="${param.children}">
                  
                  <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px;">
                    <div>
                      <label>Họ tên khách *</label>
                      <input name="newFullName" placeholder="Họ & tên khách hàng" maxlength="150" required style="width:100%">
                    </div>
                    <div>
                      <label>Số điện thoại</label>
                      <input name="newPhone" value="<c:out value='${param.q}'/>" placeholder="Số điện thoại liên lạc" maxlength="30" style="width:100%">
                    </div>
                    <div>
                      <label>Loại giấy tờ tùy thân</label>
                      <select name="newDocType" style="width:100%">
                        <option value="">-- Chọn loại giấy tờ --</option>
                        <option value="CCCD">CCCD</option>
                        <option value="PASSPORT">Hộ chiếu</option>
                      </select>
                    </div>
                    <div>
                      <label>Số giấy tờ</label>
                      <input name="newDocNumber" placeholder="Số CCCD hoặc số Hộ chiếu" maxlength="50" style="width:100%">
                    </div>
                  </div>
                  <button class="btn btn-success btn-small" type="submit" style="margin-top:12px; width:100%;">Tạo mới & Gán khách hàng</button>
                </form>
              </details>
            </c:if>
            <p style="color:#b91c1c; font-size:13px; font-weight:600; margin-top:10px; margin-bottom:0;">⚠️ Chưa chọn khách hàng. Vui lòng tìm kiếm hoặc tạo khách mới để tiếp tục.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </c:if>

    <!-- Main Booking Form Card -->
    <div class="card">
      <h2 style="border-bottom: 1px solid var(--bk-border); padding-bottom: 12px; margin-bottom: 20px; font-size: 18px;">
        ✍️ Thông tin đặt phòng chi tiết
      </h2>
      <form method="post" action="${pageContext.request.contextPath}/booking">
        <input type="hidden" name="roomTypeId" value="${roomType.roomTypeId}">
        
        <div style="display:grid; grid-template-columns: 1fr 1fr; gap:16px;">
          <div>
            <label>📅 Ngày nhận phòng</label>
            <input type="date" name="checkIn" value="${param.checkIn}" required style="width:100%">
          </div>
          <div>
            <label>📅 Ngày trả phòng</label>
            <input type="date" name="checkOut" value="${param.checkOut}" required style="width:100%">
          </div>
          <div>
            <label>🔑 Số lượng phòng đặt</label>
            <input type="number" name="quantity" min="1" value="1" style="width:100%" required>
          </div>
          <div style="display:grid; grid-template-columns: 1fr 1fr; gap:8px;">
            <div>
              <label>👥 Người lớn</label>
              <input type="number" name="adults" min="1" value="${empty param.adults ? 1 : param.adults}" style="width:100%">
            </div>
            <div>
              <label>🧒 Trẻ em</label>
              <input type="number" name="children" min="0" value="${empty param.children ? 0 : param.children}" style="width:100%">
            </div>
          </div>
        </div>

        <c:if test="${sessionScope.currentUser.roleCode != 'CUSTOMER'}">
          <input type="hidden" name="customerId" value="${selectedCustomer.customerId}">
        </c:if>

        <label style="margin-top:16px;">👤 Tên khách lưu trú chính</label>
        <input name="primaryGuestName" value="<c:out value='${selectedCustomer != null ? selectedCustomer.fullName : (sessionScope.currentUser.roleCode == "CUSTOMER" ? sessionScope.currentUser.fullName : "")}'/>" placeholder="Họ tên người đại diện nhận phòng" maxlength="150" style="width:100%">

        <label style="margin-top:16px;">💬 Yêu cầu đặc biệt (Không bắt buộc)</label>
        <textarea name="specialRequests" placeholder="Ví dụ: phòng tầng cao, giường phụ, check-in muộn..." style="width:100%" rows="3"></textarea>

        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--bk-border); display:flex; justify-content: flex-end;">
          <button class="btn btn-success" type="submit" style="font-size:16px; padding:10px 28px;" ${sessionScope.currentUser.roleCode != 'CUSTOMER' && selectedCustomer == null ? 'disabled' : ''}>
            ✔ Xác nhận đặt phòng
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- Right Side: Room Type Summary & Pricing Card (col-4) -->
  <div class="col-4">
    <div class="card" style="border: 2px solid var(--bk-gold-border); background: var(--bk-gold-light);">
      <h3 style="margin-top:0; color:#92400e; font-size:16px; font-weight:700; border-bottom:1px solid rgba(254,187,2,0.3); padding-bottom:8px; margin-bottom:14px;">
        🏨 Tóm tắt Loại phòng đặt
      </h3>
      <div style="font-weight:700; color:var(--bk-navy); font-size:18px;">
        ${roomType.typeName}
      </div>
      <div style="font-size:13px; color:var(--bk-muted); margin-top:4px; margin-bottom:16px;">
        Mã: ${roomType.typeCode}
      </div>

      <div style="border-bottom:1px dashed rgba(254,187,2,0.3); padding-bottom:12px; margin-bottom:12px; font-size:13px; color:#78350f; display:flex; justify-content:space-between;">
        <span>Giá cơ bản/phòng/đêm:</span>
        <strong><fmt:formatNumber value="${roomType.basePrice}"/> đ</strong>
      </div>
      
      <div style="border-bottom:1px dashed rgba(254,187,2,0.3); padding-bottom:12px; margin-bottom:12px; font-size:13px; color:#78350f; display:flex; justify-content:space-between;">
        <span>Sức chứa/phòng:</span>
        <strong>${roomType.maxAdults} NL + ${roomType.maxChildren} TE</strong>
      </div>

      <div style="border-bottom:1px dashed rgba(254,187,2,0.3); padding-bottom:12px; margin-bottom:12px; font-size:13px; color:#78350f; display:flex; justify-content:space-between;">
        <span>Tiện nghi:</span>
        <strong style="text-align: right;">${roomType.amenitiesJson}</strong>
      </div>

      <div style="font-size:12px; color:#92400e; line-height:1.5; margin-top:16px;">
        💡 <strong>Thông tin hóa đơn đặt chỗ:</strong><br>
        Tổng tiền tạm tính = Đơn giá ngày × Số đêm lưu trú × Số lượng phòng đặt + 10% thuế VAT. Quý khách vui lòng thanh toán hoặc đặt cọc tối thiểu <strong>30%</strong> giá trị đơn hàng để được xác nhận giữ chỗ thành công.
      </div>
    </div>
  </div>
</div>

<%@ include file="_footer.jspf" %>
