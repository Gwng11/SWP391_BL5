<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="page-head"><h1>Reports & Statistics</h1><a class="btn" href="${pageContext.request.contextPath}/manager/dashboard">Dashboard</a></div>
<div class="card"><form method="get" class="inline"><label>Từ ngày</label><input type="date" name="from" required value="${from}"><label>Đến ngày</label><input type="date" name="to" required value="${to}"><button class="btn">Tạo báo cáo</button></form></div>
<c:if test="${not empty reportError}"><div class="err" role="alert"><c:out value="${reportError}"/> <a href="?from=${from}&to=${to}">Thử lại</a></div></c:if>
<c:if test="${not empty report}"><div class="grid">
  <div class="card kpi col-4"><div class="label">Occupancy report</div><div class="value">${report.bookedRoomNights}</div><div class="muted">booked room-nights trong kỳ</div></div>
  <div class="card kpi col-4"><div class="label">Reservation report</div><div class="value">${report.reservations}</div><div class="muted">reservation tạo trong kỳ</div></div>
  <div class="card kpi col-4"><div class="label">Cancellation report</div><div class="value">${report.cancellations}</div><div class="muted">reservation bị hủy</div></div>
  <div class="card kpi col-6"><div class="label">Revenue report</div><div class="value"><fmt:formatNumber value="${report.reservationRevenue}" pattern="#,##0"/> đ</div><div class="muted">giá trị reservation không hủy được tạo trong kỳ</div></div>
  <div class="card kpi col-6"><div class="label">Payment report</div><div class="value"><fmt:formatNumber value="${report.successfulPayments}" pattern="#,##0"/> đ</div><div class="muted">${report.paymentTransactions} giao dịch SUCCESS</div></div>
  <div class="card kpi col-12"><div class="label">Service performance report</div><div class="value">${report.completedServices} / ${report.serviceRequests}</div><div class="muted">service request hoàn tất / tổng request trong kỳ</div></div>
</div><c:if test="${report.reservations == 0 && report.paymentTransactions == 0 && report.serviceRequests == 0}"><div class="empty">Không có dữ liệu trong kỳ đã chọn. Hãy chọn khoảng ngày khác.</div></c:if></c:if>
<%@ include file="_footer.jspf" %>
