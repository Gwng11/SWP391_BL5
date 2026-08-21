<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<!-- Booking Hero Header -->
<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>💳 Thủ tục Check-out Trả phòng</h1>
    <p>Đối chiếu hóa đơn thanh toán, thu phí phát sinh và xác nhận khách trả phòng chính thức</p>
  </div>
</div>

<c:if test="${param.done == '1'}">
  <div class="msg" style="margin-bottom: 20px;">🎉 Check-out thành công! Phòng đã được giải phóng và chuyển sang trạng thái chờ dọn dẹp.</div>
</c:if>

<div class="grid">
  <!-- Left Column: List of Stays to check-out (col-5) -->
  <div class="card col-5">
    <div style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
      <h2 style="margin:0; font-size:18px;">📋 Danh sách Phòng đang ở</h2>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th style="text-align: right;">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="rv" items="${stays}">
            <tr>
              <td style="font-family:monospace; font-weight:600; color:var(--bk-navy);">${rv.bookingCode}</td>
              <td><strong><c:out value="${rv.customerName}"/></strong></td>
              <td style="text-align: right;">
                <a class="btn btn-muted btn-small" href="?id=${rv.reservationId}">Xem chi phí</a>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Right Column: Invoice & Settle Confirmation Panel (col-7) -->
  <div class="col-7">
    <c:if test="${r != null}">
      <div class="card" style="border: 2px solid var(--bk-blue); background: var(--bk-blue-light);">
        <div style="border-bottom:1px solid var(--bk-border); padding-bottom:12px; margin-bottom:16px;">
          <h2 style="margin:0; font-size:18px; color:var(--bk-navy);">💳 Đơn ${r.bookingCode} — <c:out value="${r.customerName}"/></h2>
        </div>

        <c:choose>
          <%-- Case A: Invoice is draft/missing --%>
          <c:when test="${invoice == null || invoice.statusCode == 'DRAFT'}">
            <div style="background:#fee2e2; border:1px solid #fecaca; padding:16px; border-radius:8px; color:#b91c1c; font-weight:600;">
              ⚠️ Đơn phòng này chưa được phát hành hóa đơn thanh toán cuối cùng.
              <div style="margin-top:12px;">
                <a class="btn btn-danger" style="display:block; text-align:center;" href="${pageContext.request.contextPath}/reception/invoice?reservationId=${r.reservationId}">
                  👉 Sang trang Phát hành & Thanh toán Hóa đơn
                </a>
              </div>
            </div>
          </c:when>
          
          <%-- Case B: Invoice is ready --%>
          <c:otherwise>
            <div style="background:#ffffff; border:1px solid var(--bk-border); padding:16px; border-radius:8px; margin-bottom:16px;">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                <span>Số hóa đơn: <strong>${invoice.invoiceNumber}</strong></span>
                <span class="badge status-${invoice.statusCode}">${invoice.statusCode}</span>
              </div>
              <div style="display:flex; justify-content:space-between; font-size:15px; color:var(--bk-text);">
                <span>Tổng chi phí:</span>
                <strong style="color:var(--bk-navy); font-size:17px;"><fmt:formatNumber value="${invoice.totalAmount}"/> đ</strong>
              </div>
              <div style="display:flex; justify-content:space-between; font-size:15px; color:#047857; margin-top:6px;">
                <span>Khách đã trả:</span>
                <strong style="font-size:17px;"><fmt:formatNumber value="${invoice.paidAmount}"/> đ</strong>
              </div>
            </div>

            <h3 style="font-size:14px; font-weight:700; color:var(--bk-navy); margin-bottom:10px;">📋 Chi tiết các khoản phí</h3>
            <div class="table-wrap" style="background:#ffffff; border-radius:6px; border:1px solid var(--bk-border); margin-bottom:16px;">
              <table>
                <thead>
                  <tr>
                    <th>Mô tả</th>
                    <th>Loại</th>
                    <th>SL</th>
                    <th style="text-align: right;">Đơn giá</th>
                    <th style="text-align: right;">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="i" items="${items}">
                    <tr>
                      <td><c:out value="${i.description}"/></td>
                      <td><span class="badge" style="font-size:11px; padding:2px 6px;">${i.itemType}</span></td>
                      <td>${i.quantity}</td>
                      <td style="text-align: right;"><fmt:formatNumber value="${i.unitPrice}"/> đ</td>
                      <td style="text-align: right; font-weight:600;"><fmt:formatNumber value="${i.amount}"/> đ</td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/reception/checkout" onsubmit="return confirm('Xác nhận khách thanh toán hoàn tất và rời khỏi phòng?')">
              <input type="hidden" name="id" value="${r.reservationId}">
              <button class="btn btn-danger" type="submit" style="width:100%; font-size:16px; padding:12px; font-weight:700;">
                ✔ Xác nhận Khách rời đi (Check-out)
              </button>
            </form>
          </c:otherwise>
        </c:choose>
      </div>
    </c:if>
  </div>
</div>

<%@ include file="_footer.jspf" %>
