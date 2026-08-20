<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:420px;margin:40px auto">
  <h2>Đăng ký tài khoản</h2>
  <form method="post" action="${pageContext.request.contextPath}/register">
    <input type="hidden" name="redirect" value="${not empty redirect ? redirect : param.redirect}">
    <label>Họ tên</label><input name="fullName" style="width:100%" value="${param.fullName}" required>
    <label>Email</label><input type="email" name="email" style="width:100%" value="${param.email}" required>
    <label>Số điện thoại</label><input name="phone" style="width:100%" value="${param.phone}">
    <label>Mật khẩu (≥8 ký tự, có chữ và số)</label><input type="password" name="password" style="width:100%" required>
    <p><button class="btn btn-success" type="submit" style="width:100%">Đăng ký</button></p>
  </form>
  <p><a href="${pageContext.request.contextPath}/login<c:if test='${not empty (not empty redirect ? redirect : param.redirect)}'>?redirect=${not empty redirect ? redirect : param.redirect}</c:if>">Đã có tài khoản? Đăng nhập</a></p>
</div>
<%@ include file="_footer.jspf" %>
