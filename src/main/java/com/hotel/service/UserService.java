package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.User;
import com.hotel.interfaces.ICustomerRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.UserRepository;
import com.hotel.ultis.ValidationUtil;
import java.time.LocalDate;

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

    /**
     * Hoàn thiện hồ sơ khách hàng ngay trong luồng đặt phòng online.
     * Các trường này là thông tin tối thiểu để đơn có thể được lễ tân kiểm tra mà
     * không phải chờ khách tới quầy mới bổ sung hồ sơ.
     */
    public Customer completeCustomerProfileForReservation(long userId, Customer submitted) {
        Customer existing = customerRepo.findByUserId(userId);
        if (existing == null) throw new IllegalStateException("Tài khoản chưa có hồ sơ khách hàng");
        if (submitted == null) throw new IllegalArgumentException("Thiếu thông tin khách hàng đặt phòng");

        submitted.setFullName(required(submitted.getFullName(), "Họ tên"));
        submitted.setPhone(required(submitted.getPhone(), "Số điện thoại").replaceAll("[\\s.\\-]", ""));
        submitted.setIdDocumentType(required(submitted.getIdDocumentType(), "Loại giấy tờ"));
        submitted.setIdDocumentNumber(required(submitted.getIdDocumentNumber(), "Số giấy tờ"));
        submitted.setNationality(required(submitted.getNationality(), "Quốc tịch"));
        submitted.setAddress(required(submitted.getAddress(), "Địa chỉ"));

        if (submitted.getDateOfBirth() == null)
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        LocalDate today = LocalDate.now();
        if (!submitted.getDateOfBirth().isBefore(today)
                || submitted.getDateOfBirth().isBefore(today.minusYears(120)))
            throw new IllegalArgumentException("Ngày sinh không hợp lệ");
        if (!ValidationUtil.isPhone(submitted.getPhone()))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (8-15 chữ số)");
        if (!"CCCD".equals(submitted.getIdDocumentType())
                && !"PASSPORT".equals(submitted.getIdDocumentType()))
            throw new IllegalArgumentException("Loại giấy tờ không hợp lệ");
        if (!ValidationUtil.isValidDocument(submitted.getIdDocumentType(), submitted.getIdDocumentNumber()))
            throw new IllegalArgumentException("Số giấy tờ không hợp lệ (CCCD: 12 chữ số; Hộ chiếu: 6-9 ký tự chữ/số)");

        ValidationUtil.requireMaxLen(submitted.getFullName(), 150, "Họ tên");
        ValidationUtil.requireMaxLen(submitted.getPhone(), 30, "Số điện thoại");
        ValidationUtil.requireMaxLen(submitted.getIdDocumentNumber(), 50, "Số giấy tờ");
        ValidationUtil.requireMaxLen(submitted.getNationality(), 80, "Quốc tịch");
        ValidationUtil.requireMaxLen(submitted.getAddress(), 250, "Địa chỉ");

        Customer duplicate = customerRepo.findByDocument(
                submitted.getIdDocumentType(), submitted.getIdDocumentNumber());
        if (duplicate != null && duplicate.getCustomerId() != existing.getCustomerId())
            throw new IllegalArgumentException("Giấy tờ tùy thân đã được sử dụng bởi khách hàng khác");

        submitted.setCustomerId(existing.getCustomerId());
        submitted.setUserId(existing.getUserId());
        submitted.setCreatedByUserId(existing.getCreatedByUserId());
        submitted.setCustomerCode(existing.getCustomerCode());
        submitted.setEmail(existing.getEmail());
        submitted.setStatusCode(existing.getStatusCode());

        updateProfile(userId, submitted.getFullName(), submitted.getPhone(), submitted.getAddress(),
                submitted.getIdDocumentNumber(), submitted);
        return customerRepo.findByUserId(userId);
    }

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

    private String required(String value, String label) {
        if (ValidationUtil.isBlank(value))
            throw new IllegalArgumentException(label + " không được để trống");
        return value.trim();
    }
}
