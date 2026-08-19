<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Hóa đơn — Đơn ${r.bookingCode} (${r.customerName})</h2>
  <c:choose>
    <c:when test="${invoice == null || invoice.statusCode == 'DRAFT'}">
      <p>Chưa phát hành hóa đơn cuối. Tổng đã thanh toán (cọc): <fmt:formatNumber value="${totalPaid}"/> đ</p>
      <c:if test="${invoice != null && not empty items}">
        <h4>Phụ thu đã ghi trong kỳ ở (hủy được trước khi phát hành)</h4>
        <table>
          <tr><th>Mô tả</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th><th></th></tr>
          <c:forEach var="i" items="${items}">
            <tr>
              <td>${i.description}</td><td>${i.quantity}</td>
              <td><fmt:formatNumber value="${i.unitPrice}"/> đ</td>
              <td><fmt:formatNumber value="${i.amount}"/> đ</td>
              <td>
                <c:if test="${i.itemType == 'EXTRA'}">
                  <form method="post" style="display:inline"
                        action="${pageContext.request.contextPath}/reception/invoice"
                        onsubmit="return confirm('Hủy dòng phụ thu này?')">
                    <input type="hidden" name="reservationId" value="${r.reservationId}">
                    <input type="hidden" name="action" value="voidItem">
                    <input type="hidden" name="itemId" value="${i.invoiceItemId}">
                    <button class="btn btn-danger" style="padding:2px 8px;font-size:12px" type="submit">Hủy dòng</button>
                  </form>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
      </c:if>
      <form method="post" action="${pageContext.request.contextPath}/reception/invoice">
        <input type="hidden" name="reservationId" value="${r.reservationId}">
        <input type="hidden" name="action" value="generate">
        <button class="btn" type="submit">📄 Phát hành hóa đơn cuối</button>
      </form>
    </c:when>
    <c:otherwise>
      <p><b>${invoice.invoiceNumber}</b> — <span class="badge">${invoice.statusCode}</span> — Phát hành: ${invoice.issuedAt}</p>
      <table><tr><th>Mục</th><th>Loại</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>
      <c:forEach var="i" items="${items}">
        <tr><td>${i.description}</td><td>${i.itemType}</td><td>${i.quantity}</td>
            <td><fmt:formatNumber value="${i.unitPrice}"/> đ</td><td><fmt:formatNumber value="${i.amount}"/> đ</td></tr>
      </c:forEach></table>
      <p>Tạm tính: <fmt:formatNumber value="${invoice.subtotal}"/> đ — Thuế: <fmt:formatNumber value="${invoice.taxAmount}"/> đ
       — <b>Tổng: <fmt:formatNumber value="${invoice.totalAmount}"/> đ</b>
       — Đã trả: <fmt:formatNumber value="${invoice.paidAmount}"/> đ
       — Còn lại: <b style="color:#c0392b"><fmt:formatNumber value="${outstanding}"/> đ</b></p>
      <c:if test="${invoice.statusCode != 'PAID'}">
        <form method="post" action="${pageContext.request.contextPath}/reception/invoice">
          <input type="hidden" name="reservationId" value="${r.reservationId}">
          <input type="hidden" name="action" value="pay">
          <select name="method">
            <option value="CASH">Tiền mặt</option>
            <option value="CARD">Thẻ</option>
            <option value="BANK_TRANSFER">Chuyển khoản</option>
            <option value="ONLINE">Online (giả lập)</option>
          </select>
          <button class="btn btn-success" type="submit">💰 Thu phần còn lại & gửi email hóa đơn</button>
        </form>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>
<%@ include file="_footer.jspf" %>
