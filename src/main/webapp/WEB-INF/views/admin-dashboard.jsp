<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header Banner -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📊 Dashboard Quản trị Hệ thống (Admin Dashboard)</h1>
    <p>Tổng quan chỉ số hoạt động, quản lý tài khoản nhân viên và theo dõi hạ tầng email hệ thống</p>
  </div>
  <div>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-gold">
      ✨ + Tạo tài khoản mới
    </a>
  </div>
</div>

<!-- KPI Summary Stat Cards -->
<div class="bk-kpi-grid">
  <div class="bk-kpi-card">
    <div class="bk-kpi-label">👥 Tổng người dùng</div>
    <div class="bk-kpi-value">${totalUsers}</div>
    <small class="muted">Bao gồm ${staffCount} nhân viên & ${customerCount} khách hàng</small>
  </div>

  <div class="bk-kpi-card kpi-gold">
    <div class="bk-kpi-label">🛡️ Tài khoản Nhân viên</div>
    <div class="bk-kpi-value">${staffCount}</div>
    <small class="muted">Lễ tân, Quản lý, Phục vụ</small>
  </div>

  <div class="bk-kpi-card kpi-green">
    <div class="bk-kpi-label">📧 Mẫu Email Hoạt động</div>
    <div class="bk-kpi-value">${activeTemplates} / ${totalTemplates}</div>
    <small class="muted">${totalTemplates - activeTemplates} mẫu đang tạm tắt</small>
  </div>

  <div class="bk-kpi-card ${failedLogs > 0 ? 'kpi-red' : 'kpi-green'}">
    <div class="bk-kpi-label">📜 Email Đã gửi / Thất bại</div>
    <div class="bk-kpi-value" style="font-size: 22px;">
      <span style="color:#059669;">${sentLogs} Sent</span> / 
      <span style="color:#dc2626;">${failedLogs} Fail</span>
    </div>
    <small class="muted">Nhật ký xử lý hạ tầng SMTP</small>
  </div>
</div>

<!-- Quick Navigation Action Cards -->
<h2 style="font-size: 18px; font-weight: 700; color: var(--bk-navy); margin-bottom: 16px;">🚀 Phím tắt quản trị nhanh</h2>
<div class="grid" style="margin-bottom: 28px;">
  <div class="card col-4" style="margin-bottom:0; transition: transform 0.2s;" onmouseover="this.style.transform='translateY(-3px)'" onmouseout="this.style.transform='none'">
    <h2>👤 Quản lý Tài khoản</h2>
    <p class="muted" style="font-size: 13px; margin-bottom: 16px;">Tạo mới, phân quyền vai trò nhân viên, khóa/mở tài khoản và đặt lại mật khẩu</p>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn" style="width: 100%;">Truy cập Quản lý Tài khoản →</a>
  </div>

  <div class="card col-4" style="margin-bottom:0; transition: transform 0.2s;" onmouseover="this.style.transform='translateY(-3px)'" onmouseout="this.style.transform='none'">
    <h2>📧 Mẫu Email Hệ thống</h2>
    <p class="muted" style="font-size: 13px; margin-bottom: 16px;">Cấu hình giao diện HTML email kích hoạt, xác nhận đơn hàng và khôi phục mật khẩu</p>
    <a href="${pageContext.request.contextPath}/admin/templates" class="btn btn-secondary" style="width: 100%;">Truy cập Mẫu Email →</a>
  </div>

  <div class="card col-4" style="margin-bottom:0; transition: transform 0.2s;" onmouseover="this.style.transform='translateY(-3px)'" onmouseout="this.style.transform='none'">
    <h2>📜 Nhật ký Gửi Email</h2>
    <p class="muted" style="font-size: 13px; margin-bottom: 16px;">Xem chi tiết thông tin log gửi thư điện tử, kiểm tra nguyên nhân lỗi và retry gửi lại</p>
    <a href="${pageContext.request.contextPath}/admin/email-logs" class="btn btn-muted" style="width: 100%;">Truy cập Nhật ký Email →</a>
  </div>
</div>

<!-- Overview Recent Tables -->
<div class="grid">
  <!-- Recent Users -->
  <div class="card col-6 table-wrap">
    <h2>👥 Tài khoản mới nhất</h2>
    <table>
      <thead>
        <tr>
          <th>Họ tên</th>
          <th>Email</th>
          <th>Vai trò</th>
          <th>Trạng thái</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="u" items="${recentUsers}">
          <tr>
            <td><strong style="color:var(--bk-navy);"><c:out value="${u.fullName}"/></strong></td>
            <td><span style="font-family:monospace; font-size:12px;"><c:out value="${u.email}"/></span></td>
            <td><span class="badge badge-role ${u.roleCode}">${u.roleCode}</span></td>
            <td><span class="badge status-${u.statusCode}">${u.statusCode}</span></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div style="margin-top: 14px; text-align: right;">
      <a href="${pageContext.request.contextPath}/admin/users" style="color: var(--bk-blue); font-weight: 600; text-decoration: none; font-size: 13px;">Xem tất cả tài khoản →</a>
    </div>
  </div>

  <!-- Recent Email Logs -->
  <div class="card col-6 table-wrap">
    <h2>📜 Nhật ký gửi mail gần đây</h2>
    <table>
      <thead>
        <tr>
          <th>Người nhận</th>
          <th>Tiêu đề</th>
          <th>Trạng thái</th>
          <th>Retries</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="l" items="${recentLogs}">
          <tr>
            <td><strong style="color:var(--bk-navy);"><c:out value="${l.recipientEmail}"/></strong></td>
            <td><div style="max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;"><c:out value="${l.subjectSnapshot}"/></div></td>
            <td><span class="badge ${l.statusCode == 'SENT' ? 'status-SENT' : (l.statusCode == 'FAILED' ? 'status-FAILED' : 'status-QUEUED')}">${l.statusCode}</span></td>
            <td>${l.retryCount}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div style="margin-top: 14px; text-align: right;">
      <a href="${pageContext.request.contextPath}/admin/email-logs" style="color: var(--bk-blue); font-weight: 600; text-decoration: none; font-size: 13px;">Xem tất cả nhật ký email →</a>
    </div>
  </div>
</div>

<%@ include file="_footer.jspf" %>
