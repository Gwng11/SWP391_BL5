<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<c:set var="showForm" value="${not empty editUser || param.add == 'true' || param.action == 'create' || param.action == 'update'}"/>

<!-- Booking Hero Header Banner -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>👥 Quản lý Tài khoản & Phân quyền</h1>
    <p>Quản lý người dùng, phân quyền vai trò nhân viên và cài đặt trạng thái tài khoản hệ thống</p>
  </div>
  <div>
    <c:if test="${!showForm}">
      <a href="${pageContext.request.contextPath}/admin/users?add=true" class="btn btn-gold">
        ✨ + Tạo tài khoản mới
      </a>
    </c:if>
  </div>
</div>

<c:if test="${!showForm}">
  <!-- Booking Search & Filter Box -->
  <div class="bk-search-box">
    <form method="get" action="${pageContext.request.contextPath}/admin/users" class="bk-search-form">
      <div class="bk-search-input-wrap">
        <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🔍 Từ khóa tìm kiếm</label>
        <input name="q" placeholder="Tìm theo tên, email hoặc số điện thoại..." value="${param.q}">
      </div>
      
      <div style="min-width: 180px;">
        <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">🛡️ Vai trò hệ thống</label>
        <select name="roleCode">
          <option value="">-- Tất cả vai trò --</option>
          <c:forEach var="r" items="${roles}">
            <option value="${r}" ${param.roleCode == r ? 'selected' : ''}>${r}</option>
          </c:forEach>
        </select>
      </div>

      <div style="min-width: 180px;">
        <label style="margin-bottom:4px; font-size:12px; font-weight:600; color:var(--bk-navy);">⚡ Trạng thái</label>
        <select name="statusCode">
          <option value="">-- Tất cả trạng thái --</option>
          <c:forEach var="s" items="${statuses}">
            <option value="${s}" ${param.statusCode == s ? 'selected' : ''}>${s}</option>
          </c:forEach>
        </select>
      </div>

      <div style="display:flex; gap:8px; align-self: flex-end; margin-top: auto;">
        <button class="btn" type="submit">🔍 Lọc dữ liệu</button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-muted">🔄 Đặt lại</a>
      </div>
    </form>
  </div>

  <div class="grid">
    <!-- Full Width Users List (col-12) -->
    <div class="card col-12 table-wrap">
      <h2>📋 Danh sách Tài khoản (${users.size()} kết quả)</h2>
      <table>
        <thead>
          <tr>
            <th>Họ & Tên</th>
            <th>Email</th>
            <th>Vai trò</th>
            <th>Bộ phận</th>
            <th>Trạng thái</th>
            <th style="text-align: right;">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="u" items="${users}">
            <tr>
              <td>
                <div style="display:flex; align-items:center; gap:10px;">
                  <div style="width:32px; height:32px; border-radius:50%; background:var(--bk-blue-light); color:var(--bk-blue); display:flex; align-items:center; justify-content:center; font-weight:700; font-size:13px; border: 1px solid #c7d2fe;">
                    ${u.fullName.substring(0, 1)}
                  </div>
                  <div>
                    <div style="font-weight:600; color:var(--bk-text);"><c:out value="${u.fullName}"/></div>
                    <c:if test="${not empty u.phone}"><small class="muted">📞 <c:out value="${u.phone}"/></small></c:if>
                  </div>
                </div>
              </td>
              <td><span style="font-family: monospace; font-size: 13px;"><c:out value="${u.email}"/></span></td>
              <td><span class="badge badge-role ${u.roleCode}">${u.roleCode}</span></td>
              <td><c:out value="${empty u.departmentCode ? '-' : u.departmentCode}"/></td>
              <td>
                <span class="badge status-${u.statusCode}">${u.statusCode}</span>
                <c:if test="${not empty u.lockedUntil}">
                  <br/><small class="danger-text" style="font-size:11px;">🔒 Đến: ${u.lockedUntil}</small>
                </c:if>
              </td>
              <td style="white-space:nowrap; text-align: right;">
                <div class="inline" style="justify-content: flex-end;">
                  <a class="btn btn-small btn-muted" href="?edit=${u.userId}&q=${param.q}&roleCode=${param.roleCode}&statusCode=${param.statusCode}">✏️ Sửa</a>
                  <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa tài khoản này?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="userId" value="${u.userId}">
                    <button class="btn btn-danger btn-small" type="submit">🗑️ Xóa</button>
                  </form>
                </div>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty users}">
            <tr><td colspan="6" class="empty">🚫 Không tìm thấy tài khoản phù hợp với điều kiện tìm kiếm.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>
