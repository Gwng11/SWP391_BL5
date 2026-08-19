package com.hotel.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private long customerId;
    private Long userId;
    private Long createdByUserId;
    private String customerCode;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String idDocumentType;
    private String idDocumentNumber;
    private String nationality;
    private String address;
    private String statusCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getIdDocumentType() { return idDocumentType; }
    public void setIdDocumentType(String idDocumentType) { this.idDocumentType = idDocumentType; }
    public String getIdDocumentNumber() { return idDocumentNumber; }
    public void setIdDocumentNumber(String idDocumentNumber) { this.idDocumentNumber = idDocumentNumber; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
