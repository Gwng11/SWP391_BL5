package com.hotel.interfaces;

import com.hotel.entity.EmailTemplate;
import java.util.List;

public interface IEmailTemplateRepository {
    EmailTemplate findActiveByEvent(String eventCode);
    List<EmailTemplate> findAll();
    EmailTemplate findById(long templateId);
    long insert(EmailTemplate t);
    void update(EmailTemplate t);
    void delete(long templateId);
}
