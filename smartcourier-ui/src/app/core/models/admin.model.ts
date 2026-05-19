export interface Hub {
  id: number;
  name: string;
  city: string;
  state: string;
  contactPhone: string;
  pincode: string;
  isActive: boolean;
  createdAt?: string;
}

export interface HubRequest {
  name: string;
  city: string;
  state: string;
  contactPhone: string;
  pincode: string;
}

export interface DashboardData {
  totalDeliveries: number;
  pendingDeliveries: number;
  inTransitDeliveries: number;
  deliveredDeliveries: number;
  totalHubs: number;
  activeHubs?: number;
  [key: string]: any;
}

export interface Report {
  id: number;
  reportType: string;
  startDate: string;
  endDate: string;
  generatedAt: string;
  generatedBy: number;
  data?: any;
}

export interface ReportRequest {
  reportType: string;
  startDate: string;
  endDate: string;
}

export interface ExceptionLog {
  id: number;
  deliveryId: number;
  description: string;
  resolvedBy?: number;
  resolvedAt?: string;
  resolution?: string;
}
