<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
  .service-order { --order-blue:#0757c8;--order-ink:#152c4a; }
  .service-order .back-link { display:inline-flex;align-items:center;gap:7px;margin-bottom:18px;color:#52647a;font-size:13px;font-weight:700;text-decoration:none; }
  .service-order .back-link:hover { color:var(--order-blue); }
  .service-order .order-layout { display:grid;grid-template-columns:minmax(0,1.2fr) minmax(330px,.8fr);gap:24px;align-items:start; }
  .service-order .showcase,.service-order .request-panel { overflow:hidden;border:1px solid #dce6f0;border-radius:19px;background:#fff;box-shadow:0 8px 28px rgba(35,57,84,.09); }
  .service-order .showcase-image { position:relative;height:350px;background:linear-gradient(140deg,#0757c8,#65b9ef); }
  .service-order .showcase-image img { width:100%;height:100%;object-fit:cover; }
  .service-order .showcase-image:after { content:"";position:absolute;inset:auto 0 0;height:45%;background:linear-gradient(0deg,rgba(5,27,55,.55),transparent); }
  .service-order .image-placeholder { height:100%;display:grid;place-items:center;color:#fff;font-size:86px; }
  .service-order .available-tag { position:absolute;z-index:2;top:16px;left:16px;padding:7px 12px;border-radius:999px;background:#fff;color:#087443;font-size:11px;font-weight:800;box-shadow:0 5px 14px rgba(0,0,0,.16); }
  .service-order .showcase-content { padding:24px; }
  .service-order .showcase-content h1 { margin:0 0 10px;color:var(--order-ink);font-size:28px; }
  .service-order .showcase-content p { margin:0;color:#637287;line-height:1.65; }
  .service-order .feature-row { display:flex;flex-wrap:wrap;gap:8px;margin-top:20px; }
  .service-order .feature-pill { padding:7px 10px;border-radius:999px;background:#f1f7ff;color:#355371;font-size:11px;font-weight:700; }
  .service-order .request-panel { padding:23px;position:sticky;top:18px; }
  .service-order .panel-kicker { color:var(--order-blue);font-size:11px;font-weight:800;letter-spacing:.08em;text-transform:uppercase; }
  .service-order .request-panel h2 { margin:7px 0 4px;color:var(--order-ink);font-size:21px; }
  .service-order .service-price { margin:9px 0 17px;color:var(--order-blue);font-size:25px;font-weight:800; }
  .service-order .service-price small { color:#748298;font-size:12px;font-weight:500; }
  .service-order .stay-box { display:flex;gap:10px;margin-bottom:18px;padding:11px;border-radius:11px;background:#f4f8fc;color:#4a5d74;font-size:12px;line-height:1.45; }
  .service-order .stay-icon { flex:none;width:32px;height:32px;display:grid;place-items:center;border-radius:9px;background:#dcecff; }
  .service-order .request-panel label { margin:14px 0 6px;color:#42556d;font-size:12px;font-weight:700; }
  .service-order .request-panel input,.service-order .request-panel textarea { width:100%;background:#fafbfd; }
  .service-order .request-panel textarea { resize:vertical; }
  .service-order .submit-service { width:100%;margin-top:18px;padding:12px;border-radius:11px;font-size:14px; }
  @media(max-width:820px){.service-order .order-layout{grid-template-columns:1fr}.service-order .request-panel{position:static}.service-order .showcase-image{height:270px}}
</style>

<div class="service-order">
  <c:url var="backUrl" value="/services"><c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST' && not empty currentStay}"><c:param name="reservationId" value="${currentStay.reservationId}"/></c:if></c:url>
  <a class="back-link" href="${backUrl}">← Quay lại danh sách dịch vụ</a>

  <div class="order-layout">
    <section class="showcase">
      <div class="showcase-image">
        <span class="available-tag">● Đang phục vụ</span>
        <c:choose><c:when test="${not empty service.imageUrl}"><img src="<c:out value='${service.imageUrl}'/>" alt="<c:out value='${service.serviceName}'/>"></c:when><c:otherwise><div class="image-placeholder">🛎️</div></c:otherwise></c:choose>
      </div>
      <div class="showcase-content">
        <h1><c:out value="${service.serviceName}"/></h1>
        <p><c:out value="${empty service.description ? 'Dịch vụ tiện ích dành cho khách đang lưu trú tại khách sạn.' : service.description}"/></p>
        <div class="feature-row"><span class="feature-pill">✓ Dịch vụ chính thức của khách sạn</span><span class="feature-pill">⏱ Chọn thời gian mong muốn</span><span class="feature-pill">✎ Có thể thêm ghi chú</span></div>
      </div>
    </section>

    <aside class="request-panel">
      <div class="panel-kicker">Đặt dịch vụ</div>
      <h2>Thông tin yêu cầu</h2>
      <div class="service-price"><fmt:formatNumber value="${service.unitPrice}"/> đ <small>/ <c:out value="${service.unitName}"/></small></div>
      <c:choose>
        <c:when test="${not empty currentStay}">
          <div class="stay-box"><span class="stay-icon">🏨</span><span>Kỳ lưu trú <b><c:out value="${currentStay.bookingCode}"/></b><br><c:out value="${currentStay.customerName}"/></span></div>
          <form method="post" action="${pageContext.request.contextPath}/service-detail">
            <input type="hidden" name="hotelServiceId" value="${service.hotelServiceId}">
            <c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST'}"><input type="hidden" name="reservationId" value="${currentStay.reservationId}"></c:if>
            <label>Số lượng (<c:out value="${service.unitName}"/>)</label>
            <input type="number" name="quantity" value="1" min="0.01" step="0.01" required>
            <label>Thời gian mong muốn</label>
            <input type="datetime-local" name="requestedForAt" required>
            <label>Ghi chú thêm (không bắt buộc)</label>
            <textarea name="notes" rows="4" maxlength="500" placeholder="Ví dụ: giao đến phòng trước 20:00"></textarea>
            <button class="btn btn-success submit-service" type="submit">Gửi yêu cầu dịch vụ →</button>
          </form>
        </c:when>
        <c:otherwise><div class="err"><c:out value="${stayError}"/></div></c:otherwise>
      </c:choose>
    </aside>
  </div>
</div>

<%@ include file="_footer.jspf" %>
