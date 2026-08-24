<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>

<style>
  .my-services { --ms-blue:#0757c8;--ms-ink:#152c4a;--ms-soft:#eef6ff; }
  .my-services .ms-hero { position:relative;overflow:hidden;display:flex;justify-content:space-between;align-items:center;gap:24px;padding:30px 34px;border-radius:20px;background:linear-gradient(120deg,#063f89,#1479d8);color:#fff;box-shadow:0 14px 34px rgba(7,73,149,.18); }
  .my-services .ms-hero:after { content:"";position:absolute;width:230px;height:230px;right:12%;top:-150px;border:1px solid rgba(255,255,255,.17);border-radius:50%; }
  .my-services .ms-hero-copy,.my-services .hero-action { position:relative;z-index:1; }
  .my-services .ms-kicker { font-size:11px;font-weight:800;letter-spacing:.09em;text-transform:uppercase;opacity:.8; }
  .my-services .ms-hero h1 { margin:8px 0 6px;color:#fff;font-size:29px; }
  .my-services .ms-hero p { margin:0;max-width:660px;color:rgba(255,255,255,.82);font-size:14px; }
  .my-services .hero-action { flex:none;border-radius:11px;background:#fff;color:var(--ms-blue);padding:11px 17px;text-decoration:none;font-weight:800;font-size:13px; }
  .my-services .hero-action.disabled { max-width:240px;background:rgba(255,255,255,.13);color:#fff;font-size:11px;font-weight:600;line-height:1.4;text-align:center;pointer-events:none; }
  .my-services .summary-grid { display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:15px;margin:21px 0; }
  .my-services .summary-card { padding:17px 19px;border:1px solid #dce6f0;border-radius:14px;background:#fff;box-shadow:0 5px 18px rgba(35,57,84,.07); }
  .my-services .summary-label { color:#758399;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.04em; }
  .my-services .summary-value { margin-top:5px;color:var(--ms-ink);font-size:25px;font-weight:800; }
  .my-services .summary-card.amount .summary-value { color:#07814f; }
  .my-services .summary-card small { color:#8290a2;font-size:10px; }
  .my-services .history-panel { padding:22px;border:1px solid #dce6f0;border-radius:17px;background:#fff;box-shadow:0 8px 25px rgba(35,57,84,.08); }
  .my-services .history-head { display:flex;justify-content:space-between;align-items:end;gap:18px;margin-bottom:18px; }
  .my-services .history-head h2 { margin:0;color:var(--ms-ink);font-size:22px; }
  .my-services .history-head p { margin:5px 0 0;color:#78869a;font-size:12px; }
  .my-services .status-filters { display:flex;flex-wrap:wrap;gap:7px; }
  .my-services .filter-pill { padding:7px 11px;border:1px solid #d8e3ef;border-radius:999px;background:#f8fafc;color:#56677c;text-decoration:none;font-size:11px;font-weight:700; }
  .my-services .filter-pill.active { border-color:var(--ms-blue);background:var(--ms-blue);color:#fff; }
  .my-services .request-list { display:grid;gap:12px; }
  .my-services .request-card { display:grid;grid-template-columns:minmax(180px,1.4fr) minmax(145px,.9fr) minmax(125px,.75fr) minmax(125px,.75fr);gap:18px;align-items:center;padding:17px;border:1px solid #e0e8f1;border-radius:13px;background:#fff; }
  .my-services .service-name { color:var(--ms-ink);font-size:16px;font-weight:800; }
  .my-services .request-code { margin-top:4px;color:#7b899b;font-size:11px; }
  .my-services .request-meta { margin-top:8px;color:#596a7f;font-size:12px;line-height:1.45; }
  .my-services .field-label { margin-bottom:4px;color:#8995a5;font-size:10px;font-weight:800;text-transform:uppercase; }
  .my-services .field-value { color:#40536b;font-size:12px;font-weight:650; }
  .my-services .request-price { color:var(--ms-blue);font-size:17px;font-weight:800; }
  .my-services .status-badge { display:inline-flex;padding:6px 10px;border-radius:999px;font-size:10px;font-weight:800; }
  .my-services .status-PENDING { background:#fff4d6;color:#946200; }
  .my-services .status-ASSIGNED { background:#e7f0ff;color:#0757c8; }
  .my-services .status-IN_PROGRESS { background:#eee8ff;color:#6838b8; }
  .my-services .status-COMPLETED { background:#dcf7e9;color:#087443; }
  .my-services .status-CANCELLED { background:#fee7e7;color:#b42318; }
  .my-services .empty-history { padding:44px 20px;text-align:center;border:1px dashed #bfd0e3;border-radius:14px;background:#f8fbff;color:#6f7e91; }
  .my-services .empty-history .btn { margin-top:13px; }
  @media(max-width:950px){.my-services .summary-grid{grid-template-columns:repeat(2,1fr)}.my-services .request-card{grid-template-columns:repeat(2,1fr)}}
  @media(max-width:650px){.my-services .ms-hero,.my-services .history-head{align-items:flex-start;flex-direction:column}.my-services .summary-grid,.my-services .request-card{grid-template-columns:1fr}.my-services .hero-action{width:100%;text-align:center}.my-services .history-panel{padding:16px}}
</style>

<div class="my-services">
  <section class="ms-hero">
    <div class="ms-hero-copy">
      <div class="ms-kicker">Kỳ lưu trú và tiện ích</div>
      <h1>Dịch vụ của tôi</h1>
      <p>Theo dõi tiến độ phục vụ, nhân viên phụ trách và khoản phí của từng yêu cầu.</p>
    </div>
    <c:choose>
      <c:when test="${not empty currentStay}"><a class="hero-action" href="${pageContext.request.contextPath}/services">＋ Đặt thêm dịch vụ</a></c:when>
      <c:otherwise><span class="hero-action disabled"><c:out value="${empty stayError ? 'Cần có kỳ lưu trú đang hoạt động để đặt dịch vụ.' : stayError}"/></span></c:otherwise>
    </c:choose>
  </section>

  <c:if test="${param.created == '1'}"><div class="msg" style="margin-top:18px">✅ Yêu cầu dịch vụ đã được gửi. Lễ tân sẽ tiếp nhận và phân công nhân viên.</div></c:if>

  <div class="summary-grid">
    <div class="summary-card"><div class="summary-label">Tổng yêu cầu</div><div class="summary-value">${totalRequestCount}</div><small>Trên tất cả kỳ lưu trú</small></div>
    <div class="summary-card"><div class="summary-label">Đang xử lý</div><div class="summary-value">${activeRequestCount}</div><small>Chờ nhận, đã phân công hoặc đang làm</small></div>
    <div class="summary-card"><div class="summary-label">Đã hoàn tất</div><div class="summary-value">${completedRequestCount}</div><small>Đã hoàn thành phục vụ</small></div>
    <div class="summary-card amount"><div class="summary-label">Phí đã hoàn tất</div><div class="summary-value"><fmt:formatNumber value="${completedAmount}"/> đ</div><small>Được cộng vào hóa đơn lưu trú</small></div>
  </div>

  <section class="history-panel">
    <div class="history-head">
      <div><h2>Lịch sử yêu cầu</h2><p>Giá được cố định tại thời điểm gửi yêu cầu; yêu cầu đã hủy không bị tính phí.</p></div>
      <div class="status-filters">
        <a class="filter-pill ${empty statusFilter ? 'active' : ''}" href="${pageContext.request.contextPath}/my-service-requests">Tất cả</a>
        <a class="filter-pill ${statusFilter == 'PENDING' ? 'active' : ''}" href="?status=PENDING">Chờ tiếp nhận</a>
        <a class="filter-pill ${statusFilter == 'IN_PROGRESS' ? 'active' : ''}" href="?status=IN_PROGRESS">Đang thực hiện</a>
        <a class="filter-pill ${statusFilter == 'COMPLETED' ? 'active' : ''}" href="?status=COMPLETED">Hoàn tất</a>
        <a class="filter-pill ${statusFilter == 'CANCELLED' ? 'active' : ''}" href="?status=CANCELLED">Đã hủy</a>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty requests}">
        <div class="empty-history">🛎️ Chưa có yêu cầu dịch vụ phù hợp.<br><c:if test="${not empty currentStay}"><a class="btn" href="${pageContext.request.contextPath}/services">Khám phá dịch vụ</a></c:if></div>
      </c:when>
      <c:otherwise>
        <div class="request-list">
          <c:forEach var="s" items="${requests}">
            <article class="request-card">
              <div>
                <div class="service-name"><c:out value="${s.serviceName}"/></div>
                <div class="request-code">Yêu cầu #${s.serviceRequestId} · Đơn <c:out value="${s.bookingCode}"/></div>
                <div class="request-meta">${s.quantity} <c:out value="${s.unitName}"/><c:if test="${not empty s.notes}"><br>Ghi chú: <c:out value="${s.notes}"/></c:if></div>
              </div>
              <div><div class="field-label">Thời gian mong muốn</div><div class="field-value"><c:out value="${s.requestedForAt}"/></div><div class="field-label" style="margin-top:9px">Nhân viên</div><div class="field-value"><c:out value="${empty s.staffName ? 'Chưa phân công' : s.staffName}"/></div></div>
              <div><div class="field-label">Thành tiền</div><div class="request-price"><fmt:formatNumber value="${s.totalAmount}"/> đ</div><div class="field-value" style="margin-top:5px"><fmt:formatNumber value="${s.unitPriceSnapshot}"/> đ / <c:out value="${s.unitName}"/></div></div>
              <div>
                <div class="field-label">Trạng thái</div>
                <c:choose>
                  <c:when test="${s.statusCode == 'PENDING'}"><span class="status-badge status-PENDING">Chờ tiếp nhận</span></c:when>
                  <c:when test="${s.statusCode == 'ASSIGNED'}"><span class="status-badge status-ASSIGNED">Đã phân công</span></c:when>
                  <c:when test="${s.statusCode == 'IN_PROGRESS'}"><span class="status-badge status-IN_PROGRESS">Đang thực hiện</span></c:when>
                  <c:when test="${s.statusCode == 'COMPLETED'}"><span class="status-badge status-COMPLETED">Đã hoàn tất</span></c:when>
                  <c:otherwise><span class="status-badge status-CANCELLED">Đã hủy</span></c:otherwise>
                </c:choose>
                <c:if test="${not empty s.completedAt}"><div class="request-meta">Hoàn tất: <c:out value="${s.completedAt}"/></div></c:if>
              </div>
            </article>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </section>
</div>

<%@ include file="_footer.jspf" %>
