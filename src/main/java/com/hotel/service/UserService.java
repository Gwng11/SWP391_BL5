package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.User;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.UserRepository;
import com.hotel.ultis.ValidationUtil;

/** F05 - Quản lý hồ sơ cá nhân */
public class UserService {

    private final IUserRepository userRepo = new UserRepository();
    private final ICustomerRepository customerRepo = new CustomerRepository();

    public User getUser(long userId) { return userRepo.findById(userId); }

    public Customer getCustomerProfile(long userId) { return customerRepo.findByUserId(userId); }

    /** Cập nhật tên/sđt trên users, đồng bộ sang customers (nếu là khách hàng) */
    public void updateProfile(long userId, String fullName, String phone,
                              Customer customerInfo /* null nếu không phải CUSTOMER */) {
        if (ValidationUtil.isBlank(fullName)) throw new IllegalArgumentException("Họ tên không được để trống");
        if (!ValidationUtil.isBlank(phone) && !ValidationUtil.isPhone(phone))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        userRepo.updateProfile(userId, fullName.trim(), phone);

        Customer existing = customerRepo.findByUserId(userId);
        if (existing != null) {
            existing.setFullName(fullName.trim());
            existing.setPhone(phone);
            if (customerInfo != null) {
                existing.setDateOfBirth(customerInfo.getDateOfBirth());
                existing.setIdDocumentType(customerInfo.getIdDocumentType());
                existing.setIdDocumentNumber(customerInfo.getIdDocumentNumber());
                existing.setNationality(customerInfo.getNationality());
                existing.setAddress(customerInfo.getAddress());
            }
            customerRepo.update(existing);
        }
    }
}
