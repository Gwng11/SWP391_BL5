<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>

<div class="bk-page-header">
  <div class="bk-page-title">
    <h1>🧾 Hóa đơn của tôi</h1>
    <p>Đơn <c:out value="${r.bookingCode}"/> — xem chi tiết các khoản phí và số tiền còn lại.</p>
  </div>
  <a class="btn btn-muted" href="${pageContext.request.contextPath}/my-reservations">← Đơn của tôi</a>
</div>

<c:choose>
  <c:when test="${invoice == null || invoice.statusCode == 'DRAFT'}">
    <div class="card">
      <h2>Hóa đơn chưa được phát hành</h2>
      <p class="muted">Lễ tân sẽ phát hành hóa đơn cuối sau khi các yêu cầu dịch vụ của kỳ lưu trú được xử lý xong.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="card">
      <div style="display:flex;justify-content:space-between;gap:16px;align-items:center;flex-wrap:wrap">
        <div>
          <h2 style="margin-bottom:4px"><c:out value="${invoice.invoiceNumber}"/></h2>
          <span class="badge status-${invoice.statusCode}">${invoice.statusCode}</span>
        </div>
        <div style="text-align:right">
          <div class="muted">Còn phải thanh toán</div>
          <strong style="font-size:26px;color:var(--bk-blue)"><fmt:formatNumber value="${outstanding}"/> đ</strong>
        </div>
      </div>

      <div class="table-wrap" style="margin-top:20px">
        <table>
          <thead><tr><th>Nội dung</th><th>Loại</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>
          <tbody>
            <c:forEach var="i" items="${items}">
              <tr>
                <td><c:out value="${i.description}"/></td><td>${i.itemType}</td><td>${i.quantity}</td>
                <td><fmt:formatNumber value="${i.unitPrice}"/> đ</td>
                <td><strong><fmt:formatNumber value="${i.amount}"/> đ</strong></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>

      <div style="margin-top:20px;display:grid;gap:8px;max-width:430px;margin-left:auto">
        <div style="display:flex;justify-content:space-between"><span>Tạm tính</span><b><fmt:formatNumber value="${invoice.subtotal}"/> đ</b></div>
        <c:if test="${invoice.taxAmount > 0}">
          <div style="display:flex;justify-content:space-between"><span>Thuế</span><b><fmt:formatNumber value="${invoice.taxAmount}"/> đ</b></div>
        </c:if>
        <div style="display:flex;justify-content:space-between"><span>Tổng hóa đơn</span><b><fmt:formatNumber value="${invoice.totalAmount}"/> đ</b></div>
        <div style="display:flex;justify-content:space-between"><span>Đã thanh toán (gồm tiền cọc)</span><b><fmt:formatNumber value="${invoice.paidAmount}"/> đ</b></div>
      </div>

      <c:if test="${invoice.statusCode != 'PAID' && outstanding > 0}">
        <form method="post" action="${pageContext.request.contextPath}/my-invoice" style="margin-top:22px;text-align:right"
              onsubmit="return confirm('Xác nhận thanh toán toàn bộ số tiền còn lại?')">
          <input type="hidden" name="reservationId" value="${r.reservationId}">
          <button class="btn btn-success" type="submit">💳 Thanh toán online <fmt:formatNumber value="${outstanding}"/> đ</button>
        </form>
      </c:if>
    </div>
  </c:otherwise>
</c:choose>

<div class="card">
  <h2>📋 Lịch sử giao dịch</h2>
  <c:choose>
    <c:when test="${empty payments}"><div class="empty">Chưa có giao dịch.</div></c:when>
    <c:otherwise>
      <div class="table-wrap"><table>
        <thead><tr><th>Loại</th><th>Phương thức</th><th>Số tiền</th><th>Trạng thái</th><th>Mã tham chiếu</th><th>Thời gian</th></tr></thead>
        <tbody><c:forEach var="p" items="${payments}"><tr>
          <td>${p.paymentType}</td><td>${p.methodCode}</td>
          <td><fmt:formatNumber value="${p.amount}"/> đ</td>
          <td><span class="badge status-${p.statusCode}">${p.statusCode}</span></td>
          <td><c:out value="${empty p.providerReference ? '-' : p.providerReference}"/></td>
          <td>${p.paidAt != null ? p.paidAt : p.createdAt}</td>
        </tr></c:forEach></tbody>
      </table></div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="_footer.jspf" %>
