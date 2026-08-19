<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="page-head">
  <div><h1>Manager Dashboard</h1><div class="muted">Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/></div></div>
  <a class="btn btn-muted" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
</div>
<c:choose>
  <c:when test="${not empty dashboardError}">
    <div class="err" role="alert"><c:out value="${dashboardError}"/> <a href="${pageContext.request.contextPath}/manager/dashboard">Thử lại</a></div>
  </c:when>
  <c:otherwise>
    <div class="grid" aria-label="Chỉ số vận hành hiện tại">
      <div class="card kpi col-4"><div class="label">Room occupancy</div><div class="value">${dashboard.occupiedRooms} / ${dashboard.operationalRooms}</div><div class="muted">phòng đang ở / phòng vận hành</div></div>
      <div class="card kpi col-4"><div class="label">Arrivals hôm nay</div><div class="value">${dashboard.arrivals}</div></div>
      <div class="card kpi col-4"><div class="label">Departures hôm nay</div><div class="value">${dashboard.departures}</div></div>
      <div class="card kpi col-4"><div class="label">Đặt phòng mới</div><div class="value">${dashboard.newReservations}</div></div>
      <div class="card kpi col-4"><div class="label">Doanh thu đã thu hôm nay</div><div class="value"><fmt:formatNumber value="${dashboard.revenue}" pattern="#,##0"/> đ</div></div>
      <div class="card kpi col-4"><div class="label">Việc vận hành chờ xử lý</div><div class="value">${dashboard.pendingHousekeepingTasks + dashboard.unresolvedMaintenanceIssues}</div><div class="muted">${dashboard.pendingHousekeepingTasks} housekeeping · ${dashboard.unresolvedMaintenanceIssues} maintenance</div></div>
    </div>
  </c:otherwise>
</c:choose>
<h2>Chức năng Manager</h2>
<div class="grid">
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/manager/rooms"><h3>Phòng vật lý</h3><span>Room number, tầng và trạng thái vận hành</span></a>
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/manager/room-types"><h3>Loại phòng & Amenities</h3><span>Catalogue, ảnh và sales status</span></a>
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/manager/pricing"><h3>Room Pricing</h3><span>Base price, giá theo ngày và stop-sell</span></a>
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/manager/housekeeping"><h3>Housekeeping Tasks</h3><span>Tạo, phân công và theo dõi cleaning</span></a>
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/manager/maintenance"><h3>Maintenance Issues</h3><span>Ưu tiên, giao việc, review và đóng issue</span></a>
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/manager/reports"><h3>Reports & Statistics</h3><span>Báo cáo theo kỳ từ dữ liệu thật</span></a>
  <a class="card nav-card col-4" href="${pageContext.request.contextPath}/profile"><h3>Personal Profile</h3><span>Xem và cập nhật hồ sơ cá nhân</span></a>
</div>
<%@ include file="_footer.jspf" %>
