<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>🚶 Walk-in — Khách đặt trực tiếp tại quầy</h2>
  <p style="color:#636e72">Quy trình: tra giấy tờ → chọn phòng sẵn sàng → thu tiền → hệ thống tự tạo đơn + check-in + gán phòng.</p>

  <!-- BƯỚC 1: Tra cứu khách cũ theo giấy tờ -->
  <form method="get" action="${pageContext.request.contextPath}/reception/walkin"
        style="display:flex;gap:10px;align-items:end;flex-wrap:wrap;background:#f8f9fa;padding:12px;border-radius:6px">
    <div>
      <label>Loại giấy tờ</label>
      <select name="docType">
        <option value="CCCD" ${param.docType == 'CCCD' || empty param.docType ? 'selected' : ''}>CCCD</option>
        <option value="PASSPORT" ${param.docType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
      </select>
    </div>
    <div><label>Số giấy tờ</label><input name="docNo" value="<c:out value='${param.docNo}'/>" placeholder="Quét / nhập số giấy tờ" required></div>
    <button class="btn" type="submit">🔍 Tra cứu khách</button>
  </form>
  <c:if test="${lookedUp}">
    <c:choose>
      <c:when test="${found != null}">
        <div class="msg" style="margin-top:10px">✔ Khách cũ: <b><c:out value="${found.fullName}"/></b> (${found.customerCode}) — thông tin đã được điền sẵn bên dưới.</div>
      </c:when>
      <c:otherwise>
        <div class="err" style="margin-top:10px">Chưa có hồ sơ với giấy tờ này — nhập thông tin bên dưới để tạo mới.</div>
      </c:otherwise>
    </c:choose>
  </c:if>
</div>

<div class="card">
  <form method="post" action="${pageContext.request.contextPath}/reception/walkin">
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:24px">
      <!-- BƯỚC 2: Thông tin khách -->
      <div>
        <h3>👤 Thông tin khách</h3>
        <label>Loại giấy tờ *</label>
        <select name="idDocumentType" style="width:100%">
          <option value="CCCD" ${param.docType == 'CCCD' || empty param.docType ? 'selected' : ''}>CCCD</option>
          <option value="PASSPORT" ${param.docType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
        </select>
        <label>Số giấy tờ *</label>
        <input name="idDocumentNumber" value="<c:out value='${found != null ? found.idDocumentNumber : param.docNo}'/>" maxlength="50" style="width:100%" required>
        <label>Họ tên *</label>
        <input name="fullName" value="<c:out value='${found.fullName}'/>" maxlength="150" style="width:100%" required>
        <label>SĐT</label>
        <input name="phone" value="<c:out value='${found.phone}'/>" maxlength="30" style="width:100%">
        <label>Email (để gửi xác nhận, bỏ trống được)</label>
        <input name="email" value="<c:out value='${found.email}'/>" maxlength="255" style="width:100%">
        <label>Quốc tịch</label>
        <input name="nationality" value="${found != null ? found.nationality : 'Việt Nam'}" maxlength="80" style="width:100%">
      </div>

      <!-- BƯỚC 3: Phòng & thanh toán -->
      <div>
        <h3>🛏 Phòng & kỳ ở (nhận phòng HÔM NAY)</h3>
        <label>Phòng sẵn sàng ở ngay (sạch + trống) *</label>
        <select name="roomId" style="width:100%" required>
          <option value="">-- chọn phòng --</option>
          <c:forEach var="rm" items="${readyRooms}">
            <option value="${rm.roomId}">${rm.typeName} — Phòng ${rm.roomNumber} (tầng ${rm.floorNumber})</option>
          </c:forEach>
        </select>
        <label>Số đêm *</label><input type="number" name="nights" min="1" value="1" style="width:100%" required>
        <label>Người lớn</label><input type="number" name="adults" min="1" value="1" style="width:100%">
        <label>Trẻ em</label><input type="number" name="children" min="0" value="0" style="width:100%">

        <h3 style="margin-top:16px">💰 Thu tiền tại quầy</h3>
        <label>Phương thức</label>
        <select name="method" style="width:100%">
          <option value="CASH">Tiền mặt</option>
          <option value="CARD">Thẻ</option>
        </select>
        <label>Số tiền thu (tối thiểu = cọc 30%, khuyến nghị thu 100%)</label>
        <input type="number" step="0.01" name="amount" style="width:100%" required
               placeholder="Xem bảng giá bên dưới để tính: giá/đêm × số đêm × 1.1 (thuế)">
        <label>Ghi chú</label><textarea name="notes" rows="2" style="width:100%"></textarea>
      </div>
    </div>
    <p style="margin-top:16px">
      <button class="btn btn-success" type="submit" style="font-size:16px;padding:10px 24px">
        ✔ Hoàn tất: tạo đơn + thu tiền + check-in + gán phòng
      </button>
    </p>
  </form>
</div>

<div class="card">
  <h3>Bảng giá tham khảo (giá cơ bản/đêm, chưa gồm 10% thuế)</h3>
  <table>
    <tr><th>Loại phòng</th><th>Giá cơ bản/đêm</th><th>Sức chứa</th></tr>
    <c:forEach var="t" items="${roomTypes}">
      <tr><td>${t.typeName}</td><td><fmt:formatNumber value="${t.basePrice}"/> đ</td>
          <td>${t.maxAdults} NL + ${t.maxChildren} TE</td></tr>
    </c:forEach>
  </table>
  <p style="color:#636e72;font-size:13px">* Giá thực tế lấy theo bảng giá ngày (room_rates) nếu có cấu hình; hệ thống sẽ tự tính đúng khi tạo đơn.</p>
</div>
<%@ include file="_footer.jspf" %>
