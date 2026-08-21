<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>👤 Quản lý Hồ sơ Khách hàng</h1>
    <p>Quản lý thông tin định danh khách hàng, điện thoại, email và hồ sơ lưu trú</p>
  </div>
</div>

<!-- Search & Filter Box -->
<div class="bk-search-box">
  <form method="get" action="${pageContext.request.contextPath}/reception/customers" class="bk-search-form">
    <div style="flex: 1; min-width: 280px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🔍 Từ khóa tìm kiếm</label>
      <input name="q" placeholder="Nhập tên khách hàng, email, số điện thoại, CCCD/Hộ chiếu..." value="<c:out value='${param.q}'/>" style="width:100%">
    </div>
    <div style="margin-top: auto;">
      <button class="btn" type="submit" style="height: 38px;">🔍 Tìm hồ sơ</button>
    </div>
  </form>
</div>

<div class="grid">
  <!-- Left Side: Customers list table (col-7) -->
  <div class="card col-7">
    <div style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
      <h2 style="margin:0; font-size:18px;">📋 Danh sách Khách hàng</h2>
    </div>

    <c:choose>
      <c:when test="${empty customers}">
        <div style="text-align:center; padding:30px 10px; color:var(--bk-muted); font-size:13px;">
          📭 Không tìm thấy kết quả khách hàng nào phù hợp.
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Mã KH</th>
                <th>Họ tên</th>
                <th>Thông tin liên hệ</th>
                <th>Giấy tờ</th>
                <th style="text-align: right;">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="c" items="${customers}">
                <tr>
                  <td style="font-family:monospace; font-size:13px;">${c.customerCode}</td>
                  <td><strong><c:out value="${c.fullName}"/></strong></td>
                  <td>
                    <span style="font-size:13px; color:var(--bk-text);"><c:out value="${c.phone}"/></span><br>
                    <small class="muted"><c:out value="${c.email}"/></small>
                  </td>
                  <td>
                    <span class="badge" style="background:var(--bk-blue-light); color:var(--bk-blue); font-size:11px;">
                      ${c.idDocumentType} <c:out value="${c.idDocumentNumber}"/>
                    </span>
                  </td>
                  <td style="text-align: right;">
                    <a class="btn btn-muted btn-small" href="?edit=${c.customerId}&q=${param.q}">✏️ Sửa</a>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- Right Side: Edit / Create Customer Form (col-5) -->
  <div class="card col-5" style="border: 2px solid var(--bk-gold-border); background: var(--bk-gold-light); height: fit-content;">
    <div style="border-bottom:1px solid rgba(254,187,2,0.3); padding-bottom:12px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center;">
      <h2 style="margin:0; font-size:18px; color: #92400e;">
        ${editing == null ? '➕ Tạo hồ sơ khách mới' : '✏️ Cập nhật hồ sơ khách'}
      </h2>
      <c:if test="${editing != null}">
        <a class="btn btn-muted btn-small" href="?q=${param.q}">Hủy</a>
      </c:if>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/reception/customers">
      <c:if test="${editing != null}">
        <input type="hidden" name="customerId" value="${editing.customerId}">
      </c:if>
      
      <label>Họ & tên khách hàng *</label>
      <input name="fullName" value="<c:out value='${editing.fullName}'/>" maxlength="150" style="width:100%" required placeholder="Họ và tên khách hàng">

      <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-top:12px;">
        <div>
          <label>Số điện thoại</label>
          <input name="phone" value="<c:out value='${editing.phone}'/>" maxlength="30" style="width:100%" placeholder="SĐT liên hệ">
        </div>
        <div>
          <label>Ngày sinh</label>
          <input type="date" name="dateOfBirth" value="${editing.dateOfBirth}" style="width:100%">
        </div>
      </div>

      <label style="margin-top:12px;">Email liên lạc</label>
      <input name="email" type="email" value="<c:out value='${editing.email}'/>" maxlength="255" style="width:100%" placeholder="Ví dụ: khachhang@gmail.com">

      <div style="display:grid; grid-template-columns: 1fr 2fr; gap:12px; margin-top:12px;">
        <div>
          <label>Loại giấy tờ</label>
          <select name="idDocumentType" style="width:100%">
            <option value="">-- Không --</option>
            <option value="CCCD" ${editing.idDocumentType == 'CCCD' ? 'selected' : ''}>CCCD</option>
            <option value="PASSPORT" ${editing.idDocumentType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
          </select>
        </div>
        <div>
          <label>Số giấy tờ tùy thân</label>
          <input name="idDocumentNumber" value="<c:out value='${editing.idDocumentNumber}'/>" maxlength="50" style="width:100%" placeholder="Số CCCD hoặc số Hộ chiếu">
        </div>
      </div>

      <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-top:12px;">
        <div>
          <label>Quốc tịch</label>
          <input name="nationality" value="<c:out value='${editing.nationality}'/>" maxlength="80" style="width:100%" placeholder="Ví dụ: Việt Nam">
        </div>
        <div>
          <label>Địa chỉ thường trú</label>
          <input name="address" value="<c:out value='${editing.address}'/>" maxlength="255" style="width:100%" placeholder="Số nhà, Tên đường, Thành phố...">
        </div>
      </div>

      <div style="margin-top:20px; border-top: 1px dashed rgba(254,187,2,0.3); padding-top:14px;">
        <button class="btn btn-success" type="submit" style="width:100%; font-size:15px; font-weight:700; padding:10px 14px;">
          ${editing == null ? '➕ Tạo hồ sơ khách hàng' : '💾 Lưu lại thay đổi'}
        </button>
      </div>
    </form>
  </div>
</div>

<%@ include file="_footer.jspf" %>
