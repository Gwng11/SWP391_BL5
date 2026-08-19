<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" style="max-width:420px;margin:40px auto">
  <h2>Quên mật khẩu</h2>
  <form method="post" action="${pageContext.request.contextPath}/forgot-password">
    <label>Email đã đăng ký</label><input type="email" name="email" style="width:100%" required>
    <p><button class="btn" type="submit" style="width:100%">Gửi link đặt lại mật khẩu</button></p>
  </form>
</div>
<%@ include file="_footer.jspf" %>
