package com.hotel.interfaces;

import com.hotel.entity.EmailLog;

public interface IEmailLogRepository {
    long insert(EmailLog log);
    void markSent(long emailLogId, String providerName, String providerMessageId);
    void markFailed(long emailLogId, String error);
}
