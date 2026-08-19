package com.hotel.interfaces;

import com.hotel.entity.EmailTemplate;

public interface IEmailTemplateRepository {
    EmailTemplate findActiveByEvent(String eventCode);
}
