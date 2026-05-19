import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { CreateDeliveryRequest, Delivery, PageResponse } from '../models/delivery.model';
import { TrackingEvent, TrackingEventRequest } from '../models/tracking.model';
import { DashboardData, ExceptionLog, Hub, HubRequest, Report, ReportRequest } from '../models/admin.model';

/**
 * Gateway routing (StripPrefix=1 strips /gateway):
 *   /gateway/auth/**       → auth-service      as /auth/**
 *   /gateway/deliveries/** → delivery-service  as /deliveries/**
 *   /gateway/tracking/**   → tracking-service  as /tracking/**
 *   /gateway/admin/**      → admin-service     as /admin/**
 *
 * So Angular URLs must be:  ${base}/{gatewayPrefix}/{controllerPath}
 *   e.g.  /gateway/auth/register  (NOT /gateway/auth/auth/register)
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl; // '/gateway'

  constructor(private http: HttpClient) {}

  // ─── Auth ───────────────────────────────────────────────────────────────────

  login(req: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/auth/login`, req);
  }

  register(req: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.base}/auth/register`, req);
  }

  // ─── Deliveries ─────────────────────────────────────────────────────────────

  createDelivery(req: CreateDeliveryRequest): Observable<ApiResponse<Delivery>> {
    return this.http.post<ApiResponse<Delivery>>(`${this.base}/deliveries`, req);
  }

  getMyDeliveries(page = 0, size = 10): Observable<ApiResponse<PageResponse<Delivery>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<Delivery>>>(`${this.base}/deliveries/my`, { params });
  }

  getDeliveryById(id: number): Observable<ApiResponse<Delivery>> {
    return this.http.get<ApiResponse<Delivery>>(`${this.base}/deliveries/${id}`);
  }

  trackByNumber(trackingNumber: string): Observable<ApiResponse<Delivery>> {
    return this.http.get<ApiResponse<Delivery>>(`${this.base}/deliveries/track/${trackingNumber}`);
  }

  updateDeliveryStatus(id: number, status: string, hubId?: number, location?: string): Observable<ApiResponse<Delivery>> {
    let params = new HttpParams().set('status', status);
    if (hubId != null) params = params.set('hubId', hubId.toString());
    if (location) params = params.set('location', location);
    return this.http.put<ApiResponse<Delivery>>(`${this.base}/deliveries/${id}/status`, null, { params });
  }

  cancelDelivery(id: number): Observable<ApiResponse<Delivery>> {
    return this.http.put<ApiResponse<Delivery>>(`${this.base}/deliveries/${id}/cancel`, null);
  }

  getAllDeliveries(page = 0, size = 20): Observable<ApiResponse<PageResponse<Delivery>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<Delivery>>>(`${this.base}/deliveries`, { params });
  }

  getQuote(serviceType: string, weightKg: number): Observable<ApiResponse<number>> {
    const params = new HttpParams().set('serviceType', serviceType).set('weightKg', weightKg);
    return this.http.get<ApiResponse<number>>(`${this.base}/deliveries/quote`, { params });
  }

  // ─── Tracking ────────────────────────────────────────────────────────────────

  getTrackingEvents(trackingNumber: string): Observable<ApiResponse<TrackingEvent[]>> {
    return this.http.get<ApiResponse<TrackingEvent[]>>(`${this.base}/tracking/${trackingNumber}`);
  }

  getTrackingEventsByDeliveryId(deliveryId: number): Observable<ApiResponse<TrackingEvent[]>> {
    return this.http.get<ApiResponse<TrackingEvent[]>>(`${this.base}/tracking/delivery/${deliveryId}`);
  }

  createTrackingEvent(req: TrackingEventRequest): Observable<ApiResponse<TrackingEvent>> {
    return this.http.post<ApiResponse<TrackingEvent>>(`${this.base}/tracking/events`, req);
  }

  // ─── Admin ───────────────────────────────────────────────────────────────────

  getDashboard(): Observable<ApiResponse<DashboardData>> {
    return this.http.get<ApiResponse<DashboardData>>(`${this.base}/admin/dashboard`);
  }

  getAllHubs(): Observable<ApiResponse<Hub[]>> {
    return this.http.get<ApiResponse<Hub[]>>(`${this.base}/admin/hubs`);
  }

  createHub(req: HubRequest): Observable<ApiResponse<Hub>> {
    return this.http.post<ApiResponse<Hub>>(`${this.base}/admin/hubs`, req);
  }

  toggleHub(id: number): Observable<ApiResponse<Hub>> {
    return this.http.put<ApiResponse<Hub>>(`${this.base}/admin/hubs/${id}/toggle`, null);
  }

  getAdminDeliveries(page = 0, size = 20): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any>>(`${this.base}/admin/deliveries`, { params });
  }

  generateReport(req: ReportRequest): Observable<ApiResponse<Report>> {
    return this.http.post<ApiResponse<Report>>(`${this.base}/admin/reports`, req);
  }

  getReports(): Observable<ApiResponse<Report[]>> {
    return this.http.get<ApiResponse<Report[]>>(`${this.base}/admin/reports`);
  }

  resolveException(deliveryId: number, resolution: string): Observable<ApiResponse<ExceptionLog>> {
    return this.http.post<ApiResponse<ExceptionLog>>(`${this.base}/admin/deliveries/resolve`, { deliveryId, resolution });
  }
}
