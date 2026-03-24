package com.delivery_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SchedulePickupRequest {

    @NotNull(message = "Scheduled date is required")
    @Future(message = "Pickup date must be in the future")
    private LocalDate scheduledDate;

    @NotBlank(message = "Slot is required")
    private String slot;

    // Getters and Setters

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
}