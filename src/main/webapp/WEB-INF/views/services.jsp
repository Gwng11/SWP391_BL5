<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
  .service-shop { --svc-blue:#0757c8;--svc-ink:#152c4a;--svc-soft:#f1f7ff; }
  .service-shop .service-hero { position:relative;overflow:hidden;padding:34px 38px;border:1px solid #cfe1f8;border-radius:22px;background:linear-gradient(118deg,#f6faff,#e7f2ff); }
  .service-shop .service-hero:before,.service-shop .service-hero:after { content:"";position:absolute;border:2px solid rgba(7,87,200,.08);border-radius:50%; }
  .service-shop .service-hero:before { width:260px;height:260px;right:-20px;top:-130px; }
  .service-shop .service-hero:after { width:160px;height:160px;right:130px;bottom:-110px; }
  .service-shop .service-hero-content { position:relative;z-index:1;max-width:780px; }
  .service-shop .service-kicker { color:var(--svc-blue);font-size:12px;font-weight:800;letter-spacing:.08em;text-transform:uppercase; }
  .service-shop .service-hero h1 { margin:8px 0 7px;color:var(--svc-blue);font-size:32px; }
  .service-shop .service-hero p { margin:0;color:#52647a;font-size:15px;line-height:1.55; }
  .service-shop .stay-context { position:relative;z-index:1;display:inline-flex;align-items:center;gap:9px;margin-top:18px;padding:9px 13px;border-radius:999px;background:#fff;color:#2b405d;font-size:12px;box-shadow:0 4px 14px rgba(41,78,122,.09); }
  .service-shop .stay-dot { width:9px;height:9px;border-radius:50%;background:#19a46b;box-shadow:0 0 0 4px rgba(25,164,107,.12); }
  .service-shop .service-toolbar { display:flex;justify-content:space-between;align-items:center;gap:18px;margin:30px 0 20px; }
  .service-shop .service-toolbar h2 { margin:0;color:var(--svc-ink);font-size:25px; }
  .service-shop .service-toolbar p { margin:5px 0 0;color:#718096;font-size:13px; }
  .service-shop .service-search { display:flex;align-items:center;gap:8px;min-width:340px;padding:6px;border:1px solid #d7e2ef;border-radius:999px;background:#fff;box-shadow:0 5px 16px rgba(29,55,88,.07); }
  .service-shop .service-search input { flex:1;min-width:0;border:0;background:transparent;box-shadow:none;padding-left:12px; }
  .service-shop .service-search input:focus { outline:0;box-shadow:none; }
  .service-shop .service-search .btn { border-radius:999px;padding:9px 18px; }
  .service-shop .service-grid { display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:20px; }
  .service-shop .service-card { overflow:hidden;display:flex;flex-direction:column;min-width:0;border:1px solid #dce5ef;border-radius:17px;background:#fff;box-shadow:0 7px 25px rgba(35,57,84,.08);transition:transform .2s,box-shadow .2s; }
  .service-shop .service-card:hover { transform:translateY(-4px);box-shadow:0 16px 34px rgba(35,57,84,.14); }
  .service-shop .service-cover { position:relative;height:210px;overflow:hidden;background:linear-gradient(140deg,#0a5fc3,#67b9ed); }
  .service-shop .service-cover img { width:100%;height:100%;object-fit:cover;transition:transform .35s; }
  .service-shop .service-card:hover img { transform:scale(1.035); }
  .service-shop .service-cover:after { content:"";position:absolute;inset:auto 0 0;height:42%;background:linear-gradient(0deg,rgba(8,28,53,.45),transparent);pointer-events:none; }
  .service-shop .cover-placeholder { height:100%;display:grid;place-items:center;color:rgba(255,255,255,.92);font-size:68px; }
  .service-shop .available-tag { position:absolute;z-index:2;top:13px;left:13px;padding:7px 11px;border-radius:999px;background:#fff;color:#087443;font-size:11px;font-weight:800;box-shadow:0 4px 12px rgba(0,0,0,.14); }
  .service-shop .service-card-body { display:flex;flex:1;flex-direction:column;padding:17px; }
  .service-shop .service-card h3 { margin:0 0 7px;color:var(--svc-ink);font-size:18px; }
  .service-shop .service-description { min-height:42px;margin:0;color:#66758a;font-size:13px;line-height:1.55;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
  .service-shop .service-footer { display:flex;align-items:end;justify-content:space-between;gap:12px;margin-top:auto;padding-top:17px; }
  .service-shop .price-from { color:#7a8798;font-size:11px; }
  .service-shop .service-price { color:var(--svc-blue);font-size:20px;font-weight:800;white-space:nowrap; }
  .service-shop .service-price small { color:#68778b;font-size:11px;font-weight:500; }
  .service-shop .service-footer .btn { flex:none;border-radius:10px;padding:9px 14px; }
  .service-shop .unavailable-note { max-width:145px;color:#7a8798;font-size:11px;line-height:1.35;text-align:right; }
  .service-shop .catalog-empty { padding:50px 20px;text-align:center;border:1px dashed #bdd0e5;border-radius:16px;background:#f8fbff;color:#68778a; }
  @media(max-width:980px){.service-shop .service-grid{grid-template-columns:repeat(2,1fr)}}
  @media(max-width:700px){.service-shop .service-hero{padding:27px 23px}.service-shop .service-hero h1{font-size:27px}.service-shop .service-toolbar{align-items:stretch;flex-direction:column}.service-shop .service-search{min-width:0;width:100%}.service-shop .service-grid{grid-template-columns:1fr}}
</style>

<div class="service-shop">
  <section class="service-hero">
    <div class="service-hero-content">
      <div class="service-kicker">Dịch vụ dành riêng cho kỳ lưu trú</div>
      <h1>Thêm trải nghiệm, tận hưởng trọn vẹn</h1>
      <p>Từ ẩm thực, giặt ủi đến đưa đón và chăm sóc cá nhân — chọn dịch vụ phù hợp, thời gian mong muốn và gửi yêu cầu chỉ trong vài bước.</p>
      <c:if test="${not empty currentStay}">
        <div class="stay-context"><span class="stay-dot"></span><span>Kỳ lưu trú <b><c:out value="${currentStay.bookingCode}"/></b> · <c:out value="${currentStay.customerName}"/></span></div>
      </c:if>
    </div>
  </section>

  <c:if test="${param.ok == '1'}"><div class="msg" style="margin-top:18px">Đã gửi yêu cầu dịch vụ thành công.</div></c:if>
  <c:if test="${not empty stayError}"><div class="err" style="margin-top:18px"><c:out value="${stayError}"/></div></c:if>

  <div class="service-toolbar">
    <div><h2>Dịch vụ nổi bật</h2><p>Chỉ hiển thị các dịch vụ đang hoạt động và có thể yêu cầu.</p></div>
    <form method="get" action="${pageContext.request.contextPath}/services" class="service-search">
      <c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST' && not empty currentStay}"><input type="hidden" name="reservationId" value="${currentStay.reservationId}"></c:if>
      <input name="q" value="<c:out value='${keyword}'/>" placeholder="Tìm theo tên dịch vụ" aria-label="Tìm dịch vụ">
      <button class="btn" type="submit">Tìm kiếm</button>
    </form>
  </div>

  <c:choose>
    <c:when test="${empty catalog}"><div class="catalog-empty">Không tìm thấy dịch vụ đang hoạt động phù hợp.</div></c:when>
    <c:otherwise>
      <div class="service-grid">
        <c:forEach var="s" items="${catalog}">
          <article class="service-card">
            <div class="service-cover">
              <span class="available-tag">● Đang phục vụ</span>
              <c:choose><c:when test="${not empty s.imageUrl}"><img src="<c:out value='${s.imageUrl}'/>" alt="<c:out value='${s.serviceName}'/>" loading="lazy"></c:when><c:otherwise><div class="cover-placeholder">🛎️</div></c:otherwise></c:choose>
            </div>
            <div class="service-card-body">
              <h3><c:out value="${s.serviceName}"/></h3>
              <p class="service-description"><c:out value="${empty s.description ? 'Dịch vụ tiện ích dành cho khách đang lưu trú tại khách sạn.' : s.description}"/></p>
              <c:url var="detailUrl" value="/service-detail"><c:param name="id" value="${s.hotelServiceId}"/><c:if test="${sessionScope.currentUser.roleCode == 'RECEPTIONIST' && not empty currentStay}"><c:param name="reservationId" value="${currentStay.reservationId}"/></c:if></c:url>
              <div class="service-footer">
                <div><div class="price-from">Giá dịch vụ</div><div class="service-price"><fmt:formatNumber value="${s.unitPrice}"/> đ <small>/ <c:out value="${s.unitName}"/></small></div></div>
                <c:choose><c:when test="${not empty currentStay}"><a class="btn" href="${detailUrl}">Chọn dịch vụ →</a></c:when><c:otherwise><span class="unavailable-note">Cần có kỳ lưu trú đang hoạt động để yêu cầu.</span></c:otherwise></c:choose>
              </div>
            </div>
          </article>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="_footer.jspf" %>