</c:if>

<c:if test="${showForm}">
  <div style="max-width: 800px; margin: 0 auto;">
    <div class="card" style="padding: 24px;">
      <c:choose>
        <c:when test="${not empty editUser}">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px; border-bottom: 1px solid var(--bk-border); padding-bottom: 12px;">
            <h2 style="margin:0;">✏️ Cập nhật thông tin Tài khoản</h2>
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-muted">← Quay lại danh sách</a>
          </div>
          <form method="post" action="${pageContext.request.contextPath}/admin/users" class="form-grid">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="userId" value="${editUser.userId}">
            <input type="hidden" name="q" value="${param.q}">
            <input type="hidden" name="roleCodeFilter" value="${param.roleCode}">
            <input type="hidden" name="statusCodeFilter" value="${param.statusCode}">

            <div class="full">
              <label>📧 Email Đăng nhập (Cố định)</label>
              <input value="${editUser.email}" disabled style="background:#f8fafc; color:#64748b;">
            </div>

            <div class="full">
              <label>👤 Họ và tên *</label>
              <input name="fullName" required value="${editUser.fullName}">
            </div>

            <div>
              <label>📞 Số điện thoại</label>
              <input name="phone" value="${editUser.phone}">
            </div>

            <div>
              <label>🪪 Định danh (CCCD/Passport)</label>
              <input name="identificationNumber" value="${editUser.identificationNumber}">
            </div>

            <div class="full">
              <label>🏠 Địa chỉ liên hệ</label>
              <input name="address" value="${editUser.address}">
            </div>

            <div>
              <label>🛡️ Vai trò hệ thống *</label>
              <select name="roleCode" required>
                <c:forEach var="r" items="${roles}">
                  <option value="${r}" ${editUser.roleCode == r ? 'selected' : ''}>${r}</option>
                </c:forEach>
              </select>
            </div>

            <div>
              <label>🏢 Bộ phận làm việc</label>
              <select name="departmentCode">
                <option value="">-- Không bộ phận --</option>
                <c:forEach var="d" items="${departments}">
                  <option value="${d}" ${editUser.departmentCode == d ? 'selected' : ''}>${d}</option>
                </c:forEach>
              </select>
            </div>

            <div>
              <label>⚡ Trạng thái tài khoản *</label>
              <select name="statusCode" required>
                <c:forEach var="s" items="${statuses}">
                  <option value="${s}" ${editUser.statusCode == s ? 'selected' : ''}>${s}</option>
                </c:forEach>
              </select>
            </div>

            <div>
              <label>🔒 Khóa tài khoản đến</label>
              <input name="lockedUntil" value="${editUser.lockedUntil}" placeholder="yyyy-MM-ddThh:mm:ss">
            </div>

            <div class="full form-actions" style="border-top: 1px solid var(--bk-border); padding-top: 16px;">
              <button class="btn" type="submit">💾 Lưu thông tin</button>
              <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-muted">✖️ Hủy</a>
            </div>
          </form>

          <form method="post" action="${pageContext.request.contextPath}/admin/users" style="margin-top: 12px;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa tài khoản này?');">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="userId" value="${editUser.userId}">
            <button class="btn btn-danger btn-small" type="submit">🗑️ Xóa vĩnh viễn tài khoản này</button>
          </form>

          <!-- Reset Password Section -->
          <div style="margin-top: 24px; padding-top: 20px; border-top: 2px dashed var(--bk-border);">
            <h3 style="font-size: 15px; font-weight: 700; color: var(--bk-navy); margin-top: 0; margin-bottom: 12px;">🔑 Trực tiếp đặt lại Mật khẩu</h3>
            <form method="post" action="${pageContext.request.contextPath}/admin/users" class="form-grid">
              <input type="hidden" name="action" value="resetPassword">
              <input type="hidden" name="userId" value="${editUser.userId}">
              <div class="full">
                <label>Mật khẩu mới *</label>
                <input type="password" name="newPassword" required placeholder="Tối thiểu 8 ký tự (chữ & số)">
              </div>
              <div class="full form-actions">
                <button class="btn btn-danger" type="submit">⚡ Cập nhật mật khẩu mới</button>
              </div>
            </form>
          </div>

          <!-- Send Email Reset Link Section -->
          <div style="margin-top: 20px; padding: 14px; background: var(--bk-gold-light); border: 1px solid var(--bk-gold-border); border-radius: 8px;">
            <h3 style="font-size: 14px; font-weight: 700; color: #92400e; margin-top: 0; margin-bottom: 6px;">📧 Cấp lại Mật khẩu qua Email</h3>
            <p style="font-size: 13px; color: #78350f; margin-bottom: 10px;">Hệ thống tự động sinh một mật khẩu mới ngẫu nhiên và gửi trực tiếp đến địa chỉ email <b><c:out value="${editUser.email}"/></b>.</p>
            <form method="post" action="${pageContext.request.contextPath}/admin/users">
              <input type="hidden" name="action" value="sendResetLink">
              <input type="hidden" name="userId" value="${editUser.userId}">
              <button class="btn btn-gold btn-small" type="submit">📩 Cấp & gửi mật khẩu mới</button>
            </form>
          </div>
        </c:when>

        <c:otherwise>
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px; border-bottom: 1px solid var(--bk-border); padding-bottom: 12px;">
            <h2 style="margin:0;">✨ Tạo Tài khoản Nhân viên Mới</h2>
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-muted">← Quay lại danh sách</a>
          </div>
          <form method="post" action="${pageContext.request.contextPath}/admin/users" class="form-grid">
            <input type="hidden" name="action" value="create">

            <div class="full">
              <label>📧 Email Đăng nhập *</label>
              <input type="email" name="email" value="<c:out value='${param.email}'/>" required placeholder="nhanvien@bookinghotel.vn">
            </div>

            <div class="full">
              <label>👤 Họ và tên *</label>
              <input name="fullName" value="<c:out value='${param.fullName}'/>" required placeholder="Nguyễn Văn A">
            </div>

            <div>
              <label>📞 Số điện thoại</label>
              <input name="phone" value="<c:out value='${param.phone}'/>" placeholder="0901234567">
            </div>

            <div>
              <label>🪪 Định danh (CCCD/Passport)</label>
              <input name="identificationNumber" value="<c:out value='${param.identificationNumber}'/>" placeholder="001090001234">
            </div>

            <div class="full">
              <label>🏠 Địa chỉ</label>
              <input name="address" value="<c:out value='${param.address}'/>" placeholder="123 Nguyễn Huệ, Q.1, TP.HCM">
            </div>

            <div>
              <label>🛡️ Vai trò nhân viên *</label>
              <select name="roleCode" required>
                <c:forEach var="r" items="${roles}">
                  <c:if test="${r != 'CUSTOMER'}">
                    <option value="${r}" ${(not empty param.roleCode ? param.roleCode == r : r == 'RECEPTIONIST') ? 'selected' : ''}>${r}</option>
                  </c:if>
                </c:forEach>
              </select>
            </div>

            <div>
              <label>🏢 Bộ phận</label>
              <select name="departmentCode">
                <option value="">-- Chọn bộ phận --</option>
                <c:forEach var="d" items="${departments}">
                  <option value="${d}" ${param.departmentCode == d ? 'selected' : ''}>${d}</option>
                </c:forEach>
              </select>
            </div>

            <div class="full">
              <label>🔑 Mật khẩu ban đầu</label>
              <input type="password" name="password" placeholder="Bỏ trống để gửi email tự kích hoạt & tạo mật khẩu">
            </div>

            <div class="full form-actions" style="margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--bk-border);">
              <button class="btn" type="submit" style="width: 100%;">➕ Tạo tài khoản nhân viên</button>
            </div>
          </form>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</c:if>

<%@ include file="_footer.jspf" %>

