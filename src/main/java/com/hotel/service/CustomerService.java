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

    /** Tạo hồ sơ khách walk-in (không gắn user account) */
    public long createWalkIn(Customer c, long createdByUserId) {
        if (ValidationUtil.isBlank(c.getFullName())) throw new IllegalArgumentException("Họ tên không được để trống");
        validateLengths(c);
        if (!ValidationUtil.isBlank(c.getEmail()) && !ValidationUtil.isEmail(c.getEmail()))
            throw new IllegalArgumentException("Email không hợp lệ");
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
        customerRepo.update(c);
    }
}
