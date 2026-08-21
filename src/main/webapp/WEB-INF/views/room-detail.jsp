<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🛏 Chi tiết Loại phòng: ${roomType.typeName}</h1>
    <p>Xem các thông số chi tiết, tiện ích đi kèm và đặt chỗ trực tuyến</p>
  </div>
  <div>
    <a class="btn btn-muted" href="${pageContext.request.contextPath}/rooms">← Quay lại danh sách</a>
  </div>
</div>

<div class="grid">
  <!-- Left Side: Room Type Info Table (col-8) -->
  <div class="card col-8">
    <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
      <h2 style="margin:0; font-size:18px;">📋 Thông tin mô tả</h2>
      <span class="badge status-ACTIVE" style="background:#e0f2fe; color:#0369a1; font-weight:600;">Mã phòng: ${roomType.typeCode}</span>
    </div>
    
    <p style="font-size:15px; color:var(--bk-muted); line-height:1.6; margin-bottom:24px;">
      ${roomType.description}
    </p>

    <div class="table-wrap">
      <table style="width:100%">
        <thead>
          <tr>
            <th style="width:40%">Thuộc tính</th>
            <th>Thông số / Chi tiết</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td><strong>🛏 Loại giường</strong></td>
            <td>${roomType.bedType}</td>
          </tr>
          <tr>
            <td><strong>📏 Diện tích phòng</strong></td>
            <td>${roomType.roomSizeM2} m²</td>
          </tr>
          <tr>
            <td><strong>👥 Sức chứa tối đa</strong></td>
            <td>
              <span style="font-weight:600; color:var(--bk-navy);">${roomType.maxAdults} người lớn</span> 
              + 
              <span style="font-weight:600; color:var(--bk-muted);">${roomType.maxChildren} trẻ em</span>
            </td>
          </tr>
          <tr>
            <td><strong>💰 Giá cơ bản/đêm</strong></td>
            <td style="font-weight:700; color:var(--bk-blue); font-size:16px;">
              <fmt:formatNumber value="${roomType.basePrice}"/> đ
            </td>
          </tr>
          <tr>
            <td><strong>✨ Tiện nghi đi kèm</strong></td>
            <td>
              <div style="display:flex; gap:6px; flex-wrap:wrap; margin-top:4px;">
                <c:out value="${roomType.amenitiesJson}"/>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Right Side: Booking Box Card (col-4) -->
  <div class="card col-4" style="background:var(--bk-gold-light); border: 2px solid var(--bk-gold-border); display:flex; flex-direction:column; justify-content:space-between; height:fit-content;">
    <div>
      <h3 style="margin-top:0; color:#92400e; font-size:16px; font-weight:700; border-bottom:1px solid rgba(254,187,2,0.3); padding-bottom:8px; margin-bottom:12px;">
        ⚡ Đặt phòng nhanh
      </h3>
      <p style="font-size:13px; color:#78350f; line-height:1.5; margin-bottom:16px;">
        Đảm bảo có phòng giá tốt nhất khi đặt trực tuyến ngay hôm nay qua hệ thống của chúng tôi.
      </p>
      <div style="font-size:13px; color:#78350f; margin-bottom:12px;">
        Giá trung bình từ:
      </div>
      <div style="font-size:26px; font-weight:800; color:var(--bk-navy); margin-bottom:16px;">
        <fmt:formatNumber value="${roomType.basePrice}"/> đ <span style="font-size:13px; font-weight:normal; color:#78350f;">/ đêm</span>
      </div>
    </div>
    
    <div>
      <a class="btn btn-success" href="${pageContext.request.contextPath}/booking?roomTypeId=${roomType.roomTypeId}" style="width:100%; text-align:center; padding:12px; font-size:15px; font-weight:700; display:block;">
        ⚡ ĐẶT PHÒNG NGAY
      </a>
      <a class="btn btn-muted" href="${pageContext.request.contextPath}/rooms" style="width:100%; text-align:center; margin-top:8px; display:block;">
        ← Tìm ngày khác
      </a>
    </div>
  </div>
</div>

<%@ include file="_footer.jspf" %>
