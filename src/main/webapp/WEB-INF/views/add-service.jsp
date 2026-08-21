<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="_header.jspf" %>

<style>
    /* Nền hệ thống */
    .admin-main-container {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        background-color: #f5f5f5;
        padding: 24px 32px;
        min-height: 100vh;
        color: #1a1a1a;
    }

    /* Banner xanh đậm */
    .admin-banner {
        background-color: #003b95;
        color: white;
        border-radius: 8px;
        padding: 28px 32px;
        margin-bottom: 24px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .admin-banner-title { margin: 0 0 8px 0; font-size: 22px; font-weight: 700; display: flex; align-items: center; gap: 10px; }
    .admin-banner-sub { margin: 0; color: #add3ff; font-size: 14px; }

    /* Card trắng nhập liệu */
    .admin-card {
        background: white;
        border-radius: 8px;
        padding: 32px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        max-width: 800px;
    }
    .admin-card-header {
        font-size: 16px;
        font-weight: 700;
        margin-bottom: 24px;
        color: #1a1a1a;
        display: flex;
        align-items: center;
        gap: 8px;
        border-bottom: 1px solid #eaeaea;
        padding-bottom: 12px;
    }

    /* Form Styles */
    .form-row { display: flex; gap: 24px; margin-bottom: 16px; }
    .form-group { flex: 1; margin-bottom: 16px; }
    .form-group label { display: block; font-size: 13px; font-weight: 600; color: #333; margin-bottom: 8px; }
    .form-control { width: 100%; padding: 10px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; outline: none; }
    .form-control:focus { border-color: #006ce4; box-shadow: 0 0 0 1px #006ce4; }

    /* Buttons */
    .btn { padding: 9px 18px; border-radius: 4px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; transition: background 0.2s; }
    .btn-primary { background-color: #006ce4; color: white; }
    .btn-primary:hover { background-color: #0056b3; }
    .btn-outline { background-color: transparent; color: #006ce4; border: 1px solid #006ce4; text-decoration: none; display: inline-block; text-align: center; margin-left: 12px; }
    .btn-outline:hover { background-color: #f0f6ff; }
</style>

<div class="admin-main-container">

    <!-- Header Banner -->
    <div class="admin-banner">
        <h1 class="admin-banner-title">✨ Thêm mới Dịch vụ</h1>
        <p class="admin-banner-sub">Cập nhật danh mục dịch vụ tiện ích để cung cấp cho khách hàng lưu trú.</p>
    </div>

    <!-- Main Card -->
    <div class="admin-card">
        <div class="admin-card-header">
            📝 Thông tin dịch vụ
        </div>

        <c:if test="${not empty error}">
            <div style="background: #f8d7da; color: #721c24; padding: 12px; border-radius: 4px; margin-bottom: 20px; font-size: 14px;">
                    ${error}
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/admin/services/add">
            <div class="form-row">
                <div class="form-group">
                    <label>Mã dịch vụ (Code) *</label>
                    <input type="text" name="serviceCode" class="form-control" placeholder="Ví dụ: LNDRY, SPA01..." required>
                </div>
                <div class="form-group">
                    <label>Tên dịch vụ (Name) *</label>
                    <input type="text" name="serviceName" class="form-control" placeholder="Nhập tên dịch vụ hiển thị..." required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Đơn vị tính *</label>
                    <input type="text" name="unitName" class="form-control" placeholder="Ví dụ: Giờ, Lượt, Bộ..." required>
                </div>
                <div class="form-group">
                    <label>Đơn giá (VNĐ) *</label>
                    <input type="number" name="unitPrice" step="0.01" min="0" class="form-control" placeholder="0.00" required>
                </div>
            </div>

            <div class="form-group">
                <label>Đường dẫn hình ảnh (URL) *</label>
                <input type="url" name="imageUrl" class="form-control" placeholder="https://example.com/image.jpg" required>
            </div>

            <div class="form-group">
                <label>Mô tả chi tiết</label>
                <textarea name="description" rows="4" class="form-control" placeholder="Mô tả công năng và chi tiết của dịch vụ này..."></textarea>
            </div>

            <div style="margin-top: 32px; padding-top: 20px; border-top: 1px solid #eaeaea;">
                <button class="btn btn-primary" type="submit">Lưu dữ liệu</button>
                <a href="${pageContext.request.contextPath}/services" class="btn btn-outline">Hủy thao tác</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="_footer.jspf" %>