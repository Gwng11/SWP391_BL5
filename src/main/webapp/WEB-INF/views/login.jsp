<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:420px;margin:40px auto">
  <h2>Đăng nhập</h2>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <label>Email</label><input type="email" name="email" style="width:100%" required>
    <label>Mật khẩu</label><input type="password" name="password" style="width:100%" required>
    <p><button class="btn" type="submit" style="width:100%">Đăng nhập</button></p>
  </form>
  <p><a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
   &nbsp;|&nbsp; <a href="${pageContext.request.contextPath}/register">Đăng ký tài khoản</a></p>
</div>
<%@ include file="_footer.jspf" %>
