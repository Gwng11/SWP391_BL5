package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.ultis.CodeGenerator;
import com.hotel.ultis.ValidationUtil;
import java.util.List;

/** F09 - Lễ tân quản lý khách hàng (kể cả walk-in không có tài khoản) */
public class CustomerService {

    private final ICustomerRepository customerRepo = new CustomerRepository();

    public List<Customer> search(String keyword) { return customerRepo.search(keyword); }

    public Customer getById(long customerId) { return customerRepo.findById(customerId); }

    /** V4: chặn vượt độ dài cột DB (customers) */
    private void validateLengths(Customer c) {
        ValidationUtil.requireMaxLen(c.getFullName(), 150, "Họ tên");
        ValidationUtil.requireMaxLen(c.getEmail(), 255, "Email");
        ValidationUtil.requireMaxLen(c.getPhone(), 30, "Số điện thoại");
        ValidationUtil.requireMaxLen(c.getIdDocumentType(), 30, "Loại giấy tờ");
        ValidationUtil.requireMaxLen(c.getIdDocumentNumber(), 50, "Số giấy tờ");
        ValidationUtil.requireMaxLen(c.getNationality(), 80, "Quốc tịch");
        ValidationUtil.requireMaxLen(c.getAddress(), 255, "Địa chỉ");
    }

    /** V5-V8: validate định dạng dữ liệu khách (SĐT, email, ngày sinh, giấy tờ) */
    private void validateFormats(Customer c) {
        // V5: số điện thoại - chuẩn hóa bỏ khoảng trắng/chấm/gạch rồi kiểm tra 8-15 chữ số
        if (!ValidationUtil.isBlank(c.getPhone())) {
            c.setPhone(c.getPhone().replaceAll("[\\s.\\-]", ""));
            if (!ValidationUtil.isPhone(c.getPhone()))
                throw new IllegalArgumentException("Số điện thoại không hợp lệ (8-15 chữ số)");
        }
        // V6: email
        if (!ValidationUtil.isBlank(c.getEmail()) && !ValidationUtil.isEmail(c.getEmail()))
            throw new IllegalArgumentException("Email không hợp lệ");
        // V7: ngày sinh phải ở quá khứ và không quá 120 năm
        if (c.getDateOfBirth() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!c.getDateOfBirth().isBefore(today))
                throw new IllegalArgumentException("Ngày sinh phải là ngày trong quá khứ");
            if (c.getDateOfBirth().isBefore(today.minusYears(120)))
                throw new IllegalArgumentException("Ngày sinh không hợp lệ (quá 120 năm trước)");
        }
        // V8: định dạng số giấy tờ theo loại
        if (!ValidationUtil.isBlank(c.getIdDocumentNumber())
                && !ValidationUtil.isValidDocument(c.getIdDocumentType(), c.getIdDocumentNumber()))
            throw new IllegalArgumentException(
                    "Số giấy tờ không hợp lệ (CCCD: đúng 12 chữ số; Hộ chiếu: 6-9 ký tự chữ/số)");
    }

    /** Tạo hồ sơ khách walk-in (không gắn user account) */
    public long createWalkIn(Customer c, long createdByUserId) {
        if (ValidationUtil.isBlank(c.getFullName())) throw new IllegalArgumentException("Họ tên không được để trống");
        validateLengths(c);
        validateFormats(c);
        // CK_customers_document: loại và số giấy tờ phải đi cùng nhau
        boolean hasType = !ValidationUtil.isBlank(c.getIdDocumentType());
        boolean hasNumber = !ValidationUtil.isBlank(c.getIdDocumentNumber());
        if (hasType != hasNumber)
            throw new IllegalArgumentException("Loại và số giấy tờ tùy thân phải nhập cùng nhau");
        c.setUserId(null);
        c.setCreatedByUserId(createdByUserId);
        c.setCustomerCode(CodeGenerator.customerCode());
        return customerRepo.insert(c);
    }

    public void update(Customer c) {
        if (ValidationUtil.isBlank(c.getFullName())) throw new IllegalArgumentException("Họ tên không được để trống");
        validateLengths(c);
        validateFormats(c); // V5-V8: sửa hồ sơ cũng phải qua cùng bộ kiểm tra như tạo mới
        customerRepo.update(c);
    }
}
