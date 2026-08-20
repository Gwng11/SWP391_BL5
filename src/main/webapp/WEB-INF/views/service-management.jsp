<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
    * { box-sizing: border-box; }
    body { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9; color: #333; }
    .lux-wrapper { display: flex; min-height: 100vh; }
    .lux-sidebar { width: 260px; background-color: #0b1b42; color: #fff; display: flex; flex-direction: column; }
    .lux-brand { font-size: 1.5rem; font-weight: bold; padding: 24px; color: #fff; letter-spacing: 0.5px; }
    .lux-nav { list-style: none; padding: 0; margin: 0; }
    .lux-nav li a { display: block; padding: 14px 24px; color: #9ba4b5; text-decoration: none; font-weight: 500; }
    .lux-nav li a:hover { color: #fff; background: rgba(255,255,255,0.05); }
    .lux-nav li.active a { color: #0b1b42; background-color: #e5b945; border-radius: 0 20px 20px 0; margin-right: 20px; font-weight: 600; }
    .lux-main { flex: 1; display: flex; flex-direction: column; }
    .lux-topbar { background: #fff; padding: 16px 32px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .lux-content { padding: 32px; flex: 1; }

    .lux-layout-two-col { display: grid; grid-template-columns: 1fr 380px; gap: 24px; }
    .lux-card { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
    .lux-card-title { font-size: 1.2rem; font-weight: 600; margin-top: 0; margin-bottom: 16px; color: #0b1b42; }

    .lux-table { width: 100%; border-collapse: collapse; }
    .lux-table th, .lux-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 0.9rem; }
    .lux-table th { background: #f8fafc; color: #475569; font-weight: 600; }

    .lux-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; display: inline-block; }
    .lux-badge-success { background: #d1fae5; color: #065f46; }
    .lux-badge-danger { background: #fee2e2; color: #991b1b; }

    .lux-form-group { margin-bottom: 14px; }
    .lux-form-group label { display: block; font-weight: 600; margin-bottom: 4px; font-size: 0.85rem; color: #444; }
    .lux-form-control { width: 100%; padding: 8px 12px; border-radius: 6px; border: 1px solid #ccc; font-size: 0.9rem; }
    .lux-btn { background: #0b1b42; color: #fff; border: none; padding: 10px 18px; border-radius: 6px; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-block; }
    .lux-btn-sm { padding: 4px 10px; font-size: 0.8rem; }
    .lux-btn-warning { background: #f59e0b; color: #fff; }
    .lux-btn-secondary { background: #6b7280; color: #fff; }

    .lux-alert { padding: 12px; border-radius: 6px; margin-bottom: 16px; font-size: 0.9rem; }
    .lux-alert-success { background: #d1fae5; color: #065f46; }
    .lux-alert-error { background: #fee2e2; color: #991b1b; }
</style>

<div class="lux-wrapper">
    <!-- Sidebar LuxeStay HMS bên trái -->
    <aside class="lux-sidebar">
        <div class="lux-brand">LuxeStay HMS</div>
        <ul class="lux-nav">
            <li><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
            <li><a href="${pageContext.request.contextPath}/services">Dịch vụ Khách sạn</a></li>
            <li class="active"><a href="${pageContext.request.contextPath}/manager/services">Quản lý Dịch vụ</a></li>
        </ul>
    </aside>

    <main class="lux-main">
        <header class="lux-topbar">
            <h2>Quản lý Danh mục Dịch vụ</h2>
        </header>

        <div class="lux-content">
            <c:if test="${param.ok == 'created'}"><div class="lux-alert lux-alert-success">Thêm dịch vụ thành công!</div></c:if>
            <c:if test="${param.ok == 'updated'}"><div class="lux-alert lux-alert-success">Cập nhật dịch vụ thành công!</div></c:if>
            <c:if test="${param.ok == 'toggled'}"><div class="lux-alert lux-alert-success">Đã thay đổi trạng thái dịch vụ!</div></c:if>
            <c:if test="${not empty param.err}"><div class="lux-alert lux-alert-error">${param.err}</div></c:if>

            <div class="lux-layout-two-col">
                <!-- Bảng Danh sách Dịch vụ -->
                <div class="lux-card">
                    <h3 class="lux-card-title">Danh sách dịch vụ hiện có</h3>
                    <table class="lux-table">
                        <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Mã / Tên</th>
                            <th>Đơn giá</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${services}">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.imageUrl}">
                                            <img src="${s.imageUrl}" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                                        </c:when>
                                        <c:otherwise>
                                            <div style="width: 40px; height: 40px; background: #eee; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 10px; color: #aaa;">No img</div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <b>${s.serviceName}</b><br>
                                    <small style="color:#888;">${s.serviceCode} (${s.unitName})</small>
                                </td>
                                <td><fmt:formatNumber value="${s.unitPrice}"/> đ</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.active}">
                                            <span class="lux-badge lux-badge-success">Đang mở</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="lux-badge lux-badge-danger">Đang ẩn</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div style="display: flex; gap: 6px;">
                                        <a href="${pageContext.request.contextPath}/manager/services?id=${s.hotelServiceId}" class="lux-btn lux-btn-sm lux-btn-warning">Sửa</a>
                                        <form method="post" action="${pageContext.request.contextPath}/manager/services" style="display:inline;">
                                            <input type="hidden" name="action" value="toggle">
                                            <input type="hidden" name="hotelServiceId" value="${s.hotelServiceId}">
                                            <input type="hidden" name="active" value="${!s.active}">
                                            <button type="submit" class="lux-btn lux-btn-sm lux-btn-secondary">
                                                    ${s.active ? 'Ẩn' : 'Hiện'}
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Form Thêm mới / Cập nhật Dịch vụ -->
                <div class="lux-card">
                    <h3 class="lux-card-title">${not empty editService ? 'Chỉnh sửa dịch vụ' : 'Thêm dịch vụ mới'}</h3>
                    <form method="post" action="${pageContext.request.contextPath}/manager/services">
                        <input type="hidden" name="action" value="${not empty editService ? 'update' : 'create'}">
                        <c:if test="${not empty editService}">
                            <input type="hidden" name="hotelServiceId" value="${editService.hotelServiceId}">
                        </c:if>

                        <div class="lux-form-group">
                            <label>Mã dịch vụ (Service Code)</label>
                            <input type="text" name="serviceCode" value="${editService.serviceCode}" required class="lux-form-control" placeholder="VD: SPA01, LAUNDRY">
                        </div>

                        <div class="lux-form-group">
                            <label>Tên dịch vụ</label>
                            <input type="text" name="serviceName" value="${editService.serviceName}" required class="lux-form-control" placeholder="VD: Giặt ủi cao cấp">
                        </div>

                        <div class="lux-form-group">
                            <label>Đơn vị tính</label>
                            <input type="text" name="unitName" value="${editService.unitName}" required class="lux-form-control" placeholder="VD: Lượt, Kg, Bộ">
                        </div>

                        <div class="lux-form-group">
                            <label>Đơn giá (VNĐ)</label>
                            <input type="number" name="unitPrice" value="${editService.unitPrice}" step="1000" min="0" required class="lux-form-control">
                        </div>

                        <div class="lux-form-group">
                            <label>Đường dẫn hình ảnh (URL)</label>
                            <input type="text" name="imageUrl" value="${editService.imageUrl}" class="lux-form-control" placeholder="VD: https://.../image.jpg">
                        </div>

                        <div class="lux-form-group">
                            <label>Mô tả dịch vụ</label>
                            <textarea name="description" rows="3" class="lux-form-control">${editService.description}</textarea>
                        </div>

                        <div style="display: flex; gap: 8px;">
                            <button type="submit" class="lux-btn" style="flex: 1;">${not empty editService ? 'Cập nhật' : 'Thêm mới'}</button>
                            <c:if test="${not empty editService}">
                                <a href="${pageContext.request.contextPath}/manager/services" class="lux-btn lux-btn-secondary">Hủy</a>
                            </c:if>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<%@ include file="_footer.jspf" %>