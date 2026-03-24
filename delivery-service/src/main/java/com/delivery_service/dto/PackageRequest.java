package com.delivery_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PackageRequest {

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private BigDecimal weightKg;

    @NotNull(message = "Length is required")
    @Positive(message = "Length must be positive")
    private BigDecimal lengthCm;

    @NotNull(message = "Width is required")
    @Positive(message = "Width must be positive")
    private BigDecimal widthCm;

    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    private BigDecimal heightCm;

    private String description;

    // Getters and Setters

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getLengthCm() { return lengthCm; }
    public void setLengthCm(BigDecimal lengthCm) { this.lengthCm = lengthCm; }

    public BigDecimal getWidthCm() { return widthCm; }
    public void setWidthCm(BigDecimal widthCm) { this.widthCm = widthCm; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}