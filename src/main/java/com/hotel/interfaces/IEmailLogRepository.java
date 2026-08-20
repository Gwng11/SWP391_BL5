package com.hotel.interfaces;

import com.hotel.entity.EmailLog;
import java.util.List;

public interface IEmailLogRepository {
    long insert(EmailLog log);
    void markSent(long emailLogId, String providerName, String providerMessageId);
    void markFailed(long emailLogId, String error);
    List<EmailLog> findAll(String search, String statusCode);
    EmailLog findById(long emailLogId);
    void incrementRetryCount(long emailLogId);
}
