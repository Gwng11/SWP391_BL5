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
            <c:choose>
              <c:when test="${isDispatcher}">
                <form method="post" style="display:inline-flex;gap:4px" action="${pageContext.request.contextPath}/staff/service-requests">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="assign">
                  <select name="staffUserId" required>
                    <option value="">-- chọn nhân viên --</option>
                    <c:forEach var="st" items="${staffList}">
                      <option value="${st.userId}">${st.fullName} — ${st.activeTaskCount} việc đang làm</option>
                    </c:forEach>
                  </select>
                  <button class="btn" type="submit">Gán</button>
                </form>
                <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="assignAuto">
                  <button class="btn" type="submit">Gán tự động</button>
                </form>
              </c:when>
              <c:otherwise>
                <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests">
                  <input type="hidden" name="id" value="${s.serviceRequestId}">
                  <input type="hidden" name="action" value="claim">
                  <button class="btn" type="submit">Tự nhận việc</button>
                </form>
              </c:otherwise>
            </c:choose>
          </c:if>

          <c:if test="${s.statusCode == 'ASSIGNED' && (isDispatcher || s.assignedStaffUserId == sessionScope.currentUser.userId)}">
            <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="start">
              <button class="btn" type="submit">Bắt đầu</button>
            </form>
          </c:if>
          <c:if test="${(s.statusCode == 'ASSIGNED' || s.statusCode == 'IN_PROGRESS') && (isDispatcher || s.assignedStaffUserId == sessionScope.currentUser.userId)}">
            <form method="post" style="display:inline" action="${pageContext.request.contextPath}/staff/service-requests">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="complete">
              <button class="btn btn-success" type="submit">Hoàn tất</button>
            </form>
          </c:if>
          <c:if test="${isDispatcher && s.statusCode != 'COMPLETED' && s.statusCode != 'CANCELLED'}">
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
