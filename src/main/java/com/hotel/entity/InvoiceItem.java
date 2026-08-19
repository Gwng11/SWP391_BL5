package com.hotel.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceItem {
    private long invoiceItemId;
    private long invoiceId;
    private Long serviceRequestId;
    private Long postedByUserId;
    private String itemType;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private LocalDateTime postedAt;
    private boolean voided;

    public long getInvoiceItemId() { return invoiceItemId; }
    public void setInvoiceItemId(long invoiceItemId) { this.invoiceItemId = invoiceItemId; }
    public long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(long invoiceId) { this.invoiceId = invoiceId; }
    public Long getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(Long serviceRequestId) { this.serviceRequestId = serviceRequestId; }
    public Long getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(Long postedByUserId) { this.postedByUserId = postedByUserId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public boolean isVoided() { return voided; }
    public void setVoided(boolean voided) { this.voided = voided; }
}
