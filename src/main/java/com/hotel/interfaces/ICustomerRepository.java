package com.hotel.interfaces;

import com.hotel.entity.Customer;
import java.util.List;

public interface ICustomerRepository {
    Customer findById(long customerId);
    Customer findByUserId(long userId);
    Customer findByDocument(String documentType, String documentNumber);
    /** F09: tìm theo tên / email / sđt / mã KH / số giấy tờ */
    List<Customer> search(String keyword);
    long insert(Customer c);
    void update(Customer c);
}
