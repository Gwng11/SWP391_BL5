<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📥 Làm thủ tục Check-in Nhận phòng</h1>
    <p>Xác nhận thông tin khách lưu trú, đối chiếu hồ sơ giấy tờ và kiểm tra số phòng trống sạch trước khi bàn giao khóa phòng</p>
  </div>
</div>

<!-- Search Form Box -->
<div class="bk-search-box">
  <form method="get" action="${pageContext.request.contextPath}/reception/checkin" class="bk-search-form">
    <div style="flex: 1; min-width: 250px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🔍 Từ khóa tìm kiếm</label>
      <input name="q" placeholder="Nhập mã đơn, tên khách, số điện thoại..." value="<c:out value='${param.q}'/>" style="width:100%">
    </div>
    <div style="margin-top: auto;">
      <button class="btn" type="submit" style="height: 38px;">🔍 Tìm đơn hàng</button>
    </div>
  </form>
  
  <c:if test="${not empty param.q}">
    <div class="table-wrap" style="margin-top:14px; background:#ffffff; border-radius:6px; border:1px solid var(--bk-border);">
      <table>
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách đặt</th>
            <th>Ngày nhận</th>
            <th>Ngày trả</th>
            <th style="text-align: right;">Tổng thanh toán</th>
            <th style="text-align: right;">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="rv" items="${reservations}">
            <tr>
              <td style="font-family:monospace; font-weight:600; color:var(--bk-navy);">${rv.bookingCode}</td>
              <td><strong><c:out value="${rv.customerName}"/></strong></td>
              <td>${rv.checkInDate}</td>
              <td>${rv.checkOutDate}</td>
              <td style="text-align: right; font-weight:700; color:var(--bk-blue);"><fmt:formatNumber value="${rv.totalAmount}"/> đ</td>
              <td style="text-align: right;">
                <a class="btn btn-success btn-small" href="?id=${rv.reservationId}">Chọn đơn này</a>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </c:if>
</div>

<!-- Check-in Confirmation Block -->
<c:if test="${r != null}">
  <div class="grid">
    <!-- Left Column: Guest list document check (col-7) -->
    <div class="card col-7">
      <div style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
        <h2 style="margin:0; font-size:18px; color:var(--bk-navy);">📋 Danh sách Khách lưu trú (Đối chiếu giấy tờ)</h2>
        <p style="margin:6px 0 0 0; font-size:13px; color:var(--bk-muted);">Cọc yêu cầu: <fmt:formatNumber value="${r.depositRequired}"/> đ — Đã nộp: <b><fmt:formatNumber value="${depositPaid}"/> đ</b></p>
      </div>

      <c:if test="${lateDays != null && lateDays > 0}">
        <div class="err" style="margin-bottom:16px;">
          ⚠️ Khách đến muộn <strong>${lateDays} ngày</strong> so với ngày nhận phòng thiết lập ban đầu (${r.checkInDate}).
        </div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/reception/checkin">
        <input type="hidden" name="id" value="${r.reservationId}">
        <input type="hidden" name="action" value="saveGuests">
        
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Họ tên khách hàng</th>
                <th>Loại giấy tờ</th>
                <th>Số giấy tờ</th>
                <th style="text-align:center;">Khách chính</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="g" items="${guests}" varStatus="st">
                <tr>
                  <td><input name="gName" value="<c:out value='${g.fullName}'/>" maxlength="150" style="width:100%" required></td>
                  <td>
                    <select name="gDocType" style="width:100%">
                      <option value="" ${empty g.idDocumentType ? 'selected' : ''}>--</option>
                      <option value="CCCD" ${g.idDocumentType == 'CCCD' ? 'selected' : ''}>CCCD</option>
                      <option value="PASSPORT" ${g.idDocumentType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
                    </select>
                  </td>
                  <td><input name="gDocNo" value="<c:out value='${g.idDocumentNumber}'/>" maxlength="50" style="width:100%"></td>
                  <td style="text-align:center;"><input type="radio" name="primaryIdx" value="${st.index}" ${g.primaryGuest ? 'checked' : ''}></td>
                </tr>
              </c:forEach>
              
              <%-- Form lines to add new guests --%>
              <tr>
                <td><input name="gName" placeholder="Thêm khách 1..." maxlength="150" style="width:100%"></td>
                <td>
                  <select name="gDocType" style="width:100%">
                    <option value="">--</option>
                    <option value="CCCD">CCCD</option>
                    <option value="PASSPORT">Hộ chiếu</option>
                  </select>
                </td>
                <td><input name="gDocNo" placeholder="Số giấy tờ..." maxlength="50" style="width:100%"></td>
                <td style="text-align:center;"><input type="radio" name="primaryIdx" value="${guests.size()}" ${empty guests ? 'checked' : ''}></td>
              </tr>
              <tr>
                <td><input name="gName" placeholder="Thêm khách 2..." maxlength="150" style="width:100%"></td>
                <td>
                  <select name="gDocType" style="width:100%">
                    <option value="">--</option>
                    <option value="CCCD">CCCD</option>
                    <option value="PASSPORT">Hộ chiếu</option>
                  </select>
                </td>
                <td><input name="gDocNo" placeholder="Số giấy tờ..." maxlength="50" style="width:100%"></td>
                <td style="text-align:center;"><input type="radio" name="primaryIdx" value="${guests.size() + 1}"></td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <button class="btn btn-muted btn-small" type="submit" style="margin-top:10px;">💾 Lưu danh sách khách</button>
      </form>
    </div>

    <!-- Right Column: Room availability comparison (col-5) -->
    <div class="col-5">
      <!-- Room status match card -->
      <div class="card" style="border: 2px solid var(--bk-gold-border); background: var(--bk-gold-light);">
        <h2 style="font-size:18px; margin-top:0; color:#92400e; border-bottom:1px solid rgba(254,187,2,0.3); padding-bottom:10px; margin-bottom:14px;">
          🛏 Trạng thái phòng trống
        </h2>
        <div class="table-wrap">
          <table style="background:transparent;">
            <thead>
              <tr style="border-bottom:1px solid rgba(254,187,2,0.3);">
                <th>Loại phòng</th>
                <th>Cần</th>
                <th>Sẵn có</th>
                <th>Tình trạng</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="l" items="${lines}">
                <tr>
                  <td><strong>${l.typeName}</strong></td>
                  <td><strong>${l.quantity}</strong></td>
                  <td><span style="font-weight:600; color:var(--bk-navy);">${readyMap[l.reservationRoomId]}</span></td>
                  <td>
                    <c:choose>
                      <c:when test="${readyMap[l.reservationRoomId] < l.quantity}">
                        <span style="color:#b91c1c; font-weight:700;">⚠ KHÔNG ĐỦ</span>
                      </c:when>
                      <c:otherwise><span style="color:#047857; font-weight:700;">✔ Đủ</span></c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/reception/checkin" style="margin-top:20px;">
          <input type="hidden" name="id" value="${r.reservationId}">
          <button class="btn btn-success" type="submit" style="width:100%; font-size:15px; font-weight:700; padding:12px;">
            ✔ Tiến hành Check-in & Xếp phòng
          </button>
        </form>
        <p style="color:#78350f; font-size:12px; margin-top:10px; line-height:1.4;">
          * Hệ thống sẽ kiểm tra kỹ tồn kho phòng thực tế. Nếu không đủ phòng trống dọn sạch, giao dịch check-in sẽ bị dừng.
        </p>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="_footer.jspf" %>
