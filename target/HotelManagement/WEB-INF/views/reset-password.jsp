<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:420px;margin:40px auto">
  <h2>Đặt lại mật khẩu</h2>
  <form method="post" action="${pageContext.request.contextPath}/reset-password">
    <input type="hidden" name="token" value="${token}">
    <label>Mật khẩu mới</label><input type="password" name="password" style="width:100%" required>
    <p><button class="btn btn-success" type="submit" style="width:100%">Xác nhận</button></p>
  </form>
</div>
<%@ include file="_footer.jspf" %>
