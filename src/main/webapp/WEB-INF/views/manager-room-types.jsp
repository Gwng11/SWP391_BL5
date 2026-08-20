<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="_header.jspf" %>
<div class="page-head"><h1>Loại phòng, Amenities & Images</h1><a class="btn" href="${pageContext.request.contextPath}/manager/dashboard">Dashboard</a></div>
<div class="grid">
  <div class="card col-6 table-wrap"><h2>Room Types</h2>
    <table><thead><tr><th>Mã</th><th>Tên</th><th>Sức chứa</th><th>Base price</th><th>Sales</th><th></th></tr></thead><tbody>
    <c:forEach var="t" items="${roomTypes}"><tr><td><c:out value="${t.typeCode}"/></td><td><c:out value="${t.typeName}"/></td><td>${t.maxAdults} + ${t.maxChildren}</td><td><fmt:formatNumber value="${t.basePrice}" pattern="#,##0"/> đ</td><td>${t.active ? 'ACTIVE' : 'INACTIVE'}</td><td><a class="btn btn-small" href="?edit=${t.roomTypeId}">Chi tiết</a><form method="post" class="inline"><input type="hidden" name="action" value="toggle"><input type="hidden" name="roomTypeId" value="${t.roomTypeId}"><input type="hidden" name="active" value="${!t.active}"><button class="btn btn-small ${t.active ? 'btn-danger' : 'btn-success'}" type="submit">${t.active ? 'Deactivate' : 'Activate'}</button></form></td></tr></c:forEach>
    </tbody></table>
  </div>
  <div class="card col-6"><h2>${empty editType ? 'Tạo loại phòng' : 'Cập nhật loại phòng'}</h2>
    <form method="post" class="form-grid"><c:if test="${not empty editType}"><input type="hidden" name="roomTypeId" value="${editType.roomTypeId}"></c:if>
      <div><label>Mã loại *</label><input name="typeCode" required maxlength="20" value="${editType.typeCode}" style="width:100%"></div>
      <div><label>Tên loại *</label><input name="typeName" required maxlength="100" value="${editType.typeName}" style="width:100%"></div>
      <div><label>Max adults *</label><input type="number" min="1" name="maxAdults" required value="${empty editType ? 2 : editType.maxAdults}" style="width:100%"></div>
      <div><label>Max children *</label><input type="number" min="0" name="maxChildren" required value="${empty editType ? 0 : editType.maxChildren}" style="width:100%"></div>
      <div><label>Bed type</label><input name="bedType" value="${editType.bedType}" style="width:100%"></div>
      <div><label>Room size (m²)</label><input type="number" min="0.01" step="0.01" name="roomSizeM2" value="${editType.roomSizeM2}" style="width:100%"></div>
      <div><label>Base price *</label><input type="number" min="0" step="1000" name="basePrice" required value="${empty editType ? 0 : editType.basePrice}" style="width:100%"></div>
      <div><label>Sales status</label><select name="active" style="width:100%"><option value="true" ${empty editType || editType.active ? 'selected' : ''}>ACTIVE</option><option value="false" ${not empty editType && !editType.active ? 'selected' : ''}>INACTIVE</option></select></div>
      <div class="full"><label>Mô tả</label><textarea name="description" rows="4" style="width:100%"><c:out value="${editType.description}"/></textarea></div>
      <div class="full form-actions"><button class="btn" type="submit">Lưu loại phòng</button><c:if test="${not empty editType}"><a href="${pageContext.request.contextPath}/manager/room-types">Tạo mới</a></c:if></div>
    </form>
  </div>
</div>
<c:if test="${not empty editType}">
<div class="grid">
  <div class="card col-6"><h2>Amenities</h2>
    <form method="post" class="inline"><input type="hidden" name="action" value="amenityAdd"><input type="hidden" name="roomTypeId" value="${editType.roomTypeId}"><input name="value" required maxlength="100" placeholder="Amenity mới"><button class="btn" type="submit">Thêm</button></form>
    <c:choose><c:when test="${empty amenities}"><div class="empty">Chưa có amenity.</div></c:when><c:otherwise><ul>
      <c:forEach var="a" items="${amenities}" varStatus="st"><li><form method="post" class="inline"><input type="hidden" name="roomTypeId" value="${editType.roomTypeId}"><input type="hidden" name="index" value="${st.index}"><input name="value" value="${fn:escapeXml(a)}" required><button class="btn btn-small" name="action" value="amenityUpdate">Sửa</button><button class="btn btn-danger btn-small" name="action" value="amenityRemove">Xóa</button></form></li></c:forEach>
    </ul></c:otherwise></c:choose>
  </div>
  <div class="card col-6"><h2>Room Images</h2>
    <form method="post" action="${pageContext.request.contextPath}/manager/room-types/images" enctype="multipart/form-data" class="inline"><input type="hidden" name="action" value="upload"><input type="hidden" name="roomTypeId" value="${editType.roomTypeId}"><input type="file" name="imageFile" accept="image/jpeg,image/png,image/webp" required><button class="btn" type="submit">Upload (≤ 5 MB)</button></form>
    <form method="post" action="${pageContext.request.contextPath}/manager/room-types/images" class="inline"><input type="hidden" name="action" value="addUrl"><input type="hidden" name="roomTypeId" value="${editType.roomTypeId}"><input type="url" name="imageUrl" required placeholder="https://...jpg" style="min-width:280px"><button class="btn" type="submit">Thêm URL</button></form>
    <div class="image-grid">
      <c:forEach var="img" items="${images}" varStatus="st"><div class="image-tile">
        <c:choose><c:when test="${fn:startsWith(img, 'http://') || fn:startsWith(img, 'https://')}"><img src="${fn:escapeXml(img)}" alt="Ảnh loại phòng ${st.index + 1}" loading="lazy"></c:when><c:otherwise><img src="${pageContext.request.contextPath}${fn:escapeXml(img)}" alt="Ảnh loại phòng ${st.index + 1}" loading="lazy"></c:otherwise></c:choose>
        <form method="post" action="${pageContext.request.contextPath}/manager/room-types/images"><input type="hidden" name="roomTypeId" value="${editType.roomTypeId}"><input type="hidden" name="index" value="${st.index}"><input type="text" name="imageUrl" value="${fn:escapeXml(img)}" style="width:100%"><div class="form-actions"><button class="btn btn-small" name="action" value="updateUrl">Thay URL</button><button class="btn btn-small" name="action" value="moveUp" ${st.first ? 'disabled' : ''}>↑</button><button class="btn btn-small" name="action" value="moveDown" ${st.last ? 'disabled' : ''}>↓</button><button class="btn btn-danger btn-small" name="action" value="remove">Xóa</button></div></form>
      </div></c:forEach>
    </div>
  </div>
</div>
</c:if>
<%@ include file="_footer.jspf" %>
