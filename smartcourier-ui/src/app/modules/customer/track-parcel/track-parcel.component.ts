import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { ApiService } from '../../../core/services/api.service';
import { Delivery } from '../../../core/models/delivery.model';
import { TrackingEvent } from '../../../core/models/tracking.model';
import { StatusTimelineComponent } from '../../../shared/components/status-timeline/status-timeline.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-track-parcel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
            MatButtonModule, MatIconModule, MatDividerModule, StatusTimelineComponent, StatusBadgeComponent],
  template: `
    <div class="page-header">
      <h2 class="page-title">Track Parcel</h2>
      <p class="page-subtitle">Enter a tracking number to see real-time status</p>
    </div>

    <mat-card class="search-card">
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSearch()" class="search-form" id="track-form">
          <mat-form-field appearance="outline" class="search-input">
            <mat-label>Tracking Number</mat-label>
            <input matInput formControlName="trackingNumber" placeholder="e.g. SC-1234567890" id="tracking-number-input">
            <mat-icon matSuffix>search</mat-icon>
            <mat-error *ngIf="form.get('trackingNumber')?.hasError('required')">Tracking number is required</mat-error>
          </mat-form-field>
          <button mat-flat-button type="submit" [disabled]="form.invalid || loading" class="search-btn" id="track-search-btn">
            <mat-icon>radar</mat-icon>
            {{ loading ? 'Searching...' : 'Track' }}
          </button>
        </form>
      </mat-card-content>
    </mat-card>

    <ng-container *ngIf="delivery">
      <mat-card class="result-card">
        <mat-card-header>
          <div class="result-header">
            <div>
              <h3 class="tracking-num">{{ delivery.trackingNumber }}</h3>
              <div class="result-meta">
                <span>{{ delivery.serviceType }}</span>
                <span class="dot">&#x2022;</span>
                <span>{{ delivery.createdAt | date:'dd MMM yyyy' }}</span>
              </div>
            </div>
            <app-status-badge [status]="delivery.status"></app-status-badge>
          </div>
        </mat-card-header>
        <mat-divider></mat-divider>
        <mat-card-content>
          <h4 class="section-label">Shipment Journey</h4>
          <app-status-timeline [currentStatus]="delivery.status" [events]="trackingEvents"></app-status-timeline>
          <mat-divider></mat-divider>
          <div class="details-grid" *ngIf="delivery.addresses?.length">
            <div class="detail-block">
              <div class="detail-label">From</div>
              <div class="detail-value" *ngFor="let a of getSenderAddress()">
                {{ a.contactName }}<br>{{ a.street }}, {{ a.city }}
              </div>
            </div>
            <div class="detail-block">
              <div class="detail-label">To</div>
              <div class="detail-value" *ngFor="let a of getReceiverAddress()">
                {{ a.contactName }}<br>{{ a.street }}, {{ a.city }}
              </div>
            </div>
            <div class="detail-block">
              <div class="detail-label">Charge</div>
              <div class="detail-value">&#x20B9;{{ delivery.totalCharge | number:'1.2-2' }}</div>
            </div>
          </div>
          <div class="events-section" *ngIf="trackingEvents.length > 0">
            <h4 class="section-label">Tracking History</h4>
            <div class="event-item" *ngFor="let ev of trackingEvents">
              <div class="event-time">{{ ev.eventTime | date:'dd MMM, HH:mm' }}</div>
              <div class="event-body">
                <div class="event-status">{{ ev.status }}</div>
                <div class="event-desc">{{ ev.description }}</div>
                <div class="event-location" *ngIf="ev.location">
                  <mat-icon>place</mat-icon>{{ ev.location }}
                </div>
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </ng-container>

    <div class="not-found" *ngIf="notFound">
      <mat-icon>search_off</mat-icon>
      <p>No parcel found with tracking number <strong>{{ form.value.trackingNumber }}</strong></p>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .search-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; margin-bottom: 24px; }
    .search-card mat-card-content { padding: 20px !important; }
    .search-form { display: flex; gap: 12px; align-items: flex-start; }
    .search-input { flex: 1; }
    .search-btn { height: 56px; background: #1E3A5F; color: white; border-radius: 8px; display: flex; align-items: center; gap: 6px; font-size: 1rem; }
    .result-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    mat-card-header { padding: 20px 24px 12px !important; }
    .result-header { display: flex; justify-content: space-between; align-items: flex-start; width: 100%; }
    .tracking-num { font-size: 1.2rem; font-weight: 700; font-family: monospace; color: #111827; margin: 0 0 4px; }
    .result-meta { font-size: 0.8rem; color: #9CA3AF; display: flex; gap: 4px; }
    .dot { color: #D1D5DB; }
    mat-card-content { padding: 20px 24px !important; }
    .section-label { font-size: 0.85rem; font-weight: 600; color: #6B7280; text-transform: uppercase; letter-spacing: 0.5px; margin: 16px 0 12px; }
    .details-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; margin: 20px 0; }
    .detail-label { font-size: 0.75rem; font-weight: 600; color: #9CA3AF; text-transform: uppercase; margin-bottom: 4px; }
    .detail-value { font-size: 0.875rem; color: #374151; line-height: 1.5; }
    .events-section { margin-top: 20px; }
    .event-item { display: flex; gap: 16px; padding: 12px 0; border-bottom: 1px solid #F3F4F6; }
    .event-time { font-size: 0.75rem; color: #9CA3AF; min-width: 100px; }
    .event-status { font-weight: 600; font-size: 0.875rem; color: #1E3A5F; }
    .event-desc { font-size: 0.8rem; color: #6B7280; }
    .event-location { display: flex; align-items: center; gap: 2px; font-size: 0.75rem; color: #9CA3AF; margin-top: 3px; }
    .event-location mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .not-found { display: flex; flex-direction: column; align-items: center; padding: 60px; color: #9CA3AF; gap: 12px; }
    .not-found mat-icon { font-size: 56px; width: 56px; height: 56px; }
    .not-found p { font-size: 1rem; margin: 0; }
  `]
})
export class TrackParcelComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private cdr = inject(ChangeDetectorRef);

  form = this.fb.group({ trackingNumber: ['', Validators.required] });
  delivery: Delivery | null = null;
  trackingEvents: TrackingEvent[] = [];
  loading = false;
  notFound = false;

  onSearch(): void {
    if (this.form.invalid) return;
    
    this.loading = true;
    this.notFound = false;
    this.delivery = null;
    this.cdr.detectChanges(); // Force synchronous update

    const tn = this.form.value.trackingNumber!.trim();

    this.api.trackByNumber(tn).subscribe({
      next: (res) => {
        this.delivery = res.data;
        this.cdr.detectChanges(); // Fix NG0100 for delivery object
        
        this.api.getTrackingEvents(tn).subscribe({
          next: (evRes) => { 
            this.trackingEvents = evRes.data ?? []; 
            this.loading = false;
            this.cdr.detectChanges();
          },
          error: () => { 
            this.trackingEvents = []; 
            this.loading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: () => { 
        this.notFound = true; 
        this.loading = false; 
        this.cdr.detectChanges();
      }
    });
  }

  getSenderAddress() { return this.delivery?.addresses?.filter(a => a.type === 'SENDER') ?? []; }
  getReceiverAddress() { return this.delivery?.addresses?.filter(a => a.type === 'RECEIVER') ?? []; }
}
