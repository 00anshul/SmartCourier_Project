package com.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResolveExceptionRequest {

    @NotNull(message = "Delivery ID is required")
    private Long deliveryId;

    @NotBlank(message = "Action is required")
    private String action;

    private String note;

    // Getters and Setters

    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}