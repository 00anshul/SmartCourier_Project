import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Delivery } from '../../../core/models/delivery.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatDividerModule, StatusBadgeComponent],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">My Dashboard</h2>
        <p class="page-subtitle">Welcome back, {{ authService.getFullName() }}!</p>
      </div>
      <a mat-flat-button routerLink="/customer/book" id="dashboard-book-btn" class="primary-btn">
        <mat-icon>add</mat-icon> Book Parcel
      </a>
    </div>

    <!-- Summary Cards -->
    <div class="stats-grid">
      <mat-card class="stat-card" *ngFor="let stat of stats">
        <mat-card-content>
          <div class="stat-icon" [style.background]="stat.bg">
            <mat-icon [style.color]="stat.color">{{ stat.icon }}</mat-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>

    <!-- Recent Parcels -->
    <mat-card class="recent-card">
      <mat-card-header>
        <mat-card-title>Recent Parcels</mat-card-title>
        <a mat-button routerLink="/customer/history" id="view-all-link" class="view-all-btn">View all</a>
      </mat-card-header>
      <mat-divider></mat-divider>
      <mat-card-content>
        <div *ngIf="deliveries.length === 0 && !loading" class="empty-state">
          <mat-icon>inventory_2</mat-icon>
          <p>No parcels yet</p>
          <a mat-flat-button routerLink="/customer/book" class="primary-btn">Book your first parcel</a>
        </div>
        <div class="parcel-list" *ngIf="deliveries.length > 0">
          <div class="parcel-item" *ngFor="let d of deliveries.slice(0, 5)" [id]="'parcel-item-' + d.id">
            <div class="parcel-info">
              <div class="tracking-num">{{ d.trackingNumber }}</div>
              <div class="parcel-meta">
                <span>{{ d.serviceType }}</span>
                <span class="dot">•</span>
                <span>₹{{ d.totalCharge | number:'1.2-2' }}</span>
                <span class="dot">•</span>
                <span>Created: {{ d.createdAt + 'Z' | date:'dd MMM yyyy' }}</span>
                <span class="dot">•</span>
                <span>Updated: {{ d.updatedAt + 'Z' | date:'dd MMM yyyy, h:mm a' }}</span>
              </div>
            </div>
            <div class="parcel-actions">
              <app-status-badge [status]="d.status"></app-status-badge>
              <button *ngIf="d.status === 'BOOKED'" mat-icon-button class="cancel-btn" (click)="cancelDelivery(d.id)" title="Cancel Parcel">
                <mat-icon>cancel</mat-icon>
              </button>
            </div>
          </div>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .primary-btn { background: #1E3A5F; color: white; border-radius: 8px; display: flex; align-items: center; gap: 4px; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 28px; }
    .stat-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    .stat-card mat-card-content { display: flex; align-items: center; gap: 16px; padding: 20px !important; }
    .stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
    .stat-value { font-size: 1.8rem; font-weight: 700; color: #111827; line-height: 1; }
    .stat-label { font-size: 0.8rem; color: #6B7280; margin-top: 4px; }
    .recent-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    mat-card-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px 8px; }
    mat-card-title { font-size: 1rem !important; font-weight: 600 !important; color: #111827 !important; }
    .view-all-btn { color: #2563EB; font-size: 0.875rem; }
    mat-card-content { padding: 0 !important; }
    .parcel-list { padding: 8px 0; }
    .parcel-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-bottom: 1px solid #F3F4F6; transition: background 0.15s; }
    .parcel-item:last-child { border-bottom: none; }
    .parcel-item:hover { background: #F9FAFB; }
    .tracking-num { font-weight: 600; color: #111827; font-size: 0.9rem; font-family: monospace; }
    .parcel-meta { font-size: 0.78rem; color: #9CA3AF; margin-top: 3px; display: flex; gap: 4px; }
    .dot { color: #D1D5DB; }
    .empty-state { display: flex; flex-direction: column; align-items: center; padding: 48px; color: #9CA3AF; gap: 12px; }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; }
    .empty-state p { margin: 0; font-size: 1rem; }
    .parcel-actions { display: flex; align-items: center; gap: 4px; }
    .cancel-btn { color: #DC2626; transform: scale(0.9); width: 36px; height: 36px; padding: 6px; }
  `]
})
export class CustomerDashboardComponent implements OnInit {
  private api = inject(ApiService);
  private cdr = inject(ChangeDetectorRef);
  private snackBar = inject(MatSnackBar);
  authService = inject(AuthService);

  deliveries: Delivery[] = [];
  loading = true;

  stats: { label: string; value: number; icon: string; bg: string; color: string }[] = [];

  ngOnInit(): void {
    this.api.getMyDeliveries(0, 20).subscribe({
      next: (res) => {
        this.deliveries = res.data?.content ?? [];
        this.buildStats();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private buildStats(): void {
    const all = this.deliveries;
    this.stats = [
      { label: 'Total Parcels',  value: all.length,                                                      icon: 'inventory_2',    bg: '#EFF6FF', color: '#1E3A5F' },
      { label: 'Pending',        value: all.filter(d => d.status === 'BOOKED').length,                  icon: 'hourglass_empty', bg: '#FEF3C7', color: '#D97706' },
      { label: 'In Transit',     value: all.filter(d => d.status === 'IN_TRANSIT').length,               icon: 'local_shipping', bg: '#DBEAFE', color: '#2563EB' },
      { label: 'Delivered',      value: all.filter(d => d.status === 'DELIVERED').length,                icon: 'check_circle',   bg: '#D1FAE5', color: '#059669' },
    ];
  }

  cancelDelivery(id: number): void {
    if (confirm('Are you sure you want to cancel this parcel?')) {
      this.api.cancelDelivery(id).subscribe({
        next: () => {
          this.snackBar.open('Parcel cancelled successfully', '✓', { duration: 3000, panelClass: ['sc-snackbar-success'] });
          this.ngOnInit(); // Refresh list
        },
        error: () => {
          this.snackBar.open('Failed to cancel parcel', 'X', { duration: 3000, panelClass: ['sc-snackbar-error'] });
        }
      });
    }
  }
}
