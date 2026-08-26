<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>📋 Yêu cầu dịch vụ</h1>
    <p><c:out value="${isDispatcher ? 'Phân công và theo dõi yêu cầu của khách' : 'Nhận yêu cầu mới từ hàng đợi chung và xử lý công việc của bạn'}"/></p>
  </div>
</div>

<form method="get" action="${pageContext.request.contextPath}${taskUrl}" class="card"
      style="display:flex;gap:8px;align-items:center">
  <label>Trạng thái</label>
  <select name="status">
    <option value="">Tất cả</option>
    <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>${isDispatcher ? 'Chờ phân công' : 'Chờ nhận việc'}</option>
    <option value="ASSIGNED" ${statusFilter == 'ASSIGNED' ? 'selected' : ''}>Đã phân công</option>
    <option value="IN_PROGRESS" ${statusFilter == 'IN_PROGRESS' ? 'selected' : ''}>Đang thực hiện</option>
    <option value="COMPLETED" ${statusFilter == 'COMPLETED' ? 'selected' : ''}>Hoàn tất</option>
    <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
  </select>
  <button class="btn" type="submit">Lọc</button>
</form>

<div class="card table-wrap">
  <table>
    <thead><tr>
      <th>#</th><th>Đơn lưu trú</th><th>Dịch vụ</th><th>Thời gian mong muốn</th>
      <th>Phí</th><th>Trạng thái</th><th>Nhân viên</th><th>Ghi chú</th><th>Thao tác</th>
    </tr></thead>
    <tbody>
    <c:forEach var="s" items="${requests}">
      <tr>
        <td>#${s.serviceRequestId}</td>
        <td>${s.bookingCode}</td>
        <td><c:out value="${s.serviceName}"/> — ${s.quantity} <c:out value="${s.unitName}"/></td>
        <td>${s.requestedForAt}</td>
        <td><fmt:formatNumber value="${s.totalAmount}"/> đ</td>
        <td><span class="badge">${s.statusCode}</span></td>
        <td><c:out value="${empty s.staffName ? 'Chưa phân công' : s.staffName}"/></td>
        <td style="max-width:240px;white-space:pre-wrap"><c:out value="${s.notes}"/></td>
        <td>
          <c:if test="${!isDispatcher && s.statusCode == 'PENDING'}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="claim">
              <button class="btn btn-gold" type="submit">Nhận việc</button>
            </form>
          </c:if>
          <c:if test="${isDispatcher && (s.statusCode == 'PENDING' || s.statusCode == 'ASSIGNED')}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="assign">
              <select name="staffUserId" required>
                <option value="">Chọn nhân viên</option>
                <c:forEach var="staff" items="${staffList}">
                  <option value="${staff.userId}"><c:out value="${staff.fullName}"/> (${staff.activeTaskCount} việc)</option>
                </c:forEach>
              </select>
              <button class="btn" type="submit">${s.statusCode == 'ASSIGNED' ? 'Phân công lại' : 'Phân công'}</button>
            </form>
          </c:if>
          <c:if test="${isDispatcher && s.statusCode == 'PENDING'}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="assignAuto">
              <button class="btn btn-muted" type="submit">Tự động gán</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="reschedule">
              <input type="datetime-local" name="requestedForAt" required>
              <input name="note" maxlength="300" placeholder="Ghi chú đổi lịch">
              <button class="btn btn-muted" type="submit">Đổi thời gian</button>
            </form>
          </c:if>
          <c:if test="${isDispatcher && s.statusCode != 'COMPLETED' && s.statusCode != 'CANCELLED'}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="cancel">
              <input name="note" maxlength="400" placeholder="Lý do hủy">
              <button class="btn btn-danger" type="submit">Hủy</button>
            </form>
          </c:if>


          <c:if test="${!isDispatcher && s.statusCode == 'ASSIGNED'}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="start">
              <button class="btn" type="submit">Bắt đầu</button>
            </form>
          </c:if>
          <c:if test="${!isDispatcher && s.statusCode == 'IN_PROGRESS'}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="complete">
              <button class="btn btn-success" type="submit">Hoàn tất</button>
            </form>
          </c:if>
          <c:if test="${!isDispatcher && (s.statusCode == 'ASSIGNED' || s.statusCode == 'IN_PROGRESS')}">
            <form method="post" action="${pageContext.request.contextPath}${taskUrl}">
              <input type="hidden" name="id" value="${s.serviceRequestId}">
              <input type="hidden" name="action" value="unable">
              <input name="reason" maxlength="400" required placeholder="Lý do không thể thực hiện">
              <button class="btn btn-danger" type="submit">Không thể thực hiện</button>
            </form>
          </c:if>

        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
  <c:if test="${empty requests}"><p class="muted">Không có yêu cầu phù hợp.</p></c:if>
</div>

<%@ include file="_footer.jspf" %>
