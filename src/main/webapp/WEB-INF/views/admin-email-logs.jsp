<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header Banner -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📜 Nhật ký Gửi Email (Delivery Logs)</h1>
    <p>Theo dõi chi tiết lịch sử gửi thư điện tử, trạng thái phân phối và nhật ký xử lý lỗi của hệ thống</p>
  </div>
</div>

<!-- Booking Search & Filter Box -->
<div class="bk-search-box">
  <form method="get" action="${pageContext.request.contextPath}/admin/email-logs" class="bk-search-form">
    <div class="bk-search-input-wrap">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🔍 Từ khóa tìm kiếm</label>
      <input name="q" placeholder="Tìm theo email người nhận hoặc tiêu đề thư..." value="${param.q}">
    </div>
    
    <div style="min-width: 220px;">
      <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">⚡ Trạng thái gửi thư</label>
      <select name="status">
        <option value="">-- Tất cả trạng thái --</option>
        <c:forEach var="s" items="${statuses}">
          <option value="${s}" ${param.status == s ? 'selected' : ''}>${s}</option>
        </c:forEach>
      </select>
    </div>

    <div style="display:flex; gap:8px; align-self: flex-end; margin-top: auto;">
      <button class="btn" type="submit">🔍 Lọc nhật ký</button>
      <a href="${pageContext.request.contextPath}/admin/email-logs" class="btn btn-muted">🔄 Đặt lại</a>
    </div>
  </form>
</div>

<!-- Email Logs Table -->
<div class="card table-wrap">
  <h2>📋 Lịch sử gửi Email hệ thống (${logs.size()} nhật ký)</h2>
  <table>
    <thead>
      <tr>
        <th>Người nhận</th>
        <th>Tiêu đề thư (Subject)</th>
        <th>Trạng thái</th>
        <th>Số lần thử</th>
        <th>Mốc thời gian</th>
        <th>Lỗi gần nhất</th>
        <th style="text-align: right;">Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="log" items="${logs}">
        <tr>
          <td>
            <div style="font-weight: 600; color: var(--bk-navy);"><c:out value="${log.recipientEmail}"/></div>
          </td>
          <td>
            <div style="font-weight: 500; max-width: 320px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">
              <c:out value="${log.subjectSnapshot}"/>
            </div>
          </td>
          <td>
            <span class="badge ${log.statusCode == 'SENT' ? 'status-SENT' : (log.statusCode == 'FAILED' ? 'status-FAILED' : 'status-QUEUED')}">
              ${log.statusCode}
            </span>
          </td>
          <td>
            <span class="badge" style="background:#f1f5f9; color:#475569;">${log.retryCount} retries</span>
          </td>
          <td>
            <div style="font-size: 12px; color: var(--bk-muted); font-family: monospace;">
              ⏱️ Queued: ${log.queuedAt}<br/>
              <c:if test="${not empty log.sentAt}"><span style="color:#059669;">✅ Sent: ${log.sentAt}</span><br/></c:if>
              <c:if test="${not empty log.failedAt}"><span class="danger-text">❌ Failed: ${log.failedAt}</span></c:if>
            </div>
          </td>
          <td style="max-width:250px; word-wrap:break-word;">
            <c:if test="${not empty log.lastError}">
              <div style="background:#fef2f2; border:1px solid #fca5a5; padding:6px 8px; border-radius:6px; font-family:monospace; font-size:11px; color:#b91c1c; max-height:80px; overflow-y:auto;">
                <c:out value="${log.lastError}"/>
              </div>
            </c:if>
          </td>
          <td style="text-align: right;">
            <c:if test="${log.statusCode == 'FAILED'}">
              <form method="post" action="${pageContext.request.contextPath}/admin/email-logs" class="inline" style="justify-content: flex-end;">
                <input type="hidden" name="action" value="retry">
                <input type="hidden" name="emailLogId" value="${log.emailLogId}">
                <button class="btn btn-small btn-success" type="submit">🔄 Gửi lại</button>
              </form>
            </c:if>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty logs}">
        <tr><td colspan="7" class="empty">🚫 Không tìm thấy nhật ký gửi email nào phù hợp.</td></tr>
      </c:if>
    </tbody>
  </table>
</div>

<%@ include file="_footer.jspf" %>

