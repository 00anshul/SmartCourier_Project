export type DeliveryStatus = 'DRAFT' | 'BOOKED' | 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'DELAYED' | 'FAILED' | 'RETURNED' | 'REFUNDED' | 'CANCELLED';
export type ServiceType = 'STANDARD' | 'EXPRESS' | 'OVERNIGHT';
export type AddressType = 'SENDER' | 'RECEIVER';

export interface Address {
  id?: number;
  type: AddressType;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  contactName: string;
  contactPhone: string;
}

export interface Parcel {
  id?: number;
  weightKg: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  description?: string;
}

export interface Pickup {
  id?: number;
  scheduledTime: string;
  actualPickupTime?: string;
  status: string;
}

export interface Delivery {
  id: number;
  trackingNumber: string;
  customerId: number;
  status: DeliveryStatus;
  serviceType: ServiceType;
  totalCharge: number;
  notes?: string;
  parcel?: Parcel;
  addresses?: Address[];
  pickup?: Pickup;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDeliveryRequest {
  serviceType: ServiceType;
  notes?: string;
  parcel: {
    weightKg: number;
    lengthCm?: number;
    widthCm?: number;
    heightCm?: number;
    description?: string;
  };
  senderAddress: Omit<Address, 'id' | 'type'>;
  receiverAddress: Omit<Address, 'id' | 'type'>;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
