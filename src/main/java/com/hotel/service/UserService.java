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

    private final IUserRepository userRepo;
    private final ICustomerRepository customerRepo;

    public UserService() {
        this(new UserRepository(), new CustomerRepository());
    }

    public UserService(IUserRepository userRepo, ICustomerRepository customerRepo) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
    }

    public User getUser(long userId) { return userRepo.findById(userId); }

    public Customer getCustomerProfile(long userId) { return customerRepo.findByUserId(userId); }

    /** Cập nhật tên/sđt trên users, đồng bộ sang customers (nếu là khách hàng) */
    public void updateProfile(long userId, String fullName, String phone, String address,
                              String identificationNumber,
                              Customer customerInfo /* null nếu không phải CUSTOMER */) {
        if (ValidationUtil.isBlank(fullName)) throw new IllegalArgumentException("Họ tên không được để trống");
        if (fullName.trim().length() > 150) throw new IllegalArgumentException("Họ tên tối đa 150 ký tự");
        if (!ValidationUtil.isBlank(phone) && !ValidationUtil.isPhone(phone))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        if (address != null && address.trim().length() > 250)
            throw new IllegalArgumentException("Địa chỉ tối đa 250 ký tự");
        if (identificationNumber != null && identificationNumber.trim().length() > 100)
            throw new IllegalArgumentException("Thông tin định danh tối đa 100 ký tự");
        userRepo.updateProfile(userId, fullName.trim(), normalize(phone), normalize(address),
                normalize(identificationNumber));

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

    private String normalize(String value) {
        return ValidationUtil.isBlank(value) ? null : value.trim();
    }
}
