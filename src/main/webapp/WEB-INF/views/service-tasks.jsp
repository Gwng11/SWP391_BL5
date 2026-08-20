<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
  /* Reset & Base Setup */
  * { box-sizing: border-box; }
  body { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9; color: #333; }

  /* Layout Skeleton */
  .lux-wrapper { display: flex; min-height: 100vh; }

  /* Sidebar */
  .lux-sidebar { width: 260px; background-color: #0b1b42; color: #fff; display: flex; flex-direction: column; }
  .lux-brand { font-size: 1.5rem; font-weight: bold; padding: 24px; color: #fff; letter-spacing: 0.5px; }
  .lux-profile { padding: 0 24px 20px; display: flex; align-items: center; gap: 12px; }
  .lux-avatar { width: 40px; height: 40px; border-radius: 50%; background: #ccc; }
  .lux-profile-info { font-size: 0.85rem; }
  .lux-profile-info strong { display: block; font-size: 0.95rem; }
  .lux-nav { list-style: none; padding: 0; margin: 0; }
  .lux-nav li a { display: block; padding: 14px 24px; color: #9ba4b5; text-decoration: none; font-weight: 500; display: flex; align-items: center; gap: 12px; }
  .lux-nav li a:hover { color: #fff; background: rgba(255,255,255,0.05); }
  .lux-nav li.active a { color: #0b1b42; background-color: #e5b945; border-radius: 0 20px 20px 0; margin-right: 20px; font-weight: 600; }

  /* Main Content */
  .lux-main { flex: 1; display: flex; flex-direction: column; overflow-x: hidden; }

  /* Topbar */
  .lux-topbar { background: #fff; padding: 16px 32px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
  .lux-search input { padding: 10px 16px; border-radius: 20px; border: 1px solid #e0e0e0; width: 300px; background: #f9f9f9; outline: none; }

  /* Page Content */
  .lux-content { padding: 32px; flex: 1; }
  .lux-header-title { font-size: 1.8rem; font-weight: 600; margin: 0 0 4px 0; }
  .lux-header-sub { color: #666; font-size: 0.95rem; margin-bottom: 24px; }

  /* Filter Bar */
  .lux-filter-bar { background: #fff; padding: 16px 20px; border-radius: 12px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); display: flex; align-items: center; gap: 16px; }
  .lux-filter-bar select { padding: 10px; border-radius: 8px; border: 1px solid #e0e0e0; min-width: 200px; outline: none; font-size: 0.95rem; color: #444; }

  /* Card & Table */
  .lux-card { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
  .lux-table { width: 100%; border-collapse: collapse; }
  .lux-table th { text-align: left; padding: 16px; font-size: 0.8rem; text-transform: uppercase; color: #777; border-bottom: 2px solid #eee; }
  .lux-table td { padding: 16px; border-bottom: 1px solid #f0f0f0; vertical-align: middle; font-size: 0.95rem; }
  .lux-table tr:hover td { background-color: #fafbfc; }

  /* UI Elements */
  .lux-badge { padding: 6px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; display: inline-block; }
  .lux-badge.pending { background: #fff3cd; color: #856404; }
  .lux-badge.assigned { background: #cce5ff; color: #004085; }
  .lux-badge.in_progress { background: #d4edda; color: #155724; }
  .lux-badge.completed { background: #e2e3e5; color: #383d41; }

  .lux-btn { padding: 8px 16px; border-radius: 6px; border: none; cursor: pointer; font-weight: 600; font-size: 0.85rem; transition: all 0.2s; }
  .lux-btn-primary { background: #0b1b42; color: #fff; }
  .lux-btn-primary:hover { background: #1a2f63; }
  .lux-btn-success { background: #10b981; color: #fff; }
  .lux-btn-danger { background: transparent; color: #ef4444; border: 1px solid #ef4444; padding: 7px 15px; }
  .lux-btn-danger:hover { background: #ef4444; color: #fff; }
  .lux-input-small { padding: 8px; border: 1px solid #ddd; border-radius: 6px; width: 140px; outline: none; }
</style>

<div class="lux-wrapper">
  <!-- LEFT SIDEBAR -->
  <aside class="lux-sidebar">
    <div class="lux-brand">LuxeStay HMS</div>
    <div class="lux-profile">
      <div class="lux-avatar"></div>
      <div class="lux-profile-info">
        <strong>Service Desk</strong>
        <span>Staff Account</span>
      </div>
    </div>
    <ul class="lux-nav">
      <li><a href="#">Dashboard</a></li>
      <li><a href="#">Reservations</a></li>
      <li><a href="#">Reception</a></li>
      <li class="active"><a href="#">Housekeeping / Services</a></li>
      <li><a href="#">Management</a></li>
      <li><a href="#">Settings</a></li>
    </ul>
  </aside>

  <!-- MAIN CONTENT AREA -->
  <main class="lux-main">
    <!-- Top Bar -->
    <header class="lux-topbar">
      <div class="lux-search">
        <input type="text" placeholder="Search rooms, staff, or guests...">
      </div>
      <div>
        <!-- Chỗ cho icon thông báo/cài đặt -->
      </div>
    </header>

    <!-- Page Content -->
    <div class="lux-content">
      <h1 class="lux-header-title">Hàng đợi yêu cầu dịch vụ</h1>
      <p class="lux-header-sub">Assign and monitor daily service operations.</p>

      <!-- Vùng Filter (Giữ nguyên form và logic của bạn) -->
      <form method="get" action="${pageContext.request.contextPath}/staff/service-requests" class="lux-filter-bar">
        <span style="font-weight: 500; color: #555;">Trạng thái:</span>
        <select name="status">
          <option value="">-- Tất cả trạng thái --</option>
          <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>PENDING</option>
          <option value="ASSIGNED" ${statusFilter == 'ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
          <option value="IN_PROGRESS" ${statusFilter == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
          <option value="COMPLETED" ${statusFilter == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
        </select>
        <button class="lux-btn lux-btn-primary" type="submit">Lọc dữ liệu</button>
      </form>

      <!-- Bảng Dữ Liệu -->
      <div class="lux-card">
        <table class="lux-table">
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
              <td style="font-weight: 600;">${s.serviceRequestId}</td>
              <td>${s.bookingCode}</td>
              <td>${s.serviceName}</td>
              <td>${s.quantity} ${s.unitName}</td>
              <td><strong style="color: #0b1b42;"><fmt:formatNumber value="${s.totalAmount}"/> đ</strong></td>
              <td>${s.scheduledAt}</td>
              <td>
                <!-- Áp dụng CSS badge tùy theo status -->
                <span class="lux-badge ${s.statusCode.toLowerCase()}">${s.statusCode}</span>
              </td>
              <td>
                <c:choose>
                  <c:when test="${empty s.staffName}"><em style="color:#999;">Chưa gán</em></c:when>
                  <c:otherwise>${s.staffName}</c:otherwise>
                </c:choose>
              </td>
              <td style="text-align: right;">
                <!-- GIỮ NGUYÊN 100% LOGIC VÀ ACTION FORM CỦA BẠN DƯỚI NÀY -->
                <div style="display: flex; gap: 8px; justify-content: flex-end; align-items: center;">
                  <c:if test="${s.statusCode == 'PENDING'}">
                    <form method="post" style="display:inline-flex; gap:6px; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests">
                      <input type="hidden" name="id" value="${s.serviceRequestId}">
                      <input type="hidden" name="action" value="assign">
                      <input type="number" name="staffUserId" placeholder="ID NV (trống=tôi)" class="lux-input-small">
                      <button class="lux-btn lux-btn-primary" type="submit">Nhận việc</button>
                    </form>
                  </c:if>

                  <c:if test="${s.statusCode == 'ASSIGNED'}">
                    <form method="post" style="display:inline; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests">
                      <input type="hidden" name="id" value="${s.serviceRequestId}">
                      <input type="hidden" name="action" value="start">
                      <button class="lux-btn lux-btn-primary" style="background-color: #3b82f6;" type="submit">Bắt đầu</button>
                    </form>
                  </c:if>

                  <c:if test="${s.statusCode == 'ASSIGNED' || s.statusCode == 'IN_PROGRESS'}">
                    <form method="post" style="display:inline; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests">
                      <input type="hidden" name="id" value="${s.serviceRequestId}">
                      <input type="hidden" name="action" value="complete">
                      <button class="lux-btn lux-btn-success" type="submit">Hoàn tất</button>
                    </form>
                  </c:if>

                  <c:if test="${s.statusCode != 'COMPLETED' && s.statusCode != 'CANCELLED'}">
                    <form method="post" style="display:inline; margin:0;" action="${pageContext.request.contextPath}/staff/service-requests" onsubmit="return confirm('Hủy yêu cầu này?')">
                      <input type="hidden" name="id" value="${s.serviceRequestId}">
                      <input type="hidden" name="action" value="cancel">
                      <button class="lux-btn lux-btn-danger" type="submit">Hủy</button>
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
  </main>
</div>

<%@ include file="_footer.jspf" %>