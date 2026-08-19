<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="_header.jspf" %>
<div class="card">
  <h2>Quản lý khách hàng</h2>
  <form method="get" action="${pageContext.request.contextPath}/reception/customers">
    <input name="q" placeholder="Tên / email / SĐT / mã KH / số giấy tờ" value="${param.q}" style="width:340px">
    <button class="btn" type="submit">Tìm kiếm</button>
  </form>
  <c:if test="${customers != null}">
  <table style="margin-top:12px">
    <tr><th>ID</th><th>Mã KH</th><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Giấy tờ</th><th></th></tr>
    <c:forEach var="c" items="${customers}">
      <tr><td>${c.customerId}</td><td>${c.customerCode}</td><td>${c.fullName}</td><td>${c.email}</td>
          <td>${c.phone}</td><td>${c.idDocumentType} ${c.idDocumentNumber}</td>
          <td><a class="btn" href="?edit=${c.customerId}&q=${param.q}">Sửa</a></td></tr>
    </c:forEach>
  </table>
  </c:if>
</div>
<div class="card" style="max-width:560px">
  <h3>${editing == null ? 'Tạo khách walk-in' : 'Cập nhật khách hàng #'.concat(editing.customerId)}</h3>
  <form method="post" action="${pageContext.request.contextPath}/reception/customers">
    <c:if test="${editing != null}"><input type="hidden" name="customerId" value="${editing.customerId}"></c:if>
    <label>Họ tên *</label><input name="fullName" value="${editing.fullName}" style="width:100%" required>
    <label>Email</label><input name="email" value="${editing.email}" style="width:100%">
    <label>SĐT</label><input name="phone" value="${editing.phone}" style="width:100%">
    <label>Ngày sinh</label><input type="date" name="dateOfBirth" value="${editing.dateOfBirth}" style="width:100%">
    <label>Loại giấy tờ</label>
    <select name="idDocumentType" style="width:100%">
      <option value="">-- không --</option>
      <option value="CCCD" ${editing.idDocumentType == 'CCCD' ? 'selected' : ''}>CCCD</option>
      <option value="PASSPORT" ${editing.idDocumentType == 'PASSPORT' ? 'selected' : ''}>Hộ chiếu</option>
    </select>
    <label>Số giấy tờ</label><input name="idDocumentNumber" value="${editing.idDocumentNumber}" style="width:100%">
    <label>Quốc tịch</label><input name="nationality" value="${editing.nationality}" style="width:100%">
    <label>Địa chỉ</label><input name="address" value="${editing.address}" style="width:100%">
    <p><button class="btn btn-success" type="submit">${editing == null ? 'Tạo hồ sơ' : 'Lưu thay đổi'}</button></p>
  </form>
</div>
<%@ include file="_footer.jspf" %>
