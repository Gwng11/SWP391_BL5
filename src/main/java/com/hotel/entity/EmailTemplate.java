package com.hotel.entity;

public class EmailTemplate {
    private long emailTemplateId;
    private String templateCode;
    private String templateName;
    private String eventCode;
    private String subjectTemplate;
    private String bodyHtml;
    private String bodyText;
    private boolean active;

    public long getEmailTemplateId() { return emailTemplateId; }
    public void setEmailTemplateId(long emailTemplateId) { this.emailTemplateId = emailTemplateId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }
    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }
    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
