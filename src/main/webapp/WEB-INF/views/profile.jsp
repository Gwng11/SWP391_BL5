<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Hồ sơ cá nhân</h2>
  <form method="post" action="${pageContext.request.contextPath}/profile" style="max-width:520px">
    <label>Email (không đổi được)</label><input value="${user.email}" disabled style="width:100%">
    <label>Họ tên</label><input name="fullName" value="${user.fullName}" style="width:100%" required>
    <label>Số điện thoại</label><input name="phone" value="${user.phone}" style="width:100%">
    <c:if test="${user.roleCode == 'CUSTOMER'}">
      <label>Ngày sinh</label><input type="date" name="dateOfBirth" value="${customer.dateOfBirth}" style="width:100%">
      <label>Loại giấy tờ</label>
      <select name="idDocumentType" style="width:100%">
        <option value="">-- không --</option>
        <option value="CCCD" ${customer.idDocumentType == 'CCCD' ? 'selected' : ''}>CCCD</option>
        <option value="PASSPORT" ${customer.idDocumentType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
      </select>
      <label>Số giấy tờ</label><input name="idDocumentNumber" value="${customer.idDocumentNumber}" style="width:100%">
      <label>Quốc tịch</label><input name="nationality" value="${customer.nationality}" style="width:100%">
      <label>Địa chỉ</label><input name="address" value="${customer.address}" style="width:100%">
    </c:if>
    <p><button class="btn" type="submit">Lưu thay đổi</button></p>
  </form>
</div>
<div class="card">
  <h3>Đổi mật khẩu</h3>
  <form method="post" action="${pageContext.request.contextPath}/profile" style="max-width:520px">
    <input type="hidden" name="action" value="changePassword">
    <label>Mật khẩu hiện tại</label><input type="password" name="oldPassword" style="width:100%" required>
    <label>Mật khẩu mới</label><input type="password" name="newPassword" style="width:100%" required>
    <p><button class="btn btn-danger" type="submit">Đổi mật khẩu</button></p>
  </form>
</div>
<%@ include file="_footer.jspf" %>
