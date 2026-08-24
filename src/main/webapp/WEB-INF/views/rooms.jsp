<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<style>
  .room-discovery { --room-ink:#172b4d; --room-blue:#0756c9; --room-sky:#eef6ff; }
  .room-discovery .room-hero { position:relative; overflow:hidden; padding:38px 42px 92px; border-radius:22px; color:#fff; background:linear-gradient(118deg,#063b85 0%,#0768d8 62%,#2d91ef 100%); box-shadow:0 18px 42px rgba(7,74,153,.18); }
  .room-discovery .room-hero:before,.room-discovery .room-hero:after { content:""; position:absolute; border:1px solid rgba(255,255,255,.16); border-radius:50%; }
  .room-discovery .room-hero:before { width:280px;height:280px;right:-60px;top:-120px; }
  .room-discovery .room-hero:after { width:190px;height:190px;right:95px;top:50px; }
  .room-discovery .hero-copy { position:relative;z-index:1;max-width:720px; }
  .room-discovery .hero-kicker { display:inline-flex;align-items:center;gap:7px;padding:6px 12px;border-radius:999px;background:rgba(255,255,255,.15);font-size:12px;font-weight:700;letter-spacing:.05em;text-transform:uppercase; }
  .room-discovery .room-hero h1 { margin:14px 0 8px;font-size:34px;line-height:1.15;color:#fff; }
  .room-discovery .room-hero p { margin:0;color:rgba(255,255,255,.86);font-size:16px; }
  .room-discovery .room-search { position:relative;z-index:2;margin:-55px 24px 34px;padding:14px;border:1px solid #dce8f6;border-radius:16px;background:#fff;box-shadow:0 15px 36px rgba(25,55,95,.16); }
  .room-discovery .room-search form { display:grid;grid-template-columns:1.15fr 1.15fr .75fr .75fr auto;gap:10px;align-items:end; }
  .room-discovery .search-field { min-width:0;padding:0 6px; }
  .room-discovery .search-field label { display:block;margin:0 0 6px;color:#53657c;font-size:11px;font-weight:800;letter-spacing:.04em;text-transform:uppercase; }
  .room-discovery .search-field input { width:100%;height:44px;border-color:#d8e2ee;background:#f8fafc; }
  .room-discovery .search-submit { height:44px;padding:0 22px;border-radius:10px;white-space:nowrap; }
  .room-discovery .results-head { display:flex;justify-content:space-between;align-items:end;gap:20px;margin:0 0 18px; }
  .room-discovery .results-head h2 { margin:0;color:var(--room-ink);font-size:25px; }
  .room-discovery .results-head p { margin:5px 0 0;color:#65758b; }
  .room-discovery .stay-chip { flex:none;padding:9px 14px;border-radius:999px;background:var(--room-sky);color:var(--room-blue);font-size:13px;font-weight:700; }
  .room-discovery .room-grid { display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:20px; }
  .room-discovery .room-card { overflow:hidden;border:1px solid #dfe7f0;border-radius:16px;background:#fff;box-shadow:0 7px 24px rgba(36,57,85,.08);transition:transform .2s,box-shadow .2s; }
  .room-discovery .room-card:hover { transform:translateY(-4px);box-shadow:0 15px 34px rgba(36,57,85,.14); }
  .room-discovery .room-visual { position:relative;height:190px;padding:18px;display:flex;align-items:flex-end;overflow:hidden;background:linear-gradient(145deg,#0b5bb5,#53a8e8); }
  .room-discovery .room-card:nth-child(3n+2) .room-visual { background:linear-gradient(145deg,#135c72,#45a7a2); }
  .room-discovery .room-card:nth-child(3n+3) .room-visual { background:linear-gradient(145deg,#624418,#d5a94a); }
  .room-discovery .room-visual:before { content:"";position:absolute;inset:0;background:linear-gradient(0deg,rgba(4,20,41,.78),rgba(4,20,41,0) 70%); }
  .room-discovery .room-visual:after { content:"🏨";position:absolute;right:18px;top:14px;font-size:64px;opacity:.18;filter:grayscale(1) brightness(3); }
  .room-discovery .room-title { position:relative;z-index:1;color:#fff; }
  .room-discovery .room-title h3 { margin:0 0 4px;color:#fff;font-size:20px; }
  .room-discovery .room-title span { font-size:12px;color:rgba(255,255,255,.78); }
  .room-discovery .stock-badge { position:absolute;z-index:2;top:14px;left:14px;padding:6px 10px;border-radius:999px;background:#fff;color:#087443;font-size:11px;font-weight:800;box-shadow:0 4px 10px rgba(0,0,0,.12); }
  .room-discovery .room-body { padding:17px; }
  .room-discovery .room-description { min-height:40px;margin:0 0 14px;color:#637187;font-size:13px;line-height:1.55;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
  .room-discovery .room-facts { display:flex;flex-wrap:wrap;gap:7px;margin-bottom:16px; }
  .room-discovery .room-fact { padding:6px 9px;border-radius:8px;background:#f4f7fa;color:#4c5d71;font-size:11px;font-weight:600; }
  .room-discovery .price-row { display:flex;justify-content:space-between;gap:12px;align-items:end;padding-top:14px;border-top:1px solid #edf1f5; }
  .room-discovery .price-label { color:#7a8798;font-size:11px; }
  .room-discovery .price { margin-top:2px;color:var(--room-blue);font-size:20px;font-weight:800; }
  .room-discovery .price small { color:#68778a;font-size:11px;font-weight:500; }
  .room-discovery .room-actions { display:flex;gap:7px; }
  .room-discovery .room-actions .btn { padding:8px 11px;border-radius:9px; }
  .room-discovery .empty-room { padding:54px 24px;text-align:center;border:1px dashed #bfd0e3;border-radius:16px;background:#f8fbff;color:#68778a; }
  @media(max-width:980px){.room-discovery .room-search form{grid-template-columns:repeat(2,1fr)}.room-discovery .search-submit{width:100%}.room-discovery .room-grid{grid-template-columns:repeat(2,1fr)}}
  @media(max-width:650px){.room-discovery .room-hero{padding:28px 22px 78px}.room-discovery .room-hero h1{font-size:27px}.room-discovery .room-search{margin:-52px 10px 28px}.room-discovery .room-search form,.room-discovery .room-grid{grid-template-columns:1fr}.room-discovery .results-head{align-items:flex-start;flex-direction:column}.room-discovery .price-row{align-items:flex-start;flex-direction:column}.room-discovery .room-actions{width:100%}.room-discovery .room-actions .btn{flex:1;text-align:center}}
</style>

<div class="room-discovery">
  <section class="room-hero">
    <div class="hero-copy">
      <span class="hero-kicker">✦ Sunrise Hanoi Hotel</span>
      <h1>Tìm căn phòng dành cho kỳ nghỉ của bạn</h1>
      <p>Chọn ngày lưu trú và số khách để khám phá những hạng phòng đang sẵn sàng.</p>
    </div>
  </section>

  <div class="room-search">
    <form method="get" action="${pageContext.request.contextPath}/rooms">
      <div class="search-field"><label>📅 Ngày nhận phòng</label><input type="date" name="checkIn" value="${checkIn}" required></div>
      <div class="search-field"><label>📅 Ngày trả phòng</label><input type="date" name="checkOut" value="${checkOut}" required></div>
      <div class="search-field"><label>👥 Người lớn</label><input type="number" name="adults" min="1" value="${adults}"></div>
      <div class="search-field"><label>🧒 Trẻ em</label><input type="number" name="children" min="0" value="${children}"></div>
      <button class="btn search-submit" type="submit">Tìm phòng</button>
    </form>
  </div>

  <div class="results-head">
    <div><h2>Khám phá các loại phòng</h2><p>${totalResults} lựa chọn còn phòng trong thời gian bạn đã chọn</p></div>
    <span class="stay-chip">${nights} đêm · ${adults} người lớn · ${children} trẻ em</span>
  </div>

  <c:choose>
    <c:when test="${empty results}">
      <div class="empty-room">📭 Không có phòng phù hợp trong khoảng ngày đã chọn. Vui lòng thử lại với ngày khác.</div>
    </c:when>
    <c:otherwise>
      <div class="room-grid">
        <c:forEach var="a" items="${results}">
          <article class="room-card">
            <div class="room-visual">
              <span class="stock-badge">Còn ${a.availableRooms} phòng</span>
              <div class="room-title"><h3><c:out value="${a.roomType.typeName}"/></h3><span>Mã phòng: <c:out value="${a.roomType.typeCode}"/></span></div>
            </div>
            <div class="room-body">
              <p class="room-description"><c:out value="${empty a.roomType.description ? 'Không gian nghỉ dưỡng tiện nghi, phù hợp cho kỳ lưu trú của bạn.' : a.roomType.description}"/></p>
              <div class="room-facts">
                <span class="room-fact">🛏 <c:out value="${a.roomType.bedType}"/></span>
                <span class="room-fact">👥 ${a.roomType.maxAdults} NL + ${a.roomType.maxChildren} TE</span>
                <c:if test="${not empty a.roomType.roomSizeM2}"><span class="room-fact">↔ ${a.roomType.roomSizeM2} m²</span></c:if>
              </div>
              <div class="price-row">
                <div><div class="price-label">Tổng cho ${nights} đêm</div><div class="price"><fmt:formatNumber value="${a.totalPricePerRoom}"/> đ</div><small class="muted"><fmt:formatNumber value="${a.nightlyAvgPrice}"/> đ / đêm</small></div>
                <div class="room-actions">
                  <a class="btn btn-muted btn-small" href="${pageContext.request.contextPath}/rooms/detail?id=${a.roomType.roomTypeId}">Chi tiết</a>
                  <a class="btn btn-success btn-small" href="${pageContext.request.contextPath}/booking?roomTypeId=${a.roomType.roomTypeId}&checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}">Đặt ngay</a>
                </div>
              </div>
            </div>
          </article>
        </c:forEach>
      </div>

      <c:if test="${totalPages > 1}">
        <div class="pagination" style="display:flex;justify-content:center;align-items:center;gap:16px;margin-top:28px">
          <c:if test="${currentPage > 1}"><a class="btn btn-muted btn-small" href="?checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}&page=${currentPage-1}">‹ Trang trước</a></c:if>
          <span class="stay-chip">Trang ${currentPage} / ${totalPages}</span>
          <c:if test="${currentPage < totalPages}"><a class="btn btn-muted btn-small" href="?checkIn=${checkIn}&checkOut=${checkOut}&adults=${adults}&children=${children}&page=${currentPage+1}">Trang sau ›</a></c:if>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="_footer.jspf" %>
