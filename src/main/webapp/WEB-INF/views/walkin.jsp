<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🚶 Walk-in — Khách Đặt phòng tại Quầy</h1>
    <p>Quy trình nhanh: Tra cứu giấy tờ khách → Chọn phòng trống → Thu tiền mặt/thẻ → Check-in lập tức</p>
  </div>
</div>

<!-- STEP 1: Customer Lookup Search Box -->
<div class="bk-search-box">
  <form method="get" action="${pageContext.request.contextPath}/reception/walkin" class="bk-search-form">
    <div style="width: 180px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🆔 Loại giấy tờ</label>
      <select name="docType" style="width:100%">
        <option value="CCCD" ${param.docType == 'CCCD' || empty param.docType ? 'selected' : ''}>CCCD</option>
        <option value="PASSPORT" ${param.docType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
      </select>
    </div>
    <div style="flex: 1; min-width: 250px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🔍 Số giấy tờ (CCCD / Hộ chiếu)</label>
      <input name="docNo" value="<c:out value='${param.docNo}'/>" placeholder="Quét hoặc nhập số định danh khách hàng..." required style="width:100%">
    </div>
    <div style="margin-top: auto;">
      <button class="btn" type="submit" style="height: 38px;">🔍 Tra cứu khách hàng</button>
    </div>
  </form>

  <c:if test="${lookedUp}">
    <c:choose>
      <c:when test="${found != null}">
        <div class="msg" style="margin-top:12px; font-weight: 600; border-color: #059669; background: #ecfdf5; color: #047857;">
          ✔ Khách hàng cũ: <b><c:out value="${found.fullName}"/></b> (${found.customerCode}) — Thông tin hồ sơ đã tự động được điền.
        </div>
      </c:when>
      <c:otherwise>
        <div class="err" style="margin-top:12px; font-weight: 600; border-color: #dc2626; background: #fef2f2; color: #b91c1c;">
          ℹ️ Chưa có hồ sơ với giấy tờ này — Bạn hãy điền thông tin bên dưới để tạo hồ sơ khách hàng mới.
        </div>
      </c:otherwise>
    </c:choose>
  </c:if>
</div>

<!-- STEP 2 & 3: Main Checkout Grid Form -->
<form method="post" action="${pageContext.request.contextPath}/reception/walkin">
  <div class="grid">
    <!-- Left column: Customer Info (col-6) -->
    <div class="card col-6">
      <h2 style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px; font-size:18px;">
        👤 Thông tin khách hàng
      </h2>

      <label>Loại giấy tờ tùy thân *</label>
      <select name="idDocumentType" style="width:100%">
        <option value="CCCD" ${param.docType == 'CCCD' || empty param.docType ? 'selected' : ''}>CCCD</option>
        <option value="PASSPORT" ${param.docType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
      </select>

      <label style="margin-top:12px;">Số định danh giấy tờ *</label>
      <input name="idDocumentNumber" value="<c:out value='${found != null ? found.idDocumentNumber : param.docNo}'/>" maxlength="50" style="width:100%" required placeholder="Nhập CCCD hoặc số Hộ chiếu">

      <label style="margin-top:12px;">Họ tên khách hàng *</label>
      <input name="fullName" value="<c:out value='${found.fullName}'/>" maxlength="150" style="width:100%" required placeholder="Họ & tên chữ in hoa có dấu">

      <label style="margin-top:12px;">Số điện thoại</label>
      <input name="phone" value="<c:out value='${found.phone}'/>" maxlength="30" style="width:100%" placeholder="Số điện thoại liên lạc">

      <label style="margin-top:12px;">Email (bỏ trống nếu khách không dùng)</label>
      <input name="email" value="<c:out value='${found.email}'/>" maxlength="255" style="width:100%" placeholder="Ví dụ: khachhang@gmail.com">

      <label style="margin-top:12px;">Quốc tịch</label>
      <input name="nationality" value="${found != null ? found.nationality : 'Việt Nam'}" maxlength="80" style="width:100%" placeholder="Ví dụ: Việt Nam, Anh, Mỹ...">
    </div>

    <!-- Right column: Stay Period, Room and Payments (col-6) -->
    <div class="card col-6" style="border: 2px solid var(--bk-gold-border); background: var(--bk-gold-light);">
      <h2 style="border-bottom: 1px solid rgba(254,187,2,0.3); padding-bottom:12px; margin-bottom:16px; font-size:18px; color: #92400e;">
        🛏 Phòng & Kỳ lưu trú (Check-in hôm nay)
      </h2>

      <label>Phòng sẵn sàng (Trống + Sạch) *</label>
      <select name="roomId" style="width:100%" required>
        <option value="">-- Click để chọn phòng đang sẵn sàng --</option>
        <c:forEach var="rm" items="${readyRooms}">
          <option value="${rm.roomId}">${rm.typeName} — Phòng ${rm.roomNumber} (Tầng ${rm.floorNumber})</option>
        </c:forEach>
      </select>

      <div style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap:12px; margin-top:12px;">
        <div>
          <label>Số đêm lưu trú *</label>
          <input type="number" name="nights" min="1" value="1" style="width:100%" required>
        </div>
        <div>
          <label>Người lớn</label>
          <input type="number" name="adults" min="1" value="1" style="width:100%">
        </div>
        <div>
          <label>Trẻ em</label>
          <input type="number" name="children" min="0" value="0" style="width:100%">
        </div>
      </div>

      <h3 style="margin-top:20px; color:#92400e; font-size:16px; font-weight:700; border-top:1px dashed rgba(254,187,2,0.3); padding-top:14px; margin-bottom:12px;">
        💰 Thanh toán trực tiếp tại quầy
      </h3>
      
      <div style="display:grid; grid-template-columns: 1fr 2fr; gap:12px;">
        <div>
          <label>Phương thức</label>
          <select name="method" style="width:100%">
            <option value="CASH">Tiền mặt</option>
            <option value="CARD">Thẻ / Chuyển khoản</option>
          </select>
        </div>
        <div>
          <label>Số tiền đã thu *</label>
          <input type="number" step="0.01" name="amount" style="width:100%" required placeholder="Thu tối thiểu 30% cọc hoặc 100%">
        </div>
      </div>

      <label style="margin-top:12px;">Ghi chú giao dịch</label>
      <textarea name="notes" rows="2" style="width:100%" placeholder="Ví dụ: Khách thanh toán trước toàn bộ tiền phòng..."></textarea>

      <div style="margin-top:24px;">
        <button class="btn btn-success" type="submit" style="width:100%; font-size:16px; padding:12px; font-weight:700;">
          ✔ Hoàn tất thủ tục và Nhận phòng ngay
        </button>
      </div>
    </div>
  </div>
</form>

<!-- Room Rates Reference Table -->
<div class="card">
  <h2 style="font-size:16px; font-weight:700; margin-top:0; border-bottom:1px solid var(--bk-border); padding-bottom:8px; margin-bottom:12px;">
    📊 Bảng giá tham khảo (Giá chưa gồm 10% thuế VAT)
  </h2>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>Loại phòng</th>
          <th>Sức chứa tối đa</th>
          <th style="text-align: right;">Giá cơ bản/đêm</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="t" items="${roomTypes}">
          <tr>
            <td><strong>${t.typeName}</strong></td>
            <td>${t.maxAdults} NL + ${t.maxChildren} TE</td>
            <td style="text-align: right; font-weight:700; color:var(--bk-blue);">
              <fmt:formatNumber value="${t.basePrice}"/> đ
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
  <p style="color:#636e72; font-size:12px; margin-top:10px;">* Đơn giá thực tế có thể thay đổi linh hoạt theo chính sách giá ngày nếu có thiết lập.</p>
</div>

<%@ include file="_footer.jspf" %>
