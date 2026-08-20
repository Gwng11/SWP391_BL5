<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<style>
  .template-page-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--line);
  }
  .template-page-head h1 {
    margin: 0;
    font-size: 26px;
    color: #1e293b;
    font-weight: 700;
  }
  .template-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
    border: 1px solid #e2e8f0;
  }
  .template-card h2 {
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
    margin-top: 0;
    margin-bottom: 18px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  /* Table Improvements */
  .template-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0;
  }
  .template-table th {
    background: #f8fafc;
    color: #475569;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.03em;
    padding: 12px 14px;
    border-bottom: 2px solid #e2e8f0;
  }
  .template-table td {
    padding: 14px;
    border-bottom: 1px solid #f1f5f9;
    vertical-align: middle;
    font-size: 14px;
  }
  .template-table tr:hover td {
    background-color: #f8fafc;
  }
  .code-pill {
    background: #f1f5f9;
    color: #0f172a;
    font-family: "Consolas", "Fira Code", Monaco, monospace;
    font-size: 13px;
    padding: 3px 8px;
    border-radius: 6px;
    border: 1px solid #cbd5e1;
  }
  .event-pill {
    background: #e0f2fe;
    color: #0369a1;
    font-size: 12px;
    font-weight: 600;
    padding: 3px 8px;
    border-radius: 6px;
  }

  /* Form Improvements */
  .form-group {
    margin-bottom: 16px;
  }
  .form-group label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #334155;
    margin-bottom: 6px;
  }
  .form-control-custom {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    font-size: 14px;
    transition: border-color 0.2s, box-shadow 0.2s;
    background: #fff;
    box-sizing: border-box;
  }
  .form-control-custom:focus {
    outline: none;
    border-color: #0284c7;
    box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.15);
  }
  
  .code-editor {
    font-family: "Consolas", "Fira Code", Monaco, monospace;
    font-size: 13px;
    line-height: 1.6;
    background-color: #0f172a;
    color: #f8fafc;
    border-radius: 8px;
    padding: 14px;
    border: 1px solid #334155;
    width: 100%;
    box-sizing: border-box;
    resize: vertical;
  }
  .code-editor:focus {
    outline: none;
    border-color: #38bdf8;
    box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2);
  }

  .variable-chips {
    background: #f8fafc;
    border: 1px dashed #cbd5e1;
    border-radius: 8px;
    padding: 12px 16px;
    margin-top: 8px;
  }
  .variable-chips p {
    margin: 0 0 8px 0;
    font-size: 13px;
    font-weight: 600;
    color: #475569;
  }
  .var-tag {
    display: inline-block;
    background: #e2e8f0;
    color: #1e293b;
    font-family: monospace;
    font-size: 12px;
    padding: 2px 7px;
    border-radius: 4px;
    margin-right: 6px;
    margin-bottom: 4px;
  }

  .btn-primary-custom {
    background: #0284c7;
    color: #ffffff;
    font-weight: 600;
    padding: 10px 20px;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-size: 14px;
    transition: background 0.2s;
  }
  .btn-primary-custom:hover {
    background: #0369a1;
  }
</style>

<div class="template-page-head">
  <h1>📧 Quản lý Mẫu Email</h1>
  <a href="${pageContext.request.contextPath}/admin/templates" class="btn btn-muted">+ Tạo mẫu mới</a>
</div>

