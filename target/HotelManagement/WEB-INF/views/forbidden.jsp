<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card" role="alert">
  <h2>403 - Không có quyền truy cập</h2>
  <p>${err}</p>
  <a class="btn" href="${pageContext.request.contextPath}/home">Về trang chủ</a>
</div>
<%@ include file="_footer.jspf" %>
