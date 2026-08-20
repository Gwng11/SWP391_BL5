<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="page-head">
  <h1>Nhật ký Gửi Email (Email Delivery Logs)</h1>
  <div>
    <form method="get" class="inline">
      <input name="q" placeholder="Tìm theo email / tiêu đề" value="${param.q}" style="width:250px">
      <select name="status">
        <option value="">-- Tất cả trạng thái --</option>
        <c:forEach var="s" items="${statuses}">
          <option value="${s}" ${param.status == s ? 'selected' : ''}>${s}</option>
        </c:forEach>
      </select>
      <button class="btn btn-muted" type="submit">Lọc</button>
      <a href="${pageContext.request.contextPath}/admin/email-logs" class="btn btn-muted">Reset</a>
    </form>
  </div>
</div>

<div class="card table-wrap">
  <h2>Lịch sử gửi email hệ thống</h2>
  <table>
    <thead>
      <tr>
        <th>Người nhận</th>
        <th>Tiêu đề (Subject)</th>
        <th>Trạng thái</th>
        <th>Retries</th>
        <th>Thời gian</th>
        <th>Lỗi gần nhất</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="log" items="${logs}">
        <tr>
          <td><c:out value="${log.recipientEmail}"/></td>
          <td><c:out value="${log.subjectSnapshot}"/></td>
          <td>
            <span class="badge ${log.statusCode == 'SENT' ? 'status-READY' : (log.statusCode == 'FAILED' ? 'status-BLOCKED' : '')}">
              ${log.statusCode}
            </span>
          </td>
          <td>${log.retryCount}</td>
          <td>
            <small class="muted">
              Queued: ${log.queuedAt}<br/>
              <c:if test="${not empty log.sentAt}">Sent: ${log.sentAt}<br/></c:if>
              <c:if test="${not empty log.failedAt}">Failed: ${log.failedAt}</c:if>
            </small>
          </td>
          <td style="max-width:250px; word-wrap:break-word;">
            <c:if test="${not empty log.lastError}">
              <span class="danger-text"><small><c:out value="${log.lastError}"/></small></span>
            </c:if>
          </td>
          <td>
            <c:if test="${log.statusCode == 'FAILED'}">
              <form method="post" action="${pageContext.request.contextPath}/admin/email-logs" class="inline">
                <input type="hidden" name="action" value="retry">
                <input type="hidden" name="emailLogId" value="${log.emailLogId}">
                <button class="btn btn-small btn-success" type="submit">Gửi lại</button>
              </form>
            </c:if>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty logs}">
        <tr><td colspan="7" class="empty">Không tìm thấy nhật ký gửi mail nào.</td></tr>
      </c:if>
    </tbody>
  </table>
</div>
<%@ include file="_footer.jspf" %>
