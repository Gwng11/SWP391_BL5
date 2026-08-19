package com.hotel.interfaces;

import com.hotel.entity.UserToken;

public interface IUserTokenRepository {
    void insert(UserToken t);
    /** Tìm token còn hạn, chưa dùng */
    UserToken findValid(String tokenHash, String tokenType);
    void markUsed(long userTokenId);
    /** Vô hiệu các token cũ cùng loại của user (khi phát hành token mới) */
    void invalidateOld(long userId, String tokenType);
}
