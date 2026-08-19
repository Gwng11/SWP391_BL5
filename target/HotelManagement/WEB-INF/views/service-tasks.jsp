<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Hàng đợi yêu cầu dịch vụ</h2>
  <form method="get" action="${pageContext.request.contextPath}/staff/service-requests">
    <select name="status">
      <option value="">-- tất cả --</option>
      <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>PENDING</option>
      <option value="ASSIGNED" ${statusFilter == 'ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
      <option value="IN_PROGRESS" ${statusFilter == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
      <option value="COMPLETED" ${statusFilter == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
    </select>
    <button class="btn" type="submit">Lọc</button>
  </form>
  <table style="margin-top:12px">
    <tr><th>#</th><th>Đơn</th><th>Dịch vụ</th><th>SL</th><th>Tiền</th><th>Hẹn lúc</th><th>Trạng thái</th><th>NV phụ trách</th><th>Thao tác</th></tr>
    <c:forEach var="s" items="${requests}">
      <tr>
        <td>${s.serviceRequestId}</td><td>${s.bookingCode}</td><td>${s.serviceName}</td>
        <td>${s.quantity} ${s.unitName}</td><td><fmt:formatNumber value="${s.totalAmount}"/> đ</td>
        <td>${s.scheduledAt}</td><td><span class="badge">${s.statusCode}</span></td><td>${s.staffName}</td>
        <td>
          <c:if test="${s.statusCode == 'PENDING'}">
            <form method="post" style="display:inline-flex;gap:4px" action="${pageContext.request.contextPath}/staff/service-requests">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="assign">
              <input type="number" name="staffUserId" placeholder="ID NV (trống = tôi)" style="width:130px">
              <button class="btn" type="submit">Phân công</button>
            </form>
          </c:if>
          <c:if test="${s.statusCode == 'ASSIGNED'}">
            <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="start">
              <button class="btn" type="submit">Bắt đầu</button>
            </form>
          </c:if>
          <c:if test="${s.statusCode == 'ASSIGNED' || s.statusCode == 'IN_PROGRESS'}">
            <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="complete">
              <button class="btn btn-success" type="submit">Hoàn tất</button>
            </form>
          </c:if>
          <c:if test="${s.statusCode != 'COMPLETED' && s.statusCode != 'CANCELLED'}">
            <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests"
                  onsubmit="return confirm('Hủy yêu cầu này?')">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="cancel">
              <button class="btn btn-danger" type="submit">Hủy</button>
            </form>
          </c:if>
        </td>
      </tr>
    </c:forEach>
  </table>
</div>
<%@ include file="_footer.jspf" %>
