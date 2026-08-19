package com.hotel.interfaces;

import com.hotel.entity.User;
import java.time.LocalDateTime;

public interface IUserRepository {
    User findByEmail(String email);
    User findById(long userId);
    long insert(User u);
    void updateProfile(long userId, String fullName, String phone);
    void updatePassword(long userId, String passwordHash);
    void recordLoginSuccess(long userId);
    void recordLoginFailure(long userId, int failedAttempts, LocalDateTime lockedUntil);
    void markEmailVerified(long userId);
}
