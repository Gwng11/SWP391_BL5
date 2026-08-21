<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header Banner -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📧 Quản lý Mẫu Email Hệ thống</h1>
    <p>Thiết lập giao diện HTML, nội dung gửi thông báo và các biểu mẫu xác nhận tự động qua thư điện tử</p>
  </div>
  <div>
    <a href="${pageContext.request.contextPath}/admin/templates" class="btn btn-gold">
      ✨ + Tạo mẫu email mới
    </a>
  </div>
</div>

<div class="grid">
  <!-- Left Column: Templates List (col-5) -->
  <div class="card col-5 table-wrap">
    <h2>📋 Mẫu Email hiện có (${templates.size()})</h2>
    <table>
      <thead>
        <tr>
          <th>Mã & Tên mẫu</th>
          <th>Sự kiện kích hoạt</th>
          <th>Trạng thái</th>
          <th style="text-align: right;">Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="t" items="${templates}">
          <tr>
            <td>
              <div style="font-weight:700; color:var(--bk-navy); margin-bottom:4px;"><c:out value="${t.templateName}"/></div>
              <span style="font-family: monospace; background:#f1f5f9; color:#334155; font-size:12px; padding:2px 6px; border-radius:4px; border:1px solid #cbd5e1;"><c:out value="${t.templateCode}"/></span>
            </td>
            <td>
              <span class="badge" style="background:#e0f2fe; color:#0369a1; border:1px solid #bae6fd; font-size:11px;"><c:out value="${t.eventCode}"/></span>
            </td>
            <td>
              <span class="badge ${t.active ? 'status-ACTIVE' : 'status-INACTIVE'}">
                ${t.active ? 'ACTIVE' : 'INACTIVE'}
              </span>
            </td>
            <td style="white-space:nowrap; text-align: right;">
              <div class="inline" style="justify-content: flex-end;">
                <a class="btn btn-small btn-muted" href="?edit=${t.emailTemplateId}">✏️ Chi tiết</a>
                <form method="post" action="${pageContext.request.contextPath}/admin/templates" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa mẫu email này?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="emailTemplateId" value="${t.emailTemplateId}">
                  <button class="btn btn-danger btn-small" type="submit">🗑️ Xóa</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty templates}">
          <tr><td colspan="4" class="empty">🚫 Chưa có mẫu email nào trong hệ thống.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- Right Column: Edit / Create Form (col-7) -->
  <div class="card col-7">
    <h2>
      <c:choose>
        <c:when test="${not empty editTemplate}">✏️ Cập nhật Mẫu Email [<c:out value="${editTemplate.templateCode}"/>]</c:when>
        <c:otherwise>✨ Tạo Mẫu Email Mới</c:otherwise>
      </c:choose>
    </h2>

    <form method="post" action="${pageContext.request.contextPath}/admin/templates">
      <input type="hidden" name="emailTemplateId" value="${empty editTemplate ? 0 : editTemplate.emailTemplateId}">

      <div class="grid" style="gap: 12px; margin-bottom: 14px;">
        <div class="col-6">
          <label>🏷️ Mã mẫu email (Mã tra cứu) *</label>
          <input name="templateCode" required maxlength="50" value="${editTemplate.templateCode}" placeholder="Ví dụ: TPL_VERIFY_ACC" style="font-family: monospace;">
        </div>
        <div class="col-6">
          <label>📝 Tên hiển thị mẫu email *</label>
          <input name="templateName" required maxlength="100" value="${editTemplate.templateName}" placeholder="Ví dụ: Xác nhận đăng ký tài khoản">
        </div>
      </div>

      <div class="grid" style="gap: 12px; margin-bottom: 14px;">
        <div class="col-8">
          <label>⚡ Sự kiện kích hoạt gửi tự động *</label>
          <select name="eventCode" required>
            <c:forEach var="ev" items="${events}">
              <option value="${ev}" ${editTemplate.eventCode == ev ? 'selected' : ''}>${ev}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-4">
          <label>🔘 Trạng thái sử dụng</label>
          <select name="active">
            <option value="true" ${empty editTemplate || editTemplate.active ? 'selected' : ''}>ACTIVE (Bật)</option>
            <option value="false" ${not empty editTemplate && !editTemplate.active ? 'selected' : ''}>INACTIVE (Tắt)</option>
          </select>
        </div>
      </div>

      <div style="margin-bottom: 14px;">
        <label>📌 Tiêu đề thư (Subject Line) *</label>
        <input name="subjectTemplate" required maxlength="255" value="${editTemplate.subjectTemplate}" placeholder="Ví dụ: Xác nhận đơn đặt phòng {{booking_code}} tại Sunrise Hotel">
      </div>

      <div style="margin-bottom: 16px;">
        <label>💻 Nội dung HTML (Body Template) *</label>
        <textarea name="bodyHtml" required rows="14" placeholder="<p>Chào {{full_name}},</p>..." style="font-family: 'Consolas', 'Fira Code', Monaco, monospace; font-size: 13px; line-height: 1.6; background-color: #0f172a; color: #f8fafc; border-radius: 8px; padding: 14px; border: 1px solid #334155; width: 100%; box-sizing: border-box; resize: vertical;">${editTemplate.bodyHtml}</textarea>
        
        <div style="background: var(--bk-gold-light); border: 1px dashed var(--bk-gold-border); border-radius: 8px; padding: 12px 16px; margin-top: 10px;">
          <p style="margin: 0 0 8px 0; font-size: 13px; font-weight: 700; color: #78350f;">💡 Các biến truyền động (Cú pháp <code>{{tên_biến}}</code>):</p>
          <div style="display:flex; flex-wrap:wrap; gap:6px;">
            <span style="background: #ffffff; color: #1e293b; font-family: monospace; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid var(--bk-gold-border);">{{full_name}}</span>
            <span style="background: #ffffff; color: #1e293b; font-family: monospace; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid var(--bk-gold-border);">{{verify_link}}</span>
            <span style="background: #ffffff; color: #1e293b; font-family: monospace; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid var(--bk-gold-border);">{{reset_link}}</span>
            <span style="background: #ffffff; color: #1e293b; font-family: monospace; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid var(--bk-gold-border);">{{booking_code}}</span>
            <span style="background: #ffffff; color: #1e293b; font-family: monospace; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid var(--bk-gold-border);">{{total_amount}}</span>
            <span style="background: #ffffff; color: #1e293b; font-family: monospace; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid var(--bk-gold-border);">{{hotel_name}}</span>
          </div>
        </div>
      </div>

      <div class="form-actions" style="margin-top:20px; border-top: 1px solid var(--bk-border); padding-top: 16px;">
        <button class="btn" type="submit">💾 Lưu thông tin Mẫu Email</button>
        <c:if test="${not empty editTemplate}">
          <a href="${pageContext.request.contextPath}/admin/templates" class="btn btn-muted">✖️ Đóng / Tạo mới</a>
        </c:if>
      </div>
    </form>
    
    <c:if test="${not empty editTemplate}">
      <form method="post" action="${pageContext.request.contextPath}/admin/templates" style="margin-top:12px;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa mẫu email này?');">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="emailTemplateId" value="${editTemplate.emailTemplateId}">
        <button class="btn btn-danger btn-small" type="submit">🗑️ Xóa vĩnh viễn mẫu email này</button>
      </form>
    </c:if>
  </div>
</div>

<%@ include file="_footer.jspf" %>

