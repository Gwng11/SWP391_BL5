<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="page-head">
  <h1>Quản lý Tài khoản & Vai trò</h1>
  <div>
    <form method="get" class="inline">
      <input name="q" placeholder="Tìm theo tên/email/sđt" value="${param.q}" style="width:200px">
      <select name="roleCode">
        <option value="">-- Tất cả vai trò --</option>
        <c:forEach var="r" items="${roles}">
          <option value="${r}" ${param.roleCode == r ? 'selected' : ''}>${r}</option>
        </c:forEach>
      </select>
      <select name="statusCode">
        <option value="">-- Tất cả trạng thái --</option>
        <c:forEach var="s" items="${statuses}">
          <option value="${s}" ${param.statusCode == s ? 'selected' : ''}>${s}</option>
        </c:forEach>
      </select>
      <button class="btn btn-muted" type="submit">Lọc</button>
      <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-muted">Reset</a>
    </form>
  </div>
</div>

<div class="grid">
  <!-- Left Column: Users List -->
  <div class="card col-8 table-wrap">
    <h2>Danh sách nhân viên & khách hàng</h2>
    <table>
      <thead>
        <tr>
          <th>Họ tên</th>
          <th>Email</th>
          <th>Vai trò</th>
          <th>Bộ phận</th>
          <th>Trạng thái</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="u" items="${users}">
          <tr>
            <td><c:out value="${u.fullName}"/></td>
            <td><c:out value="${u.email}"/></td>
            <td><span class="badge">${u.roleCode}</span></td>
            <td><c:out value="${u.departmentCode}"/></td>
            <td>
              <span class="badge status-${u.statusCode}">${u.statusCode}</span>
              <c:if test="${not empty u.lockedUntil}">
                <br/><small class="muted">Khóa đến: ${u.lockedUntil}</small>
              </c:if>
            </td>
            <td style="white-space:nowrap;">
              <div class="inline">
                <a class="btn btn-small" href="?edit=${u.userId}&q=${param.q}&roleCode=${param.roleCode}&statusCode=${param.statusCode}">Sửa</a>
                <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa tài khoản này?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="userId" value="${u.userId}">
                  <button class="btn btn-danger btn-small" type="submit">Xóa</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty users}">
          <tr><td colspan="6" class="empty">Không tìm thấy tài khoản nào.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- Right Column: Detail Form (Create / Edit) -->
  <div class="card col-4">
    <c:choose>
      <c:when test="${not empty editUser}">
        <h2>Cập nhật tài khoản</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/users" class="form-grid">
          <input type="hidden" name="action" value="update">
          <input type="hidden" name="userId" value="${editUser.userId}">
          <input type="hidden" name="q" value="${param.q}">
          <input type="hidden" name="roleCodeFilter" value="${param.roleCode}">
          <input type="hidden" name="statusCodeFilter" value="${param.statusCode}">

          <div class="full">
            <label>Email (Không được thay đổi)</label>
            <input value="${editUser.email}" disabled style="width:100%">
          </div>
          <div class="full">
            <label>Họ tên *</label>
            <input name="fullName" required value="${editUser.fullName}" style="width:100%">
          </div>
          <div>
            <label>Số điện thoại</label>
            <input name="phone" value="${editUser.phone}" style="width:100%">
          </div>
          <div>
            <label>Định danh (CCCD/Passport)</label>
            <input name="identificationNumber" value="${editUser.identificationNumber}" style="width:100%">
          </div>
          <div class="full">
            <label>Địa chỉ</label>
            <input name="address" value="${editUser.address}" style="width:100%">
          </div>
          <div>
            <label>Vai trò *</label>
            <select name="roleCode" style="width:100%" required>
              <c:forEach var="r" items="${roles}">
                <option value="${r}" ${editUser.roleCode == r ? 'selected' : ''}>${r}</option>
              </c:forEach>
            </select>
          </div>
          <div>
            <label>Bộ phận</label>
            <select name="departmentCode" style="width:100%">
              <option value="">-- Không bộ phận --</option>
              <c:forEach var="d" items="${departments}">
                <option value="${d}" ${editUser.departmentCode == d ? 'selected' : ''}>${d}</option>
              </c:forEach>
            </select>
          </div>
          <div>
            <label>Trạng thái *</label>
            <select name="statusCode" style="width:100%" required>
              <c:forEach var="s" items="${statuses}">
                <option value="${s}" ${editUser.statusCode == s ? 'selected' : ''}>${s}</option>
              </c:forEach>
            </select>
          </div>
          <div>
            <label>Khóa đến (yyyy-MM-ddThh:mm:ss)</label>
            <input name="lockedUntil" value="${editUser.lockedUntil}" placeholder="Ví dụ: 2026-08-20T12:00:00" style="width:100%">
          </div>
          <div class="full form-actions">
            <button class="btn" type="submit">Lưu thông tin</button>
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-muted">Hủy / Tạo mới</a>
          </div>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:inline-block; margin-top:8px;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa tài khoản này?');">
          <input type="hidden" name="action" value="delete">
          <input type="hidden" name="userId" value="${editUser.userId}">
          <button class="btn btn-danger" type="submit">Xóa tài khoản này</button>
        </form>

        <hr style="margin:20px 0; border:0; border-top:1px solid var(--line)"/>

        <h3>Đặt lại mật khẩu</h3>
        <form method="post" action="${pageContext.request.contextPath}/admin/users" class="form-grid">
          <input type="hidden" name="action" value="resetPassword">
          <input type="hidden" name="userId" value="${editUser.userId}">
          <div class="full">
            <label>Mật khẩu mới *</label>
            <input type="password" name="newPassword" required placeholder="Mật khẩu >= 8 ký tự, có chữ và số" style="width:100%">
          </div>
          <div class="full form-actions">
            <button class="btn btn-danger" type="submit">Đổi mật khẩu</button>
          </div>
        </form>

        <hr style="margin:20px 0; border:0; border-top:1px solid var(--line)"/>

        <h3>Khôi phục qua Email</h3>
        <form method="post" action="${pageContext.request.contextPath}/admin/users">
          <input type="hidden" name="action" value="sendResetLink">
          <input type="hidden" name="userId" value="${editUser.userId}">
          <p><button class="btn btn-muted" type="submit">Gửi link đặt lại mật khẩu qua email</button></p>
        </form>
      </c:when>

      <c:otherwise>
        <h2>Tạo tài khoản nhân viên</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/users" class="form-grid">
          <input type="hidden" name="action" value="create">

          <div class="full">
            <label>Email đăng nhập *</label>
            <input type="email" name="email" required placeholder="example@hotel.vn" style="width:100%">
          </div>
          <div class="full">
            <label>Họ và tên *</label>
            <input name="fullName" required style="width:100%">
          </div>
          <div>
            <label>Số điện thoại</label>
            <input name="phone" style="width:100%">
          </div>
          <div>
            <label>Định danh (CCCD/Passport)</label>
            <input name="identificationNumber" style="width:100%">
          </div>
          <div class="full">
            <label>Địa chỉ</label>
            <input name="address" style="width:100%">
          </div>
          <div>
            <label>Vai trò *</label>
            <select name="roleCode" style="width:100%" required>
              <c:forEach var="r" items="${roles}">
                <c:if test="${r != 'CUSTOMER'}">
                  <option value="${r}" ${r == 'RECEPTIONIST' ? 'selected' : ''}>${r}</option>
                </c:if>
              </c:forEach>
            </select>
          </div>
          <div>
            <label>Bộ phận</label>
            <select name="departmentCode" style="width:100%">
              <option value="">-- Không bộ phận --</option>
              <c:forEach var="d" items="${departments}">
                <option value="${d}">${d}</option>
              </c:forEach>
            </select>
          </div>
          <div class="full">
            <label>Mật khẩu ban đầu</label>
            <input type="password" name="password" placeholder="Bỏ trống để gửi email kích hoạt tự thiết lập mật khẩu" style="width:100%">
          </div>
          <div class="full form-actions" style="margin-top:20px">
            <button class="btn" type="submit">Tạo tài khoản</button>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>
<%@ include file="_footer.jspf" %>