<div class="grid">
  <!-- Left Column: Templates List (col-5) -->
  <div class="template-card col-5 table-wrap">
    <h2>📋 Danh sách mẫu hiện có</h2>
    <table class="template-table">
      <thead>
        <tr>
          <th>Mã mẫu / Tên mẫu</th>
          <th>Sự kiện</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="t" items="${templates}">
          <tr>
            <td>
              <div style="font-weight:600; color:#0f172a; margin-bottom:2px;"><c:out value="${t.templateName}"/></div>
              <span class="code-pill"><c:out value="${t.templateCode}"/></span>
            </td>
            <td>
              <span class="event-pill"><c:out value="${t.eventCode}"/></span>
            </td>
            <td>
              <span class="badge ${t.active ? 'status-READY' : 'status-BLOCKED'}">
                ${t.active ? 'ACTIVE' : 'INACTIVE'}
              </span>
            </td>
            <td style="white-space:nowrap;">
              <div class="inline">
                <a class="btn btn-small" href="?edit=${t.emailTemplateId}">Chi tiết</a>
                <form method="post" action="${pageContext.request.contextPath}/admin/templates" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa mẫu email này?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="emailTemplateId" value="${t.emailTemplateId}">
                  <button class="btn btn-danger btn-small" type="submit">Xóa</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty templates}">
          <tr><td colspan="4" class="empty">Chưa có mẫu email nào trong hệ thống.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- Right Column: Edit Form (col-7) -->
  <div class="template-card col-7">
    <h2>
      <c:choose>
        <c:when test="${not empty editTemplate}">✏️ Chi tiết & Cập nhật mẫu email</c:when>
        <c:otherwise>➕ Tạo mẫu email mới</c:otherwise>
      </c:choose>
    </h2>

    <form method="post" action="${pageContext.request.contextPath}/admin/templates">
      <input type="hidden" name="emailTemplateId" value="${empty editTemplate ? 0 : editTemplate.emailTemplateId}">

      <div class="grid" style="gap: 12px; margin-bottom: 12px;">
        <div class="col-6 form-group">
          <label>Mã mẫu email *</label>
          <input name="templateCode" class="form-control-custom" required maxlength="50" value="${editTemplate.templateCode}" placeholder="Ví dụ: TPL_VERIFY">
        </div>
        <div class="col-6 form-group">
          <label>Tên mẫu *</label>
          <input name="templateName" class="form-control-custom" required maxlength="100" value="${editTemplate.templateName}" placeholder="Ví dụ: Mẫu xác thực email">
        </div>
      </div>

      <div class="grid" style="gap: 12px; margin-bottom: 12px;">
        <div class="col-8 form-group">
          <label>Sự kiện kích hoạt *</label>
          <select name="eventCode" class="form-control-custom" required>
            <c:forEach var="ev" items="${events}">
              <option value="${ev}" ${editTemplate.eventCode == ev ? 'selected' : ''}>${ev}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-4 form-group">
          <label>Trạng thái</label>
          <select name="active" class="form-control-custom">
            <option value="true" ${empty editTemplate || editTemplate.active ? 'selected' : ''}>ACTIVE</option>
            <option value="false" ${not empty editTemplate && !editTemplate.active ? 'selected' : ''}>INACTIVE</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>Tiêu đề email (Subject) *</label>
        <input name="subjectTemplate" class="form-control-custom" required maxlength="255" value="${editTemplate.subjectTemplate}" placeholder="Xác thực tài khoản của bạn tại Sunrise Hotel">
      </div>

      <div class="form-group">
        <label>Nội dung HTML (Body HTML) *</label>
        <textarea name="bodyHtml" class="code-editor" required rows="14" placeholder="<p>Chào {{full_name}},</p>...">${editTemplate.bodyHtml}</textarea>
        
        <div class="variable-chips">
          <p>💡 Các tham số động khả dụng (Cú pháp <code>{{tên_biến}}</code>):</p>
          <span class="var-tag">{{full_name}}</span>
          <span class="var-tag">{{verify_link}}</span>
          <span class="var-tag">{{reset_link}}</span>
          <span class="var-tag">{{booking_code}}</span>
          <span class="var-tag">{{total_amount}}</span>
          <span class="var-tag">{{hotel_name}}</span>
        </div>
      </div>

      <div class="form-actions" style="margin-top:24px; display:flex; gap:12px; align-items:center;">
        <button class="btn-primary-custom" type="submit">💾 Lưu mẫu email</button>
        <c:if test="${not empty editTemplate}">
          <a href="${pageContext.request.contextPath}/admin/templates" class="btn btn-muted">Đóng / Tạo mới</a>
        </c:if>
      </div>
    </form>
    <c:if test="${not empty editTemplate}">
      <form method="post" action="${pageContext.request.contextPath}/admin/templates" style="margin-top:12px;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa mẫu email này?');">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="emailTemplateId" value="${editTemplate.emailTemplateId}">
        <button class="btn btn-danger" type="submit">🗑️ Xóa mẫu email này</button>
      </form>
    </c:if>
  </div>
</div>

<%@ include file="_footer.jspf" %>

