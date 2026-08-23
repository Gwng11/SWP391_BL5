<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<div class="card" style="max-width:760px;margin:auto">
  <c:url var="backUrl" value="/services">
    <c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST' && not empty currentStay}">
      <c:param name="reservationId" value="${currentStay.reservationId}"/>
    </c:if>
  </c:url>
  <a href="${backUrl}">← Danh sách dịch vụ</a>
  <h2><c:out value="${service.serviceName}"/></h2>
  <c:if test="${not empty service.imageUrl}">
    <img src="${service.imageUrl}" alt="${service.serviceName}"
         style="width:100%;max-height:320px;object-fit:cover;border-radius:8px">
  </c:if>
  <p><c:out value="${service.description}"/></p>
  <p><b><fmt:formatNumber value="${service.unitPrice}"/> đ / <c:out value="${service.unitName}"/></b></p>

  <c:choose>
    <c:when test="${not empty currentStay}">
      <p>Kỳ lưu trú: <b>${currentStay.bookingCode}</b> — <c:out value="${currentStay.customerName}"/></p>
      <form method="post" action="${pageContext.request.contextPath}/service-detail">
        <input type="hidden" name="hotelServiceId" value="${service.hotelServiceId}">
        <c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST'}">
          <input type="hidden" name="reservationId" value="${currentStay.reservationId}">
        </c:if>
        <label>Số lượng (<c:out value="${service.unitName}"/>)</label>
        <input type="number" name="quantity" value="1" min="0.01" step="0.01" required>
        <label>Thời gian mong muốn</label>
        <input type="datetime-local" name="requestedForAt" required>
        <label>Ghi chú thêm (không bắt buộc)</label>
        <textarea name="notes" rows="4" maxlength="500"
                  placeholder="Ví dụ: giao đến phòng trước 20:00"></textarea>
        <button class="btn btn-success" type="submit">Gửi yêu cầu dịch vụ</button>
      </form>
    </c:when>
    <c:otherwise>
      <div class="err"><c:out value="${stayError}"/></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="_footer.jspf" %>
