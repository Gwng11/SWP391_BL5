<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Có lỗi xảy ra</title>
<style>
body{font-family:Segoe UI,Arial,sans-serif;background:#f5f6fa;color:#2d3436;display:flex;align-items:center;justify-content:center;min-height:90vh;margin:0}
.box{background:#fff;border-radius:8px;padding:40px;max-width:520px;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,.08)}
a{display:inline-block;background:#2980b9;color:#fff;padding:9px 18px;border-radius:5px;text-decoration:none;margin-top:16px}
.detail{color:#636e72;font-size:13px;margin-top:12px;word-break:break-word}
</style>
</head>
<body>
<div class="box">
  <h1>😕 Có lỗi xảy ra</h1>
  <p>Yêu cầu không thực hiện được. Vui lòng quay lại và thử lại thao tác.</p>
  <% if (exception != null && exception.getMessage() != null) { %>
    <p class="detail"><%= exception.getMessage().replace("<", "&lt;") %></p>
  <% } %>
  <a href="${pageContext.request.contextPath}/home">← Về trang chủ</a>
</div>
</body>
</html>
