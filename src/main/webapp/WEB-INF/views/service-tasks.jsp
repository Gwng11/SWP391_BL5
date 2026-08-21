<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
  /* Reset cơ bản cho phần nội dung */
  .admin-main-container {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    background-color: #f5f5f5;
    padding: 32px 48px;
    min-height: 100vh;
    color: #1a1a1a;
  }

  /* Banner xanh đậm (Booking style) */
  .admin-banner {
    background-color: #003b95;
    color: white;
    border-radius: 12px;
    padding: 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
  .admin-banner-title {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .admin-banner-sub {
    font-size: 14px;
    color: #add3ff;
    margin: 0;
  }

  /* Khung Filter viền vàng */
  .admin-filter-box {
    background-color: white;
    border: 2px solid #febb02;
    border-radius: 8px;
    padding: 20px 24px;
    margin-bottom: 24px;
    display: flex;
    align-items: flex-end;
    gap: 20px;
  }
  .admin-filter-group {
    flex: 1;
    max-width: 300px;
  }
  .admin-filter-label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: #006ce4;
    margin-bottom: 8px;
  }
  .admin-filter-select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 14px;
    outline: none;
  }
  .admin-filter-select:focus {
    border-color: #006ce4;
  }

  /* Card chứa bảng (Table) */
  .admin-card {
    background: white;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.04);
  }
  .admin-card-header {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 20px;
    color: #1a1a1a;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  /* Bảng dữ liệu */
  .admin-table {
    width: 100%;
    border-collapse: collapse;
  }
  .admin-table th {
    text-align: left;
    padding: 12px 16px;
    font-size: 12px;
    font-weight: 600;
    color: #555;
    text-transform: uppercase;
    border-bottom: 2px solid #f0f0f0;
  }
  .admin-table td {
    padding: 16px;
    font-size: 14px;
    vertical-align: middle;
    border-bottom: 1px solid #f0f0f0;
  }
  .admin-table tr:hover td {
    background-color: #f9f9f9;
  }

  /* Nhãn trạng thái (Badge) */
  .status-badge {
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 700;
    display: inline-block;
    border: 1px solid transparent;
  }
  .status-badge.pending { background: #fff3cd; color: #856404; border-color: #ffeeba; }
  .status-badge.assigned { background: #cce5ff; color: #004085; border-color: #b8daff; }
  .status-badge.in_progress { background: #d4edda; color: #155724; border-color: #c3e6cb; }
  .status-badge.completed { background: #e2e3e5; color: #383d41; border-color: #d6d8db; }

  /* Nút bấm (Buttons) */
  .btn {
    padding: 8px 16px;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    border: none;
    transition: background-color 0.2s;
  }
  .btn-primary { background-color: #006ce4; color: white; }
  .btn-primary:hover { background-color: #0056b3; }
  .btn-success { background-color: #008234; color: white; }
  .btn-success:hover { background-color: #00692a; }
  .btn-danger { background-color: transparent; color: #d4111e; border: 1px solid #d4111e; padding: 7px 15px; }
  .btn-danger:hover { background-color: #d4111e; color: white; }
  .btn-outline { background-color: white; color: #006ce4; border: 1px solid #006ce4; }

  .input-assign {
    padding: 7px 12px;
    border: 1px solid #ccc;
    border-radius: 4px;
    width: 120px;
    font-size: 13px;
    outline: none;
  }
</style>

<div class="admin-main-container">

  <!-- Banner -->
  <div class="admin-banner">
    <div>
      <h1 class="admin-banner-title">📋 Quản lý Yêu cầu Dịch vụ</h1>
      <p class="admin-banner-sub">Phân công nhân sự, theo dõi trạng thái và xử lý các yêu cầu từ khách hàng</p>
    </div>
  </div>

  <!-- Bộ lọc -->
  <form method="get" action="${pageContext.request.contextPath}/staff/service-requests" class="admin-filter-box">
    <div class="admin-filter-group">
      <label class="admin-filter-label">⚡ Trạng thái yêu cầu</label>
      <select name="status" class="admin-filter-select">
        <option value="">-- Tất cả trạng thái --</option>
        <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>PENDING (Chờ xử lý)</option>
        <option value="ASSIGNED" ${statusFilter == 'ASSIGNED' ? 'selected' : ''}>ASSIGNED (Đã phân công)</option>
        <option value="IN_PROGRESS" ${statusFilter == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS (Đang thực hiện)</option>
        <option value="COMPLETED" ${statusFilter == 'COMPLETED' ? 'selected' : ''}>COMPLETED (Đã hoàn tất)</option>
      </select>
    </div>
    <div>
      <button class="btn btn-primary" type="submit">Lọc dữ liệu</button>
      <a href="${pageContext.request.contextPath}/staff/service-requests" class="btn btn-outline" style="text-decoration: none; display: inline-block; margin-left: 8px;">Đặt lại</a>
    </div>
  </form>

  <!-- Danh sách -->
  <div class="admin-card">
    <div class="admin-card-header">
      📑 Danh sách Yêu cầu
    </div>

    <table class="admin-table">
      <thead>
      <tr>
        <th>#</th>
        <th>Mã Đơn</th>
        <th>Dịch vụ</th>
        <th>Số lượng</th>
        <th>Tổng tiền</th>
        <th>Hẹn lúc</th>
        <th>Trạng thái</th>
        <th>Phụ trách</th>
        <th style="text-align: right;">Thao tác</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="s" items="${requests}">
        <tr>
          <td style="font-weight: 700; color: #006ce4;">#${s.serviceRequestId}</td>
          <td style="font-weight: 600;">${s.bookingCode}</td>
          <td>${s.serviceName}</td>
          <td>${s.quantity} ${s.unitName}</td>
          <td style="font-weight: 700;"><fmt:formatNumber value="${s.totalAmount}"/> đ</td>
          <td style="color: #555; font-size: 13px;">${s.scheduledAt}</td>
          <td>
            <span class="status-badge ${s.statusCode.toLowerCase()}">${s.statusCode}</span>
          </td>
          <td>
            <c:choose>
              <c:when test="${empty s.staffName}">
                <em style="color:#999; font-size: 13px;">Chưa gán</em>
              </c:when>
              <c:otherwise>
                <span style="font-weight: 600;">${s.staffName}</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td style="text-align: right;">
            <div style="display: flex; gap: 8px; justify-content: flex-end; align-items: center;">

              <c:if test="${s.statusCode == 'PENDING'}">
                <form method="post" style="display:inline-flex; gap:6px; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="assign">
                  <input type="number" name="staffUserId" placeholder="ID NV (Trống=Tôi)" class="input-assign">
                  <button class="btn btn-primary" type="submit">Nhận việc</button>
                </form>
              </c:if>

              <c:if test="${s.statusCode == 'ASSIGNED'}">
                <form method="post" style="display:inline; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="start">
                  <button class="btn btn-primary" type="submit">Bắt đầu</button>
                </form>
              </c:if>

              <c:if test="${s.statusCode == 'ASSIGNED' || s.statusCode == 'IN_PROGRESS'}">
                <form method="post" style="display:inline; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="complete">
                  <button class="btn btn-success" type="submit">Hoàn tất</button>
                </form>
              </c:if>

              <c:if test="${s.statusCode != 'COMPLETED' && s.statusCode != 'CANCELLED'}">
                <form method="post" style="display:inline; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests" onsubmit="return confirm('Bạn có chắc chắn muốn hủy yêu cầu này?')">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="cancel">
                  <button class="btn btn-danger" type="submit">Hủy</button>
                </form>
              </c:if>

            </div>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<%@ include file="_footer.jspf" %>