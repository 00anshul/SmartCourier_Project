export interface TrackingEvent {
  id: number;
  deliveryId: number;
  trackingNumber: string;
  status: string;
  location: string;
  description: string;
  locationDescription?: string;
  hubId?: number;
  eventTime: string;
  createdBy?: number;
}

export interface TrackingEventRequest {
  deliveryId: number;
  trackingNumber: string;
  status: string;
  location: string;
  description: string;
}

export interface DeliveryProof {
  id: number;
  deliveryId: number;
  recipientName: string;
  signatureUrl?: string;
  proofImageUrl?: string;
  deliveredAt: string;
}
