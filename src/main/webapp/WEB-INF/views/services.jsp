<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🛎️ Dịch vụ khách sạn</h1>
    <p>Chọn dịch vụ phù hợp cho kỳ lưu trú hiện tại.</p>
  </div>
</div>

<c:if test="${param.ok == '1'}"><div class="msg">Đã gửi yêu cầu dịch vụ thành công.</div></c:if>
<c:if test="${not empty stayError}"><div class="err"><c:out value="${stayError}"/></div></c:if>
<c:if test="${not empty currentStay}">
  <div class="card">
    Kỳ lưu trú hiện tại: <b>${currentStay.bookingCode}</b> —
    <c:out value="${currentStay.customerName}"/>
  </div>
</c:if>

<form method="get" action="${pageContext.request.contextPath}/services" class="card"
      style="display:flex;gap:8px;align-items:center">
  <c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST' && not empty currentStay}">
    <input type="hidden" name="reservationId" value="${currentStay.reservationId}">
  </c:if>
  <input name="q" value="${keyword}" placeholder="Tìm theo tên dịch vụ" style="flex:1">
  <button class="btn" type="submit">Tìm kiếm</button>
</form>

<div class="grid">
  <c:forEach var="s" items="${catalog}">
    <div class="card">
      <c:if test="${not empty s.imageUrl}">
        <img src="${s.imageUrl}" alt="${s.serviceName}"
             style="width:100%;height:180px;object-fit:cover;border-radius:8px">
      </c:if>
      <h3><c:out value="${s.serviceName}"/></h3>
      <p><c:out value="${s.description}"/></p>
      <p><b><fmt:formatNumber value="${s.unitPrice}"/> đ / <c:out value="${s.unitName}"/></b></p>
      <c:url var="detailUrl" value="/service-detail">
        <c:param name="id" value="${s.hotelServiceId}"/>
        <c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST' && not empty currentStay}">
          <c:param name="reservationId" value="${currentStay.reservationId}"/>
        </c:if>
      </c:url>
      <c:choose>
        <c:when test="${not empty currentStay}">
          <a class="btn" href="${detailUrl}">Chọn dịch vụ</a>
        </c:when>
        <c:otherwise>
          <span class="muted">Cần có kỳ lưu trú đang hoạt động để yêu cầu.</span>
        </c:otherwise>
      </c:choose>
    </div>
  </c:forEach>
</div>

<c:if test="${empty catalog}"><div class="card muted">Không tìm thấy dịch vụ đang hoạt động.</div></c:if>
<%@ include file="_footer.jspf" %>
