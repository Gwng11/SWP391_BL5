package com.hotel.interfaces;

import com.hotel.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public interface IUserRepository {
    User findByEmail(String email);
    User findById(long userId);
    List<User> findActiveByRole(String roleCode);
    long insert(User u);
    void updateProfile(long userId, String fullName, String phone, String address, String identificationNumber);
    void updatePassword(long userId, String passwordHash);
    void recordLoginSuccess(long userId);
    void recordLoginFailure(long userId, int failedAttempts, LocalDateTime lockedUntil);
    void markEmailVerified(long userId);
    /** Danh sách nhân viên dịch vụ active theo bộ phận, xếp theo số việc đang gánh. */
    List<User> findActiveServiceStaffWithWorkload(String departmentCode);
    List<User> findAll(String search, String roleCode, String statusCode);
    void updateByAdmin(long userId, String fullName, String phone, String address, String identificationNumber, String roleCode, String departmentCode, String statusCode, LocalDateTime lockedUntil);
    void delete(long userId);
}
