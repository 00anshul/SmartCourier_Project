package com.delivery_service.dto;

import com.delivery_service.entity.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CreateDeliveryRequest {

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @Valid
    @NotNull(message = "Sender address is required")
    private AddressRequest senderAddress;

    @Valid
    @NotNull(message = "Receiver address is required")
    private AddressRequest receiverAddress;

    @Valid
    @NotNull(message = "Package details are required")
    private PackageRequest packageDetails;

    private String notes;

    // Getters and Setters

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public AddressRequest getSenderAddress() { return senderAddress; }
    public void setSenderAddress(AddressRequest senderAddress) { this.senderAddress = senderAddress; }

    public AddressRequest getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(AddressRequest receiverAddress) { this.receiverAddress = receiverAddress; }

    public PackageRequest getPackageDetails() { return packageDetails; }
    public void setPackageDetails(PackageRequest packageDetails) { this.packageDetails = packageDetails; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
